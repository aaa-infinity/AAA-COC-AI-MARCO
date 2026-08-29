"""
Upload Directory Watcher & Screenshot-to-Code Pipeline.
Monitors /root/projects/Ai-marco/uploads/ for in-game screenshots and executes the Vision AI parser.
"""

import os
import sys
import time
import json
import logging

project_root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
if project_root not in sys.path:
    sys.path.insert(0, project_root)

from ai.screenshot_to_code_engine import ScreenshotToCodeEngine

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger(__name__)

UPLOAD_DIRS = [
    os.path.join(project_root, "uploads/screenshots"),
    os.path.join(project_root, "uploads/bases"),
    os.path.join(project_root, "uploads/templates"),
    os.path.join(project_root, "uploads")
]

PROCESSED_DIR = os.path.join(project_root, "uploads/processed")
os.makedirs(PROCESSED_DIR, exist_ok=True)

def process_upload_file(file_path: str, engine: ScreenshotToCodeEngine):
    filename = os.path.basename(file_path)
    base_name = os.path.splitext(filename)[0]
    out_json = os.path.join(PROCESSED_DIR, f"{base_name}_analysis.json")
    out_py = os.path.join(PROCESSED_DIR, f"{base_name}_action.py")

    logger.info(f"⚡ Processing uploaded screenshot: {filename}")
    result = engine.analyze_screenshot(file_path)

    with open(out_json, "w", encoding="utf-8") as f:
        json.dump(result, f, indent=2)

    with open(out_py, "w", encoding="utf-8") as f:
        f.write(result.get("generated_macro_code", "# No code generated"))

    logger.info(f"✓ Analysis saved: {out_json}")
    logger.info(f"✓ Action Code generated: {out_py}")
    logger.info(f"  Game State: {result.get('game_state')} (Confidence: {result.get('confidence')})")
    tactics = result.get("tactical_analysis", {})
    logger.info(f"  Optimal Entry: {tactics.get('recommended_entry_side')}")
    logger.info(f"  Zap Targets: {tactics.get('zap_targets')}")

def scan_and_process():
    engine = ScreenshotToCodeEngine()
    logger.info("==================================================")
    logger.info("  AAA COC AI - SCREENSHOT-TO-CODE UPLOAD WATCHER  ")
    logger.info("==================================================")
    logger.info(f"Monitoring folder: {os.path.join(project_root, 'uploads')}")

    processed_files = set()
    for d in UPLOAD_DIRS:
        if not os.path.exists(d):
            os.makedirs(d, exist_ok=True)

    valid_exts = {".png", ".jpg", ".jpeg", ".webp"}
    for d in UPLOAD_DIRS:
        for f in os.listdir(d):
            fp = os.path.join(d, f)
            if os.path.isfile(fp) and os.path.splitext(f)[1].lower() in valid_exts:
                if fp not in processed_files:
                    process_upload_file(fp, engine)
                    processed_files.add(fp)

    logger.info("Initial scan complete. Ready for new screenshot uploads.")

if __name__ == "__main__":
    scan_and_process()
