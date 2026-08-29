import os

# Update config/settings.py default to ZAP_DRAGON_FARMING
settings_code = """\"\"\"
Configuration settings for Modern Clash of Clans AI & Macro Engine.
Default: Home Village Zap Dragon & Air Assault Farming.
\"\"\"

import os
import json
from dataclasses import dataclass, field, asdict
from typing import Tuple, Dict, Any, Optional


@dataclass
class DeviceConfig:
    adb_host: str = "127.0.0.1"
    adb_port: int = 5555
    device_serial: Optional[str] = None
    connection_type: str = "adb"
    ref_width: int = 1920
    ref_height: int = 1080


@dataclass
class AntiDetectionConfig:
    enable_jitter: bool = True
    jitter_std_dev: float = 3.5
    bezier_swipes: bool = True
    min_action_delay: float = 0.6
    max_action_delay: float = 1.4
    session_timeout_minutes: int = 180
    rest_break_minutes: int = 15


@dataclass
class FarmingCriteria:
    min_gold: int = 600_000
    min_elixir: int = 600_000
    min_dark_elixir: int = 4_000
    target_star_bonus_ores: bool = True
    max_search_attempts: int = 60


@dataclass
class VillageConfig:
    auto_collect_resources: bool = True
    auto_collect_ores: bool = True
    auto_train: bool = True
    army_slot: int = 1  # Dragon Army Quick-Train Slot #1
    auto_request_troops: bool = True


@dataclass
class BattleConfig:
    strategy: str = "ZAP_DRAGON_FARMING"  # ZAP_DRAGON_FARMING, ELECTRO_DRAGON_SPAM, DRAGON_RIDER_SMASH, SNEAKY_GOBLIN_ORE_FARM
    use_lightning_zap: bool = True
    use_hero_abilities: bool = True
    hero_ability_delay_sec: int = 12
    deployment_delay: float = 0.10


@dataclass
class AppConfig:
    device: DeviceConfig = field(default_factory=DeviceConfig)
    anti_detection: AntiDetectionConfig = field(default_factory=AntiDetectionConfig)
    farming: FarmingCriteria = field(default_factory=FarmingCriteria)
    village: VillageConfig = field(default_factory=VillageConfig)
    battle: BattleConfig = field(default_factory=BattleConfig)
    templates_dir: str = "vision/templates"
    ocr_backend: str = "easyocr"
    debug_mode: bool = True
    log_level: str = "INFO"

    def to_dict(self) -> Dict[str, Any]:
        return asdict(self)

    def save(self, filepath: str = "config.json"):
        with open(filepath, "w") as f:
            json.dump(self.to_dict(), f, indent=4)

    @classmethod
    def load(cls, filepath: str = "config.json") -> "AppConfig":
        if os.path.exists(filepath):
            try:
                with open(filepath, "r") as f:
                    data = json.load(f)
                    device = DeviceConfig(**data.get("device", {}))
                    anti_det = AntiDetectionConfig(**data.get("anti_detection", {}))
                    farming = FarmingCriteria(**data.get("farming", {}))
                    village = VillageConfig(**data.get("village", {}))
                    battle = BattleConfig(**data.get("battle", {}))
                    return cls(
                        device=device,
                        anti_detection=anti_det,
                        farming=farming,
                        village=village,
                        battle=battle,
                        templates_dir=data.get("templates_dir", "vision/templates"),
                        ocr_backend=data.get("ocr_backend", "easyocr"),
                        debug_mode=data.get("debug_mode", True),
                        log_level=data.get("log_level", "INFO")
                    )
            except Exception:
                pass
        config = cls()
        config.save(filepath)
        return config


# Default global instance
config = AppConfig.load()
"""

with open("config/settings.py", "w") as f:
    f.write(settings_code)

# Update ai/attack_strategies.py with premier Zap Dragon Attack
strategies_code = """\"\"\"
Tactical Battle AI: Dedicated Home Village Dragon Farming Engine.
1. Zap Air Defenses with Lightning Spells
2. Create corner funnels with King & Queen
3. Spread Dragons in wide horizontal line
4. Drop Balloons & Grand Warden behind Dragons
5. Trigger Hero Equipment & collect 100% Home Village loot
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
        \"\"\"
        The Ultimate Home Village Farming Attack:
        Easy, consistent 3-star loot wipeout.
        \"\"\"
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
"""

