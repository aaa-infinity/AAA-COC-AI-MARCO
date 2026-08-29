"""
Matchmaker & Base Evaluator AI.
Automates 'Next' button searches and OCR-based loot filtering.
"""

import time
import random
import logging
from typing import Tuple, Optional
from core.device import DeviceController
from core.screen import ScreenCapture
from vision.template_matcher import TemplateMatcher
from vision.ocr_reader import OCRReader, LootInfo
from config.settings import AppConfig

logger = logging.getLogger(__name__)


class MatchResultStatus:
    TARGET_FOUND = "TARGET_FOUND"
    MAX_ATTEMPTS = "MAX_ATTEMPTS"
    CLOUDS_TIMEOUT = "CLOUDS_TIMEOUT"
    ERROR = "ERROR"


class MatchmakerBot:
    """
    Searches for opponent bases and validates against minimum loot criteria.
    """
    def __init__(
        self,
        device: DeviceController,
        screen: ScreenCapture,
        matcher: TemplateMatcher,
        ocr: OCRReader,
        config: AppConfig
    ):
        self.device = device
        self.screen = screen
        self.matcher = matcher
        self.ocr = ocr
        self.config = config
        self.search_count = 0
        self.current_loot: Optional[LootInfo] = None

    def start_matchmaking(self) -> bool:
        """Taps Attack -> Find a Match."""
        logger.info("Initiating matchmaking search...")
        self.search_count = 0

        # Step 1: Tap Attack button on main screen (bottom-left)
        frame = self.screen.capture()
        attack_btn = self.matcher.find_template(frame, "btn_attack_main", threshold=0.75)
        if attack_btn:
            self.device.tap(attack_btn.center_x, attack_btn.center_y)
        else:
            x, y = self.device.scale_coords(120, 950)
            self.device.tap(x, y)
        time.sleep(1.2)

        # Step 2: Tap 'Find a Match' (Multiplayer Battle button)
        frame = self.screen.capture()
        find_match_btn = self.matcher.find_template(frame, "btn_find_match", threshold=0.75)
        if find_match_btn:
            self.device.tap(find_match_btn.center_x, find_match_btn.center_y)
        else:
            # Multiplayer battle button (approx x: 1450, y: 650)
            x, y = self.device.scale_coords(1450, 650)
            self.device.tap(x, y)

        return True

    def wait_for_battle_screen(self, timeout_sec: float = 20.0) -> bool:
        """Waits until clouds disappear and the battle screen / Next button loads."""
        start_time = time.time()
        while time.time() - start_time < timeout_sec:
            frame = self.screen.capture()
            # Check for 'Next' button or 'End Battle' button
            next_btn = self.matcher.find_template(frame, "btn_next", threshold=0.75)
            end_btn = self.matcher.find_template(frame, "btn_end_battle", threshold=0.75)
            if next_btn or end_btn:
                time.sleep(0.5)  # Let loot counter animation settle
                return True
            time.sleep(0.5)

        logger.warning("Clouds loading timed out.")
        return False

    def evaluate_current_base(self) -> Tuple[bool, LootInfo]:
        """
        Reads loot on screen and tests against minimum thresholds.
        """
        frame = self.screen.capture()
        loot = self.ocr.read_loot(frame)
        self.current_loot = loot

        logger.info(
            f"[Search #{self.search_count}] Base Loot: "
            f"Gold={loot.gold:,} | Elixir={loot.elixir:,} | Dark={loot.dark_elixir:,} | Trophies={loot.trophies}"
        )

        meets = loot.meets_criteria(
            min_gold=self.config.farming.min_gold,
            min_elixir=self.config.farming.min_elixir,
            min_dark=self.config.farming.min_dark_elixir
        )

        return meets, loot

    def next_base(self) -> bool:
        """Taps the Next button to skip current base."""
        self.search_count += 1
        frame = self.screen.capture()
        next_btn = self.matcher.find_template(frame, "btn_next", threshold=0.75)

        if next_btn:
            self.device.tap(next_btn.center_x, next_btn.center_y)
        else:
            # Default 'Next' coordinate (approx x: 1720, y: 880)
            x, y = self.device.scale_coords(1720, 880)
            self.device.tap(x, y)

        # Anti-ban humanized search delay
        time.sleep(random.uniform(1.8, 3.2))
        return self.wait_for_battle_screen()
