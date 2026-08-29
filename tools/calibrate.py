"""
Calibration and Diagnostic Utility.
Tests device connectivity, captures calibration screenshots, and validates OCR & template detection.
"""

import os
import sys

# Ensure project root is on sys.path
PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
if PROJECT_ROOT not in sys.path:
    sys.path.insert(0, PROJECT_ROOT)

import logging
from core.device import DeviceController
from core.screen import ScreenCapture
from vision.ocr_reader import OCRReader
from vision.template_matcher import TemplateMatcher
from config.settings import AppConfig

logging.basicConfig(level=logging.INFO, format='%(asctime)s [%(levelname)s] %(message)s')
logger = logging.getLogger(__name__)


def run_calibration():
    print("=" * 60)
    print("   CLASH OF CLANS AI & MACRO - CALIBRATION SUITE   ")
    print("=" * 60)

    config = AppConfig.load()
    print(f"[*] Reference Resolution: {config.device.ref_width}x{config.device.ref_height}")
    print(f"[*] Target Host: {config.device.adb_host}:{config.device.adb_port}")

    # Step 1: Connect to Device
    print("\n[1/4] Checking ADB Device Connection...")
    device = DeviceController(
        serial=config.device.device_serial,
        adb_host=config.device.adb_host,
        adb_port=config.device.adb_port
    )

    if device.connected:
        print(f"[+] Device Connected: {device.serial}")
        print(f"[+] Native Resolution: {device.actual_w}x{device.actual_h}")
    else:
        print("[-] No physical device or emulator found. Operating in MOCK mode.")

    # Step 2: Screen Capture Test
    print("\n[2/4] Testing Screen Capture...")
    screen = ScreenCapture(device_serial=device.serial)
    frame = screen.capture()
    if hasattr(frame, 'shape'):
        print(f"[+] Captured frame shape: {frame.shape} (H={frame.shape[0]}, W={frame.shape[1]})")
    elif hasattr(frame, 'size'):
        print(f"[+] Captured frame size: {frame.size} (W={frame.size[0]}, H={frame.size[1]})")
    save_path = screen.save_debug(frame, name_prefix="calibration_test")
    print(f"[+] Saved calibration snapshot to: {save_path}")

    # Step 3: OCR Test
    print("\n[3/4] Testing OCR Loot Reader...")
    ocr = OCRReader(backend=config.ocr_backend)
    loot = ocr.read_loot(frame)
    print(f"[+] Detected Loot on Screen: {loot}")

    # Step 4: Template Matcher Test
    print("\n[4/4] Testing Template Matcher...")
    matcher = TemplateMatcher(templates_dir=config.templates_dir)
    print(f"[+] Loaded {len(matcher.templates)} template(s) from {config.templates_dir}")

    for tmpl_name in matcher.templates.keys():
        match = matcher.find_template(frame, tmpl_name, threshold=0.75)
        if match:
            print(f"    [✓] Template '{tmpl_name}' matched at {match.to_tuple()} (conf: {match.confidence:.2f})")
        else:
            print(f"    [x] Template '{tmpl_name}' not detected on current screen.")

    print("\n" + "=" * 60)
    print("   CALIBRATION COMPLETE   ")
    print("=" * 60)


if __name__ == "__main__":
    run_calibration()
