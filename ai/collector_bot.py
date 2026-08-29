"""
Village Resource Collector & Base Maintenance AI.
Detects full collectors, treasury, obstacle loot, and clears them automatically.
"""

import time
import logging
from typing import List, Tuple
from core.device import DeviceController
from core.screen import ScreenCapture
from vision.template_matcher import TemplateMatcher
from config.settings import AppConfig

logger = logging.getLogger(__name__)


class CollectorBot:
    """
    Automates village resource collection and base maintenance.
    """
    def __init__(self, device: DeviceController, screen: ScreenCapture, matcher: TemplateMatcher, config: AppConfig):
        self.device = device
        self.screen = screen
        self.matcher = matcher
        self.config = config

    def collect_all_resources(self) -> int:
        """
        Finds and taps resource bubbles (Gold, Elixir, Dark Elixir, Gem Mine, Cart).
        Returns the number of collected items.
        """
        logger.info("Starting village resource collection...")
        frame = self.screen.capture()
        collected = 0

        collector_templates = [
            "bubble_gold",
            "bubble_elixir",
            "bubble_dark",
            "bubble_gem",
            "cart_loot",
            "treasury_bubble"
        ]

        for tmpl in collector_templates:
            matches = self.matcher.find_all(frame, tmpl, threshold=0.75)
            for m in matches:
                logger.info(f"Collecting resource: {tmpl} at ({m.center_x}, {m.center_y})")
                self.device.tap(m.center_x, m.center_y, jitter=True, delay_range=(0.4, 0.7))
                collected += 1
                time.sleep(0.3)

        # If no specific templates matched, perform a gentle standard base sweep tap
        if collected == 0:
            # Tap typical center-base collector locations
            ref_points = [(750, 450), (950, 520), (1150, 480), (850, 650), (1050, 680)]
            for pt in ref_points:
                x, y = self.device.scale_coords(*pt)
                self.device.tap(x, y, jitter=True, delay_range=(0.2, 0.4))

        logger.info(f"Resource collection complete. Items collected: {collected}")
        return collected

    def request_clan_castle_troops(self):
        """Finds clan castle and requests reinforcement troops."""
        if not self.config.village.auto_request_troops:
            return

        frame = self.screen.capture()
        req_match = self.matcher.find_template(frame, "btn_request_troops", threshold=0.8)
        if req_match:
            logger.info("Requesting Clan Castle reinforcements...")
            self.device.tap(req_match.center_x, req_match.center_y)
            time.sleep(0.8)
            # Confirm request send button
            send_match = self.matcher.find_template(self.screen.capture(), "btn_send_request", threshold=0.8)
            if send_match:
                self.device.tap(send_match.center_x, send_match.center_y)
