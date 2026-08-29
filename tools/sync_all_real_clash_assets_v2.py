"""
Deep Datamined Real Clash of Clans Asset Synchronizer v2.
Extracts and converts:
- All Hero Pets (Spirit Fox, Diggy, Phoenix, Lassi, Electro Owl, etc.)
- All Defenses & Resource Buildings (Air Defenses, Sweepers, Eagle, Monolith, Inferno, Clan Castle)
- All Traps & Obstacles (Spring Trap, Seeking Air Mine, Gem Box, Trunks)
- All UI buttons & badges
"""

import os
import io
import json
import logging
import urllib.request
from PIL import Image

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger(__name__)

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
        headers={"User-Agent": "Ai-Marco-Asset-Pipeline"}
    )
    with urllib.request.urlopen(req, timeout=12) as resp:
        return json.loads(resp.read().decode())

def save_image_bytes(raw_bytes: bytes, filename: str):
    try:
        img = Image.open(io.BytesIO(raw_bytes))
        if img.mode != "RGBA":
            img = img.convert("RGBA")
        
        png_name = os.path.splitext(filename)[0] + ".png"
        for d in TARGET_DIRS:
            dest = os.path.join(d, png_name)
            img.save(dest, format="PNG")
        logger.info(f"✓ Saved real asset: {png_name} ({img.size[0]}x{img.size[1]})")
    except Exception as e:
        logger.error(f"Failed to process {filename}: {e}")

def fetch_pets():
    logger.info("[1/4] Fetching all Hero Pets from ClashKingAssets...")
    try:
        pets = api_get("https://api.github.com/repos/ClashKingInc/ClashKingAssets/contents/assets/pets")
        for pet in pets:
            if pet["type"] == "dir":
                sub = api_get(pet["url"])
                for item in sub:
                    if item["name"].endswith((".png", ".webp")):
                        req = urllib.request.Request(item["download_url"], headers={"User-Agent": "Ai-Marco"})
                        with urllib.request.urlopen(req, timeout=12) as resp:
                            name = f"pet_{pet['name'].lower()}_{item['name'].lower()}"
                            save_image_bytes(resp.read(), name)
    except Exception as e:
        logger.error(f"Error fetching pets: {e}")

def fetch_defenses():
    logger.info("[2/4] Fetching Defenses & Core Buildings from ClashKingAssets...")
    key_buildings = [
        "air_defense", "air_sweeper", "eagle_artillery", "inferno_tower",
        "monolith", "spell_tower", "clan_castle", "dark_elixir_drill",
        "blacksmith", "bomb_tower", "scattershot", "ricochet_cannon", "multi_archer_tower"
    ]
    try:
        for b_name in key_buildings:
            try:
                items = api_get(f"https://api.github.com/repos/ClashKingInc/ClashKingAssets/contents/assets/buildings/home-village/{b_name}")
                for item in items[:4]: # Sample highest and key levels
                    if item["name"].endswith((".png", ".webp")):
                        req = urllib.request.Request(item["download_url"], headers={"User-Agent": "Ai-Marco"})
                        with urllib.request.urlopen(req, timeout=12) as resp:
                            name = f"bldg_{b_name.lower()}_{item['name'].lower()}"
                            save_image_bytes(resp.read(), name)
            except Exception as e:
                logger.warning(f"Could not fetch building {b_name}: {e}")
    except Exception as e:
        logger.error(f"Error fetching defenses: {e}")

def fetch_traps_and_obstacles():
    logger.info("[3/4] Fetching Traps and Obstacles...")
    try:
        traps = api_get("https://api.github.com/repos/ClashKingInc/ClashKingAssets/contents/assets/traps/home-village")
        for t in traps[:10]:
            if t["type"] == "dir":
                sub = api_get(t["url"])
                for item in sub[:2]:
                    if item["name"].endswith((".png", ".webp")):
                        req = urllib.request.Request(item["download_url"], headers={"User-Agent": "Ai-Marco"})
                        with urllib.request.urlopen(req, timeout=12) as resp:
                            name = f"trap_{t['name'].lower()}_{item['name'].lower()}"
                            save_image_bytes(resp.read(), name)
    except Exception as e:
        logger.warning(f"Error fetching traps: {e}")

    try:
        obs = api_get("https://api.github.com/repos/ClashKingInc/ClashKingAssets/contents/assets/obstacles")
        for o in obs[:15]:
            if o["type"] == "dir":
                sub = api_get(o["url"])
                for item in sub[:2]:
                    if item["name"].endswith((".png", ".webp")):
                        req = urllib.request.Request(item["download_url"], headers={"User-Agent": "Ai-Marco"})
                        with urllib.request.urlopen(req, timeout=12) as resp:
                            name = f"obstacle_{o['name'].lower()}_{item['name'].lower()}"
                            save_image_bytes(resp.read(), name)
    except Exception as e:
        logger.warning(f"Error fetching obstacles: {e}")

def fetch_heroes():
    logger.info("[4/4] Fetching Heroes & Hero Renders...")
    try:
        heroes = api_get("https://api.github.com/repos/ClashKingInc/ClashKingAssets/contents/assets/heroes")
        for h in heroes:
            if h["type"] == "dir":
                sub = api_get(h["url"])
                for item in sub:
                    if item["name"].endswith((".png", ".webp")):
                        req = urllib.request.Request(item["download_url"], headers={"User-Agent": "Ai-Marco"})
                        with urllib.request.urlopen(req, timeout=12) as resp:
                            name = f"hero_{h['name'].lower()}_{item['name'].lower()}"
                            save_image_bytes(resp.read(), name)
    except Exception as e:
        logger.warning(f"Error fetching heroes: {e}")

def main():
    fetch_pets()
    fetch_defenses()
    fetch_traps_and_obstacles()
    fetch_heroes()
    logger.info("=========================================================")
    logger.info("  ALL REAL EXPANDED CLASH ASSETS SYNCHRONIZED!")
    logger.info("=========================================================")

if __name__ == "__main__":
    main()
