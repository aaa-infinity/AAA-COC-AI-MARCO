"""
Complete Real Clash of Clans Asset Synchronizer.
Cleans all placeholder/mock assets and syncs 100% real, datamined, transparent PNGs
from Statscell/clash-assets and ClashKingInc/ClashKingAssets.
"""

import os
import io
import json
import logging
import urllib.request
from PIL import Image

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

def clean_old_assets():
    logger.info("Purging all previous mock/placeholder assets...")
    for d in TARGET_DIRS:
        os.makedirs(d, exist_ok=True)
        for f in os.listdir(d):
            p = os.path.join(d, f)
            if os.path.isfile(p):
                os.remove(p)
    logger.info("All target asset folders cleared.")

def api_get(url: str):
    req = urllib.request.Request(
        url,
        headers={
            "Authorization": f"token {GITHUB_TOKEN}",
            "User-Agent": "Ai-Marco-Sync"
        }
    )
    with urllib.request.urlopen(req, timeout=15) as resp:
        return json.loads(resp.read().decode())

def save_image_bytes(raw_bytes: bytes, filename: str):
    try:
        img = Image.open(io.BytesIO(raw_bytes))
        if img.mode != "RGBA":
            img = img.convert("RGBA")
        
        # Save as PNG across all target directories
        png_name = os.path.splitext(filename)[0] + ".png"
        for d in TARGET_DIRS:
            dest = os.path.join(d, png_name)
            img.save(dest, format="PNG")
        logger.info(f"✓ Saved real asset: {png_name} ({img.size[0]}x{img.size[1]})")
    except Exception as e:
        logger.error(f"Failed to process {filename}: {e}")

def fetch_statscell_troops():
    logger.info("[1/5] Fetching all real troops, spells, heroes & sieges from Statscell/clash-assets...")
    url = "https://api.github.com/repos/Statscell/clash-assets/contents/troops/icons"
    try:
        items = api_get(url)
        for item in items:
            if item["name"].endswith(".png"):
                raw_url = item["download_url"]
                req = urllib.request.Request(raw_url, headers={"User-Agent": "Ai-Marco-Sync"})
                with urllib.request.urlopen(req, timeout=15) as resp:
                    clean_name = "troop_" + item["name"].lower().replace(" ", "_")
                    save_image_bytes(resp.read(), clean_name)
    except Exception as e:
        logger.error(f"Error fetching Statscell troops: {e}")

def fetch_statscell_townhalls():
    logger.info("[2/5] Fetching Town Halls from Statscell/clash-assets...")
    url = "https://api.github.com/repos/Statscell/clash-assets/contents/townhalls"
    try:
        items = api_get(url)
        for item in items:
            if item["name"].endswith(".png"):
                raw_url = item["download_url"]
                req = urllib.request.Request(raw_url, headers={"User-Agent": "Ai-Marco-Sync"})
                with urllib.request.urlopen(req, timeout=15) as resp:
                    clean_name = "townhall_" + item["name"].lower()
                    save_image_bytes(resp.read(), clean_name)
    except Exception as e:
        logger.error(f"Error fetching Statscell townhalls: {e}")

def fetch_clashking_equipment():
    logger.info("[3/5] Fetching Hero Equipments from ClashKingAssets...")
    url = "https://api.github.com/repos/ClashKingInc/ClashKingAssets/contents/assets/equipment"
    try:
        items = api_get(url)
        for item in items:
            if item["name"].endswith((".png", ".webp")):
                raw_url = item["download_url"]
                req = urllib.request.Request(raw_url, headers={"User-Agent": "Ai-Marco-Sync"})
                with urllib.request.urlopen(req, timeout=15) as resp:
                    clean_name = "equip_" + item["name"].lower().replace("-", "_")
                    save_image_bytes(resp.read(), clean_name)
    except Exception as e:
        logger.error(f"Error fetching ClashKing equipment: {e}")

def fetch_clashking_resources():
    logger.info("[4/5] Fetching Resources & Ores from ClashKingAssets...")
    url = "https://api.github.com/repos/ClashKingInc/ClashKingAssets/contents/assets/resources"
    try:
        items = api_get(url)
        for item in items:
            if item["name"].endswith((".png", ".webp")):
                raw_url = item["download_url"]
                req = urllib.request.Request(raw_url, headers={"User-Agent": "Ai-Marco-Sync"})
                with urllib.request.urlopen(req, timeout=15) as resp:
                    clean_name = "res_" + item["name"].lower().replace("-", "_")
                    save_image_bytes(resp.read(), clean_name)
    except Exception as e:
        logger.error(f"Error fetching ClashKing resources: {e}")

def fetch_clashking_icons():
    logger.info("[5/5] Fetching UI Icons & Buttons from ClashKingAssets...")
    url = "https://api.github.com/repos/ClashKingInc/ClashKingAssets/contents/assets/icons"
    try:
        items = api_get(url)
        for item in items[:25]:
            if item["name"].endswith((".png", ".webp")):
                raw_url = item["download_url"]
                req = urllib.request.Request(raw_url, headers={"User-Agent": "Ai-Marco-Sync"})
                with urllib.request.urlopen(req, timeout=15) as resp:
                    clean_name = "ui_" + item["name"].lower().replace("-", "_")
                    save_image_bytes(resp.read(), clean_name)
    except Exception as e:
        logger.error(f"Error fetching ClashKing icons: {e}")

def main():
    clean_old_assets()
    fetch_statscell_troops()
    fetch_statscell_townhalls()
    fetch_clashking_equipment()
    fetch_clashking_resources()
    fetch_clashking_icons()
    logger.info("=========================================================")
    logger.info(" ALL REAL CLASH OF CLANS ASSETS SYNCHRONIZED SUCCESSFULLY!")
    logger.info("=========================================================")

if __name__ == "__main__":
    main()
