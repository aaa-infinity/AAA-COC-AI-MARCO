"""
Army Trainer and Readiness Inspector AI.
Handles quick training, army camp checks, and spell brewing verification.
"""

import time
import logging
from typing import Tuple, Optional
from core.device import DeviceController
from core.screen import ScreenCapture
from vision.template_matcher import TemplateMatcher
from config.settings import AppConfig

logger = logging.getLogger(__name__)


class ArmyBot:
    """
    Manages army training presets and verifies readiness before raids.
    """
    def __init__(self, device: DeviceController, screen: ScreenCapture, matcher: TemplateMatcher, config: AppConfig):
        self.device = device
        self.screen = screen
        self.matcher = matcher
        self.config = config

    def train_preset_army(self) -> bool:
        """
        Opens army screen, switches to Quick Train, and queues the chosen preset slot.
        """
        logger.info(f"Training army preset slot {self.config.village.army_slot}...")
        frame = self.screen.capture()

        # Step 1: Tap train button on bottom left
        train_btn = self.matcher.find_template(frame, "btn_train_army", threshold=0.75)
        if train_btn:
            self.device.tap(train_btn.center_x, train_btn.center_y)
        else:
            # Default reference coordinate for Train button (x: 85, y: 830)
            x, y = self.device.scale_coords(85, 830)
            self.device.tap(x, y)
        time.sleep(1.2)

        # Step 2: Tap 'Quick Train' tab (3rd tab at top)
        frame = self.screen.capture()
        quick_train_tab = self.matcher.find_template(frame, "tab_quick_train", threshold=0.75)
        if quick_train_tab:
            self.device.tap(quick_train_tab.center_x, quick_train_tab.center_y)
        else:
            # Quick train tab coordinate (approx x: 1350, y: 150)
            x, y = self.device.scale_coords(1350, 150)
            self.device.tap(x, y)
        time.sleep(1.0)

        # Step 3: Tap 'Train' button for selected slot
        slot = self.config.village.army_slot
        # Slot 1: y ~ 380, Slot 2: y ~ 620, Slot 3: y ~ 860, x ~ 1580
        slot_y_map = {1: 380, 2: 620, 3: 860}
        target_y = slot_y_map.get(slot, 380)
        train_slot_x, train_slot_y = self.device.scale_coords(1580, target_y)

        # Check if train button is visible for this slot
        self.device.tap(train_slot_x, train_slot_y)
        time.sleep(0.8)

        # Step 4: Close training screen
        close_btn = self.matcher.find_template(self.screen.capture(), "btn_close_window", threshold=0.75)
        if close_btn:
            self.device.tap(close_btn.center_x, close_btn.center_y)
        else:
            # Close button (x: 1820, y: 85)
            x, y = self.device.scale_coords(1820, 85)
            self.device.tap(x, y)

        logger.info("Army queued successfully.")
        return True

    def is_army_ready(self) -> bool:
        """
        Verifies if army camps and spells are fully prepared for battle.
        Checks for the green checkmark badge on the train button or full army popup.
        """
        if not self.config.village.wait_for_full_army:
            return True

        frame = self.screen.capture()
        ready_badge = self.matcher.find_template(frame, "badge_army_full", threshold=0.78)
        if ready_badge:
            logger.info("Army full badge detected. Ready to attack!")
            return True

        # Check if train button has green checkmark
        train_full = self.matcher.find_template(frame, "btn_train_ready", threshold=0.78)
        if train_full:
            logger.info("Train button is in ready state.")
            return True

        logger.info("Army is still training or not full yet.")
        return False
