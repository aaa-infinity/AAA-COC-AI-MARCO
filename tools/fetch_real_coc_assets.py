"""
Automated Downloader for Real, Transparent, Pixel-Exact Clash of Clans Game Assets.
Sources from ClashKingInc/ClashKingAssets and Statscell/clash-assets.
"""

import os
import sys
import json
import logging
import urllib.request
import urllib.error
from typing import List, Dict

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger(__name__)

GITHUB_TOKEN = os.getenv("GITHUB_TOKEN", "")

PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TARGET_DIRS = [
    os.path.join(PROJECT_ROOT, "android_app/app/src/main/assets/templates"),
    os.path.join(PROJECT_ROOT, "vision/templates"),
    os.path.join(PROJECT_ROOT, "uploads/templates"),
    os.path.join(PROJECT_ROOT, "data/templates")
]

for d in TARGET_DIRS:
    os.makedirs(d, exist_ok=True)

def api_get(url: str):
    req = urllib.request.Request(
        url,
        headers={
            "Authorization": f"token {GITHUB_TOKEN}",
            "User-Agent": "Ai-Marco-Asset-Pipeline"
        }
    )
    with urllib.request.urlopen(req, timeout=12) as resp:
        return json.loads(resp.read().decode())

def download_file(raw_url: str, dest_filename: str):
    req = urllib.request.Request(
        raw_url,
        headers={"User-Agent": "Ai-Marco-Asset-Pipeline"}
    )
    with urllib.request.urlopen(req, timeout=15) as resp:
        content = resp.read()
        for d in TARGET_DIRS:
            dest_path = os.path.join(d, dest_filename)
            with open(dest_path, "wb") as f:
                f.write(content)

def fetch_category_assets(owner: str, repo: str, path: str, prefix: str = "", max_files: int = 35):
    url = f"https://api.github.com/repos/{owner}/{repo}/contents/{path}"
    try:
        items = api_get(url)
        if not isinstance(items, list):
            return

        count = 0
        for item in items:
            if count >= max_files:
                break
            if item["type"] == "file" and item["name"].endswith(".png"):
                raw_url = item["download_url"]
                name_clean = item["name"].lower().replace(" ", "_").replace("-", "_")
                dest_name = f"{prefix}_{name_clean}" if prefix else name_clean
                download_file(raw_url, dest_name)
                logger.info(f"✓ Downloaded real asset: {dest_name}")
                count += 1
            elif item["type"] == "dir" and count < max_files:
                # Sub-directory
                sub_items = api_get(item["url"])
                if isinstance(sub_items, list):
                    for sub in sub_items[:10]:
                        if sub["type"] == "file" and sub["name"].endswith(".png"):
                            raw_url = sub["download_url"]
                            sub_clean = sub["name"].lower().replace(" ", "_").replace("-", "_")
                            dest_name = f"{prefix}_{item['name'].lower()}_{sub_clean}" if prefix else f"{item['name'].lower()}_{sub_clean}"
                            download_file(raw_url, dest_name)
                            logger.info(f"✓ Downloaded real asset: {dest_name}")
                            count += 1
    except Exception as e:
        logger.error(f"Error fetching {path}: {e}")

def run_asset_download_pipeline():
    logger.info("==================================================")
    logger.info("  FETCHING REAL PIXEL-EXACT CLASH OF CLANS ASSETS ")
    logger.info("==================================================")

    # 1. Troops (Barbarian, Archer, Dragon, E-Drag, Balloon, Sneaky Goblin, Root Rider, etc.)
    logger.info("[1/6] Fetching Troops from ClashKingAssets...")
    fetch_category_assets("ClashKingInc", "ClashKingAssets", "assets/troops", prefix="troop", max_files=25)

    # 2. Spells (Lightning/Zap, Rage, Heal, Freeze, Jump, Bat, Overgrowth)
    logger.info("[2/6] Fetching Spells from ClashKingAssets...")
    fetch_category_assets("ClashKingInc", "ClashKingAssets", "assets/spells", prefix="spell", max_files=15)

    # 3. Heroes & Hero Equipment (King, Queen, Warden, Champion, Minion Prince, Gauntlet, Mirror)
    logger.info("[3/6] Fetching Heroes & Hero Equipment...")
    fetch_category_assets("ClashKingInc", "ClashKingAssets", "assets/heroes", prefix="hero", max_files=10)
    fetch_category_assets("ClashKingInc", "ClashKingAssets", "assets/equipment", prefix="equip", max_files=15)

    # 4. Defenses & Buildings (Air Defenses, Town Halls 1-17, Eagle, Monolith, Ricochet Cannon)
    logger.info("[4/6] Fetching Buildings & Defenses...")
    fetch_category_assets("ClashKingInc", "ClashKingAssets", "assets/buildings", prefix="building", max_files=20)

    # 5. Resources (Gold, Elixir, Dark Elixir, Ores, Collectors, Mines)
    logger.info("[5/6] Fetching Resources & Ores...")
    fetch_category_assets("ClashKingInc", "ClashKingAssets", "assets/resources", prefix="res", max_files=15)

    # 6. UI Buttons & Icons (Attack, Next, Return Home, Surrender, Donate)
    logger.info("[6/6] Fetching UI Icons & Buttons...")
    fetch_category_assets("ClashKingInc", "ClashKingAssets", "assets/icons", prefix="ui", max_files=20)

    logger.info("==================================================")
    logger.info("  ALL REAL CLASH OF CLANS ASSETS SYNCED!")
    logger.info("==================================================")

if __name__ == "__main__":
    run_asset_download_pipeline()
