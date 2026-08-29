import os

attack_strategies_code = """\"\"\"
Tactical Battle AI & Modern Meta Deployment Strategies.
Implements:
- Overgrowth Spell + Druid + Root Rider Smash (TH16/TH17 Meta)
- Sneaky Goblin Star Bonus Ore Farming
- TH17 Hero Hall 5th Hero Minion Prince Smash
- Builder Base 2.0 Fast Farming Loop
- BARCH Waves
\"\"\"

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
    \"\"\"
    Modern Clash of Clans Tactical Battle AI.
    \"\"\"
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

    def execute_overgrowth_root_rider(self) -> Dict[str, Any]:
        \"\"\"
        Modern TH16/TH17 Meta:
        1. Cast Overgrowth Spell to freeze flank defenses (Monolith / Inferno Artillery)
        2. Funnel Root Riders in line to break walls
        3. Druids behind Root Riders for ranged heal + bear transformation
        4. Hero Hall 5 Heroes + Equipment combo activation
        \"\"\"
        logger.info("Executing OVERGROWTH ROOT RIDER (TH16/TH17 Meta) Attack...")
        frame = self.screen.capture()
        h, w = frame.shape[:2] if hasattr(frame, 'shape') else (1080, 1920)

        # 1. Cast Overgrowth Spell on flank defense sector
        if self.config.battle.use_overgrowth_spell:
            logger.info("Casting Overgrowth Spell on defensive flank...")
            self.select_slot(7)  # Overgrowth Spell
            og_x, og_y = self.device.scale_coords(1250, 420)
            self.device.tap(og_x, og_y)
            time.sleep(0.8)

        # 2. Line deployment of Root Riders (Slot 1)
        entry_points = self.redline.get_perimeter_deployment_points((h, w), side="BOTTOM_LEFT", points_per_side=4)
        logger.info("Deploying Root Riders to smash base compartments...")
        self.select_slot(1)
        self.deploy_troops_at_points(entry_points, drops_per_point=2, delay=0.10)

        time.sleep(1.0)

        # 3. Deploy Druids (Slot 2) behind Root Riders
        logger.info("Deploying Druids for healing & bear tanking...")
        self.select_slot(2)
        self.deploy_troops_at_points(entry_points, drops_per_point=2, delay=0.10)

        # 4. Deploy Heroes (King, Queen, Warden, Champion, Minion Prince)
        self._deploy_all_heroes(entry_points[1] if len(entry_points) > 1 else (w // 2, h - 200))

        # 5. Monitor and trigger Hero Equipment (Giant Gauntlet, Magic Mirror, Fireball)
        self.monitor_battle_and_exit(max_duration_sec=55)
        return {"status": "COMPLETED", "strategy": "OVERGROWTH_ROOT_RIDER"}

    def execute_th17_hero_smash(self) -> Dict[str, Any]:
        \"\"\"
        Town Hall 17 Hero Hall Smash with Minion Prince flying hero.
        \"\"\"
        logger.info("Executing TH17 HERO HALL SMASH Attack...")
        frame = self.screen.capture()
        h, w = frame.shape[:2] if hasattr(frame, 'shape') else (1080, 1920)
        entry_points = self.redline.get_perimeter_deployment_points((h, w), side="BOTTOM_RIGHT", points_per_side=3)

        # Deploy Siege Machine
        self.select_slot(6)
        self.device.tap(entry_points[0][0], entry_points[0][1])
        time.sleep(0.5)

        # Deploy 5 Heroes
        self._deploy_all_heroes(entry_points[1])

        self.monitor_battle_and_exit(max_duration_sec=60)
        return {"status": "COMPLETED", "strategy": "TH17_HERO_SMASH"}

    def execute_sneaky_goblin_ore_farm(self) -> Dict[str, Any]:
        \"\"\"
        Sneaky Goblin Farming tailored for Star Bonus Ores & resource harvesting.
        \"\"\"
        logger.info("Executing SNEAKY GOBLIN ORE FARMING...")
        frame = self.screen.capture()
        h, w = frame.shape[:2] if hasattr(frame, 'shape') else (1080, 1920)
        perimeter_points = self.redline.get_perimeter_deployment_points((h, w), side="ALL", points_per_side=4)

        # Harvest outside collectors
        self.select_slot(1)  # Sneaky Goblins
        self.deploy_troops_at_points(perimeter_points, drops_per_point=2, delay=self.config.battle.deployment_delay)

        time.sleep(3.0)

        # Core Jump & Invisibility to secure Town Hall for Star Bonus Ores
        self.select_slot(5)  # Jump Spell
        core_x, core_y = self.device.scale_coords(960, 540)
        self.device.tap(core_x, core_y)
        time.sleep(0.4)

        # Core Goblins to snipe Town Hall
        self.select_slot(1)
        self.device.tap(core_x, core_y + 80)
        self.device.tap(core_x, core_y + 80)
        self.device.tap(core_x, core_y + 80)

        self._deploy_all_heroes(perimeter_points[0])
        self.monitor_battle_and_exit(max_duration_sec=35)
        return {"status": "COMPLETED", "strategy": "SNEAKY_GOBLIN_ORE_FARM"}

    def execute_builder_base_fast_farm(self) -> Dict[str, Any]:
        \"\"\"
        Builder Base 2.0 Ultra-Fast Farming loop.
        \"\"\"
        logger.info("Executing Builder Base 2.0 Fast Farming...")
        frame = self.screen.capture()
        h, w = frame.shape[:2] if hasattr(frame, 'shape') else (1080, 1920)
        spots = [(w // 3, h // 3), (2 * w // 3, h // 3), (w // 2, 2 * h // 3)]

        self.select_slot(1)
        self.deploy_troops_at_points(spots, drops_per_point=3, delay=0.08)
        self.select_slot(2)
        self.deploy_troops_at_points(spots, drops_per_point=3, delay=0.08)

        time.sleep(2.0)
        self._surrender_and_return()
        return {"status": "COMPLETED", "strategy": "BUILDER_BASE_FAST_FARM"}

    def execute_barch_attack(self) -> Dict[str, Any]:
        frame = self.screen.capture()
        h, w = frame.shape[:2] if hasattr(frame, 'shape') else (1080, 1920)
        perimeter = self.redline.get_perimeter_deployment_points((h, w), side="ALL", points_per_side=5)

        self.select_slot(1)  # Barbarians
        self.deploy_troops_at_points(perimeter, drops_per_point=3, delay=0.10)
        self.select_slot(2)  # Archers
        self.deploy_troops_at_points(perimeter, drops_per_point=4, delay=0.10)

        self._deploy_all_heroes(perimeter[0])
        self.monitor_battle_and_exit(max_duration_sec=50)
        return {"status": "COMPLETED", "strategy": "BARCH"}

    def _deploy_all_heroes(self, drop_coord: Tuple[int, int]):
        logger.info("Deploying Hero Squad (King, Queen, Warden, Champion, Minion Prince)...")
        for slot in [3, 4, 5, 6, 7]:
            self.select_slot(slot)
            self.device.tap(drop_coord[0], drop_coord[1], jitter=True, delay_range=(0.1, 0.2))

    def monitor_battle_and_exit(self, max_duration_sec: int = 45):
        start_time = time.time()
        equipment_triggered = False

        while time.time() - start_time < max_duration_sec:
            elapsed = time.time() - start_time

            if not equipment_triggered and elapsed >= self.config.battle.hero_ability_delay_sec:
                logger.info("Activating Hero Equipment combos (Gauntlet, Mirror, Fireball)...")
                for slot in [3, 4, 5, 6, 7]:
                    self.select_slot(slot)
                equipment_triggered = True

            frame = self.screen.capture()
            return_home_btn = self.matcher.find_template(frame, "btn_return_home", threshold=0.75)
            if return_home_btn:
                self.device.tap(return_home_btn.center_x, return_home_btn.center_y)
                time.sleep(2.0)
                return

            time.sleep(2.0)

        self._surrender_and_return()

    def _surrender_and_return(self):
        logger.info("Ending battle to return home...")
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
"""

with open("ai/attack_strategies.py", "w") as f:
    f.write(attack_strategies_code)

print("Updated ai/attack_strategies.py")