with open("ai/attack_strategies.py", "w") as f:
    f.write(strategies_code)

# Update main.py
main_code = """\"\"\"
AAA COC AI MARCO - Home Village Dragon Farming Engine.
Automated Dragon & Zap Air Assault with 0-Cost Quick Training & Star Bonus Ores.
\"\"\"

import os
import sys
import time
import argparse
import logging

try:
    from rich.console import Console
    from rich.table import Table
    from rich.logging import RichHandler
    HAS_RICH = True
    console = Console()
except ImportError:
    HAS_RICH = False
    console = None

from config.settings import AppConfig, config
from core.device import DeviceController
from core.screen import ScreenCapture
from vision.template_matcher import TemplateMatcher
from vision.ocr_reader import OCRReader
from ai.orchestrator import GameOrchestrator
from tools.calibrate import run_calibration


def setup_logger(debug: bool = False):
    level = logging.DEBUG if debug else logging.INFO
    if HAS_RICH:
        logging.basicConfig(
            level=level,
            format="%(message)s",
            datefmt="[%X]",
            handlers=[RichHandler(rich_tracebacks=True, console=console, markup=True)]
        )
    else:
        logging.basicConfig(
            level=level,
            format="%(asctime)s [%(levelname)s] %(message)s",
            datefmt="%H:%M:%S"
        )


def print_banner():
    banner_text = \"\"\"
=================================================================
             ⚔ AAA COC AI MARCO - DRAGON FARMING PRO ⚔          
      Home Village Auto-Farming | 0-Cost Zap Dragons | Ores      
=================================================================
\"\"\"
    if HAS_RICH:
        console.print(f"[bold red]{banner_text}[/bold red]")
    else:
        print(banner_text)


def display_dashboard(app_cfg: AppConfig):
    if HAS_RICH:
        table = Table(title="🐉 AAA COC AI MARCO - Home Village Farm", show_header=True, header_style="bold magenta")
        table.add_column("Setting", style="cyan", width=26)
        table.add_column("Value", style="green", width=34)

        table.add_row("Target Village", "🏰 Home Village Only")
        table.add_row("Attack Strategy", f"[bold red]{app_cfg.battle.strategy}[/bold red]")
        table.add_row("Min Gold Target", f"{app_cfg.farming.min_gold:,}")
        table.add_row("Min Elixir Target", f"{app_cfg.farming.min_elixir:,}")
        table.add_row("Min Dark Elixir", f"{app_cfg.farming.min_dark_elixir:,}")
        table.add_row("Air Defense Lightning Zap", f"{app_cfg.battle.use_lightning_zap}")
        table.add_row("Hero Equipment Triggers", f"{app_cfg.battle.use_hero_abilities}")
        table.add_row("Army Slot", f"Preset #{app_cfg.village.army_slot} (0-Cost Dragons)")

        console.print(table)
    else:
        print("\\n--- AAA COC AI MARCO CONFIGURATION ---")
        print(f"Village Target: Home Village Only")
        print(f"Attack Strategy: {app_cfg.battle.strategy}")
        print(f"Min Loot: Gold={app_cfg.farming.min_gold:,} | Elixir={app_cfg.farming.min_elixir:,} | Dark={app_cfg.farming.min_dark_elixir:,}")
        print(f"Air Defense Zap: {app_cfg.battle.use_lightning_zap}")
        print(f"Quick Train Slot: Preset #{app_cfg.village.army_slot} (0-Cost Dragons)")
        print("--------------------------------------\\n")


def main():
    parser = argparse.ArgumentParser(description="AAA COC AI MARCO - Home Village Dragon Farming Engine")
    parser.add_argument("--mode", choices=["bot", "calibrate"], default="bot", help="Execution mode")
    parser.add_argument(
        "--strategy",
        choices=["ZAP_DRAGON_FARMING", "ELECTRO_DRAGON_SPAM", "DRAGON_RIDER_SMASH", "SNEAKY_GOBLIN_ORE_FARM"],
        default="ZAP_DRAGON_FARMING",
        help="Dragon attack strategy"
    )
    parser.add_argument("--min-gold", type=int, help="Minimum gold threshold")
    parser.add_argument("--min-elixir", type=int, help="Minimum elixir threshold")
    parser.add_argument("--min-dark", type=int, help="Minimum dark elixir threshold")
    parser.add_argument("--slot", type=int, choices=[1, 2, 3], help="0-Cost Dragon Army preset slot")
    parser.add_argument("--cycles", type=int, default=None, help="Max raid cycles")
    parser.add_argument("--debug", action="store_true", help="Enable debug logging")

    args = parser.parse_args()
    setup_logger(debug=args.debug)

    app_cfg = AppConfig.load()
    if args.strategy:
        app_cfg.battle.strategy = args.strategy
    if args.min_gold is not None:
        app_cfg.farming.min_gold = args.min_gold
    if args.min_elixir is not None:
        app_cfg.farming.min_elixir = args.min_elixir
    if args.min_dark is not None:
        app_cfg.farming.min_dark_elixir = args.min_dark
    if args.slot is not None:
        app_cfg.village.army_slot = args.slot

    print_banner()

    if args.mode == "calibrate":
        run_calibration()
        return

    display_dashboard(app_cfg)

    print("[*] Launching Home Village Dragon Farming AI Engine (Ctrl+C to stop)...")
    orchestrator = GameOrchestrator(config=app_cfg)
    orchestrator.start(max_cycles=args.cycles)


if __name__ == "__main__":
    main()
"""

