"""
Optical Character Recognition (OCR) Engine for Clash of Clans.
Extracts Gold, Elixir, Dark Elixir, Trophies, and UI numbers from game screens.
"""

import re
import logging
from typing import Dict, Optional, Tuple
from PIL import Image

try:
    import cv2
    import numpy as np
    HAS_CV2 = True
except ImportError:
    HAS_CV2 = False
    try:
        import numpy as np
    except ImportError:
        np = None

logger = logging.getLogger(__name__)


class LootInfo:
    def __init__(self, gold: int = 0, elixir: int = 0, dark_elixir: int = 0, trophies: int = 0):
        self.gold = gold
        self.elixir = elixir
        self.dark_elixir = dark_elixir
        self.trophies = trophies

    @property
    def total_standard_loot(self) -> int:
        return self.gold + self.elixir

    def meets_criteria(self, min_gold: int, min_elixir: int, min_dark: int) -> bool:
        return (
            self.gold >= min_gold and
            self.elixir >= min_elixir and
            self.dark_elixir >= min_dark
        )

    def __repr__(self):
        return f"Loot(Gold={self.gold:,}, Elixir={self.elixir:,}, Dark={self.dark_elixir:,}, Trophies={self.trophies})"


class OCRReader:
    """
    OCR parser specialized for in-game fonts and numerical counters.
    """
    def __init__(self, backend: str = "easyocr"):
        self.backend = backend
        self.reader = None
        self._init_reader()

    def _init_reader(self):
        if self.backend == "easyocr":
            try:
                import easyocr
                self.reader = easyocr.Reader(["en"], gpu=False, verbose=False)
                logger.info("EasyOCR initialized successfully.")
            except Exception as e:
                logger.debug(f"EasyOCR not available ({e}). Using regex parser.")
                self.reader = None

    def preprocess_loot_region(self, crop):
        if HAS_CV2 and hasattr(crop, "shape"):
            gray = cv2.cvtColor(crop, cv2.COLOR_BGR2GRAY)
            resized = cv2.resize(gray, (0, 0), fx=2.0, fy=2.0, interpolation=cv2.INTER_CUBIC)
            _, thresh = cv2.threshold(resized, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)
            return thresh
        elif isinstance(crop, Image.Image):
            gray = crop.convert("L")
            return gray.resize((gray.width * 2, gray.height * 2), Image.BICUBIC)
        return crop

    def parse_number(self, text: str) -> int:
        if not text:
            return 0
        cleaned = text.replace(" ", "").replace(",", "").replace("O", "0").replace("o", "0").replace("l", "1").replace("I", "1")
        match_m = re.search(r"([0-9.]+)[mM]", cleaned)
        if match_m:
            try:
                return int(float(match_m.group(1)) * 1_000_000)
            except ValueError:
                pass

        match_k = re.search(r"([0-9.]+)[kK]", cleaned)
        if match_k:
            try:
                return int(float(match_k.group(1)) * 1_000)
            except ValueError:
                pass

        digits = re.findall(r"\d+", cleaned)
        if digits:
            try:
                return int("".join(digits))
            except ValueError:
                return 0
        return 0

    def read_loot(self, screen, ref_resolution: Tuple[int, int] = (1920, 1080)) -> LootInfo:
        if HAS_CV2 and hasattr(screen, "shape"):
            h, w = screen.shape[:2]
        elif isinstance(screen, Image.Image):
            w, h = screen.size
        else:
            w, h = 1920, 1080

        scale_x = w / float(ref_resolution[0])
        scale_y = h / float(ref_resolution[1])

        g_roi = (int(50 * scale_x), int(100 * scale_y), int(380 * scale_x), int(150 * scale_y))
        e_roi = (int(50 * scale_x), int(155 * scale_y), int(380 * scale_x), int(205 * scale_y))
        d_roi = (int(50 * scale_x), int(210 * scale_y), int(380 * scale_x), int(260 * scale_y))
        t_roi = (int(50 * scale_x), int(265 * scale_y), int(380 * scale_x), int(315 * scale_y))

        gold = self._read_box_number(screen, g_roi)
        elixir = self._read_box_number(screen, e_roi)
        dark = self._read_box_number(screen, d_roi)
        trophies = self._read_box_number(screen, t_roi)

        return LootInfo(gold=gold, elixir=elixir, dark_elixir=dark, trophies=trophies)

    def _read_box_number(self, screen, roi: Tuple[int, int, int, int]) -> int:
        x1, y1, x2, y2 = roi
        if HAS_CV2 and hasattr(screen, "shape"):
            if y2 <= y1 or x2 <= x1 or y2 > screen.shape[0] or x2 > screen.shape[1]:
                return 0
            crop = screen[y1:y2, x1:x2]
        elif isinstance(screen, Image.Image):
            crop = screen.crop((x1, y1, x2, y2))
        else:
            return 0

        preprocessed = self.preprocess_loot_region(crop)

        if self.reader is not None:
            try:
                results = self.reader.readtext(preprocessed, allowlist="0123456789,.")
                if results:
                    text = " ".join([r[1] for r in results])
                    return self.parse_number(text)
            except Exception as e:
                logger.debug(f"OCR read error: {e}")

        return 0
