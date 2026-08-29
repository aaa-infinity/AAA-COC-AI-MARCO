"""
Tactical Battle AI: Dedicated Home Village Dragon Farming Engine.
1. Zap Air Defenses with Lightning Spells
2. Create corner funnels with King & Queen
3. Spread Dragons in wide horizontal line
4. Drop Balloons & Grand Warden behind Dragons
5. Trigger Hero Equipment & collect 100% Home Village loot
"""

import time
import random
import logging
from typing import List, Tuple, Dict, Any
from core.device import DeviceController
from core.screen import ScreenCapture
from vision.template_matcher import TemplateMatcher
from vision.redline_detector import RedlineDetector
from vision.ocr_reader import OCRReader, LootInfo
from config.settings import AppConfig

logger = logging.getLogger(__name__)


class BattleAI:
    def __init__(
        self,
        device: DeviceController,
        screen: ScreenCapture,
        matcher: TemplateMatcher,
        redline: RedlineDetector,
        ocr: OCRReader,
        config: AppConfig
    ):
        self.device = device
        self.screen = screen
        self.matcher = matcher
        self.redline = redline
        self.ocr = ocr
        self.config = config

    def select_slot(self, slot_index: int):
        slot_x_start = 180
        slot_step = 105
        slot_y = 980
        ref_x = slot_x_start + (slot_index - 1) * slot_step
        x, y = self.device.scale_coords(ref_x, slot_y)
        self.device.tap(x, y, jitter=True, delay_range=(0.12, 0.20))

    def deploy_troops_at_points(
        self,
        points: List[Tuple[int, int]],
        drops_per_point: int = 2,
        delay: float = 0.10
    ):
        for pt in points:
            for _ in range(drops_per_point):
                self.device.tap(pt[0], pt[1], jitter=True, delay_range=(delay, delay + 0.04))

    def execute_zap_dragon_farming(self) -> Dict[str, Any]:
        """
        The Ultimate Home Village Farming Attack:
        Easy, consistent 3-star loot wipeout.
        """
        logger.info("🐉 [DRAGON AI] Step 1: Destroying Air Defenses with Lightning Spells...")
        frame = self.screen.capture()
        h, w = frame.shape[:2] if hasattr(frame, 'shape') else (1080, 1920)

        # Step 1: Zap top Air Defenses (Slot 5: Lightning Spells)
        if self.config.battle.use_lightning_zap:
            self.select_slot(5)
            ad_spots = [(750, 480), (1170, 480)]
            for spot in ad_spots:
                x, y = self.device.scale_coords(*spot)
                for _ in range(3):
                    self.device.tap(x, y, jitter=True, delay_range=(0.15, 0.25))

        time.sleep(0.8)

        # Step 2: Funnel Corner Heroes
        logger.info("🐉 [DRAGON AI] Step 2: Setting up corner funnels with King & Queen...")
        self.select_slot(3)  # King on left corner
        k_x, k_y = self.device.scale_coords(450, 850)
        self.device.tap(k_x, k_y)

        self.select_slot(4)  # Queen on right corner
        q_x, q_y = self.device.scale_coords(1450, 850)
        self.device.tap(q_x, q_y)

        time.sleep(1.0)

        # Step 3: Spread Dragons in a wide line
        logger.info("🐉 [DRAGON AI] Step 3: Line deployment of Dragons across perimeter...")
        dragon_line = self.redline.get_perimeter_deployment_points((h, w), side="BOTTOM_LEFT", points_per_side=6)
        self.select_slot(1)  # Dragons
        self.deploy_troops_at_points(dragon_line, drops_per_point=2, delay=0.10)

        # Step 4: Drop Balloons & Grand Warden behind Dragons
        logger.info("🐉 [DRAGON AI] Step 4: Deploying Balloons & Grand Warden...")
        self.select_slot(2)  # Balloons
        self.deploy_troops_at_points(dragon_line, drops_per_point=2, delay=0.08)

        self.select_slot(5)  # Grand Warden
        w_x, w_y = self.device.scale_coords(960, 850)
        self.device.tap(w_x, w_y)

        # Step 5: Cast Rage Spell into core (Slot 6)
        time.sleep(6.0)
        logger.info("🐉 [DRAGON AI] Step 5: Casting Rage Spell into Town Hall core...")
        self.select_slot(6)
        core_x, core_y = self.device.scale_coords(960, 540)
        self.device.tap(core_x, core_y)

        # Step 6: Trigger Hero Equipment abilities & monitor
        self.monitor_battle_and_exit(max_duration_sec=60)
        return {"status": "COMPLETED", "strategy": "ZAP_DRAGON_FARMING"}

    def execute_electro_dragon_spam(self) -> Dict[str, Any]:
        logger.info("⚡ [E-DRAG AI] Deploying Electro Dragons & Chain Lightning...")
        frame = self.screen.capture()
        h, w = frame.shape[:2] if hasattr(frame, 'shape') else (1080, 1920)
        line = self.redline.get_perimeter_deployment_points((h, w), side="BOTTOM_LEFT", points_per_side=4)

        self.select_slot(1)  # Electro Dragons
        self.deploy_troops_at_points(line, drops_per_point=2, delay=0.15)
        self._deploy_all_heroes(line[1])
        self.monitor_battle_and_exit(max_duration_sec=60)
        return {"status": "COMPLETED", "strategy": "ELECTRO_DRAGON_SPAM"}

    def execute_dragon_rider_smash(self) -> Dict[str, Any]:
        logger.info("🐉 [DRAGON RIDER AI] Deploying Dragons + Dragon Riders...")
        frame = self.screen.capture()
        h, w = frame.shape[:2] if hasattr(frame, 'shape') else (1080, 1920)
        line = self.redline.get_perimeter_deployment_points((h, w), side="BOTTOM_LEFT", points_per_side=5)

        self.select_slot(1)  # Dragons
        self.deploy_troops_at_points(line, drops_per_point=2, delay=0.10)
        self.select_slot(2)  # Dragon Riders
        self.deploy_troops_at_points(line, drops_per_point=2, delay=0.10)
        self._deploy_all_heroes(line[2])
        self.monitor_battle_and_exit(max_duration_sec=60)
        return {"status": "COMPLETED", "strategy": "DRAGON_RIDER_SMASH"}

    def execute_sneaky_goblin_ore_farm(self) -> Dict[str, Any]:
        frame = self.screen.capture()
        h, w = frame.shape[:2] if hasattr(frame, 'shape') else (1080, 1920)
        perimeter = self.redline.get_perimeter_deployment_points((h, w), side="ALL", points_per_side=4)
        self.select_slot(1)
        self.deploy_troops_at_points(perimeter, drops_per_point=2, delay=0.12)
        self.monitor_battle_and_exit(max_duration_sec=35)
        return {"status": "COMPLETED", "strategy": "SNEAKY_GOBLIN_ORE_FARM"}

    def _deploy_all_heroes(self, drop_coord: Tuple[int, int]):
        for slot in [3, 4, 5, 6]:
            self.select_slot(slot)
            self.device.tap(drop_coord[0], drop_coord[1], jitter=True, delay_range=(0.1, 0.2))

    def monitor_battle_and_exit(self, max_duration_sec: int = 50):
        start_time = time.time()
        ability_triggered = False

        while time.time() - start_time < max_duration_sec:
            elapsed = time.time() - start_time

            if not ability_triggered and elapsed >= self.config.battle.hero_ability_delay_sec:
                logger.info("🐉 [HERO AI] Triggering Hero Equipment & Warden Eternal Tome...")
                for slot in [3, 4, 5, 6]:
                    self.select_slot(slot)
                ability_triggered = True

            frame = self.screen.capture()
            return_home_btn = self.matcher.find_template(frame, "btn_return_home", threshold=0.75)
            if return_home_btn:
                self.device.tap(return_home_btn.center_x, return_home_btn.center_y)
                time.sleep(2.0)
                return

            time.sleep(2.0)

        self._surrender_and_return()

    def _surrender_and_return(self):
        logger.info("🐉 [DRAGON AI] Harvest finished. Returning to Home Village...")
        frame = self.screen.capture()
        end_btn = self.matcher.find_template(frame, "btn_end_battle", threshold=0.75)
        if end_btn:
            self.device.tap(end_btn.center_x, end_btn.center_y)
        else:
            x, y = self.device.scale_coords(120, 880)
            self.device.tap(x, y)
        time.sleep(0.8)

        frame = self.screen.capture()
        ok_btn = self.matcher.find_template(frame, "btn_confirm_ok", threshold=0.75)
        if ok_btn:
            self.device.tap(ok_btn.center_x, ok_btn.center_y)
        else:
            x, y = self.device.scale_coords(1100, 680)
            self.device.tap(x, y)
        time.sleep(1.8)

        frame = self.screen.capture()
        home_btn = self.matcher.find_template(frame, "btn_return_home", threshold=0.75)
        if home_btn:
            self.device.tap(home_btn.center_x, home_btn.center_y)
        else:
            x, y = self.device.scale_coords(960, 920)
            self.device.tap(x, y)
        time.sleep(2.5)