with open("main.py", "w") as f:
    f.write(main_code)

# Update orchestrator to dispatch dragon strategy
orch_code = """\"\"\"
Main Autonomous Game Orchestrator: Home Village Dragon Farming.
\"\"\"

import time
import logging
from typing import Optional
from core.device import DeviceController
from core.screen import ScreenCapture
from core.fsm import StateMachine, BotState
from vision.template_matcher import TemplateMatcher
from vision.ocr_reader import OCRReader
from vision.redline_detector import RedlineDetector
from ai.collector_bot import CollectorBot
from ai.army_bot import ArmyBot
from ai.matchmaker_bot import MatchmakerBot
from ai.attack_strategies import BattleAI
from config.settings import AppConfig

logger = logging.getLogger(__name__)


class GameOrchestrator:
    def __init__(self, config: Optional[AppConfig] = None):
        self.config = config or AppConfig.load()
        self.device = DeviceController(
            serial=self.config.device.device_serial,
            adb_host=self.config.device.adb_host,
            adb_port=self.config.device.adb_port,
            ref_resolution=(self.config.device.ref_width, self.config.device.ref_height),
            enable_jitter=self.config.anti_detection.enable_jitter,
            jitter_std=self.config.anti_detection.jitter_std_dev
        )
        self.screen = ScreenCapture(device_serial=self.device.serial)
        self.matcher = TemplateMatcher(templates_dir=self.config.templates_dir)
        self.ocr = OCRReader(backend=self.config.ocr_backend)
        self.redline = RedlineDetector()

        self.collector = CollectorBot(self.device, self.screen, self.matcher, self.config)
        self.army = ArmyBot(self.device, self.screen, self.matcher, self.config)
        self.matchmaker = MatchmakerBot(self.device, self.screen, self.matcher, self.ocr, self.config)
        self.battle = BattleAI(self.device, self.screen, self.matcher, self.redline, self.ocr, self.config)

        self.fsm = StateMachine(initial_state=BotState.VILLAGE_COLLECT)
        self._register_fsm_handlers()
        self.is_running = False

    def _register_fsm_handlers(self):
        self.fsm.register_handler(BotState.VILLAGE_COLLECT, self._handle_village_collect)
        self.fsm.register_handler(BotState.ARMY_TRAIN, self._handle_army_train)
        self.fsm.register_handler(BotState.CHECK_ARMY_READY, self._handle_check_army)
        self.fsm.register_handler(BotState.SEARCH_MATCH, self._handle_search_match)
        self.fsm.register_handler(BotState.EVALUATE_BASE, self._handle_evaluate_base)
        self.fsm.register_handler(BotState.BATTLE_DEPLOY, self._handle_battle_deploy)
        self.fsm.register_handler(BotState.RETURN_HOME, self._handle_return_home)
        self.fsm.register_handler(BotState.REST_BREAK, self._handle_rest_break)
        self.fsm.register_handler(BotState.ERROR_RECOVERY, self._handle_error_recovery)

    def _handle_village_collect(self) -> BotState:
        logger.info("=== [STATE] Home Village Collection & Daily Ores ===")
        if self.config.village.auto_collect_resources:
            self.collector.collect_all_resources()
            self.collector.request_clan_castle_troops()
        return BotState.ARMY_TRAIN

    def _handle_army_train(self) -> BotState:
        logger.info("=== [STATE] 0-Cost Quick Training Dragon Army ===")
        if self.config.village.auto_train:
            self.army.train_preset_army()
        return BotState.CHECK_ARMY_READY

    def _handle_check_army(self) -> BotState:
        logger.info("=== [STATE] Verifying Dragon Army Readiness ===")
        if self.army.is_army_ready():
            return BotState.SEARCH_MATCH
        else:
            logger.info("Dragon army queue in progress. Waiting 45s...")
            time.sleep(45.0)
            return BotState.VILLAGE_COLLECT

    def _handle_search_match(self) -> BotState:
        logger.info("=== [STATE] Starting Multiplayer Raid Matchmaking ===")
        self.matchmaker.start_matchmaking()
        if self.matchmaker.wait_for_battle_screen():
            return BotState.EVALUATE_BASE
        return BotState.ERROR_RECOVERY

    def _handle_evaluate_base(self) -> BotState:
        logger.info("=== [STATE] Evaluating Opponent Base Loot ===")
        meets_criteria, loot = self.matchmaker.evaluate_current_base()

        if meets_criteria:
            logger.info(f"Target base accepted! Available Loot: {loot.total_standard_loot:,}")
            return BotState.BATTLE_DEPLOY

        if self.matchmaker.search_count >= self.config.farming.max_search_attempts:
            logger.warning("Max search limit reached. Returning home.")
            return BotState.RETURN_HOME

        logger.info("Loot below threshold. Tapping Next...")
        if self.matchmaker.next_base():
            return BotState.EVALUATE_BASE
        return BotState.ERROR_RECOVERY

    def _handle_battle_deploy(self) -> BotState:
        logger.info(f"=== [STATE] Deploying Dragon Attack: {self.config.battle.strategy} ===")
        strat = self.config.battle.strategy.upper()

        if strat == "ZAP_DRAGON_FARMING":
            self.battle.execute_zap_dragon_farming()
        elif strat == "ELECTRO_DRAGON_SPAM":
            self.battle.execute_electro_dragon_spam()
        elif strat == "DRAGON_RIDER_SMASH":
            self.battle.execute_dragon_rider_smash()
        elif strat == "SNEAKY_GOBLIN_ORE_FARM":
            self.battle.execute_sneaky_goblin_ore_farm()
        else:
            self.battle.execute_zap_dragon_farming()

        self.fsm.context["raids_completed"] += 1
        return BotState.RETURN_HOME

    def _handle_return_home(self) -> BotState:
        logger.info("=== [STATE] Returning to Home Village ===")
        time.sleep(2.5)
        elapsed_min = (time.time() - self.fsm.context["session_start_time"]) / 60.0
        if elapsed_min >= self.config.anti_detection.session_timeout_minutes:
            return BotState.REST_BREAK
        return BotState.VILLAGE_COLLECT

    def _handle_rest_break(self) -> BotState:
        logger.info(f"=== [STATE] Anti-Ban Rest Break ({self.config.anti_detection.rest_break_minutes} mins) ===")
        self.device.pinch_zoom(zoom_in=False)
        time.sleep(self.config.anti_detection.rest_break_minutes * 60)
        self.fsm.context["session_start_time"] = time.time()
        return BotState.VILLAGE_COLLECT

    def _handle_error_recovery(self) -> BotState:
        logger.warning("=== [STATE] Error Recovery ===")
        for _ in range(3):
            self.device.press_back()
            time.sleep(0.4)
        cx, cy = self.device.scale_coords(960, 540)
        self.device.tap(cx, cy)
        time.sleep(1.5)
        return BotState.VILLAGE_COLLECT

    def start(self, max_cycles: Optional[int] = None):
        self.is_running = True
        cycles = 0
        logger.info("Starting AAA COC AI MARCO Dragon Farming Engine...")
        try:
            while self.is_running:
                self.fsm.step()
                cycles += 1
                if max_cycles and cycles >= max_cycles:
                    break
        except KeyboardInterrupt:
            logger.info("Bot stopped by user.")
        finally:
            self.is_running = False
"""

with open("ai/orchestrator.py", "w") as f:
    f.write(orch_code)

print("Updated Python engine for Home Village Dragon Farming.")
