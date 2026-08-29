import os

# 1. Update ai/orchestrator.py
orch_content = """\"\"\"
Main Autonomous Game Orchestrator.
Coordinates Village (Ores/Mines/Apprentice) ↔ 0-Cost Training ↔ Matchmaker ↔ Modern Meta Battle AI.
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
    \"\"\"
    Autonomous bot coordinator managing modern Clash of Clans gameplay.
    \"\"\"
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

        # Subsystems
        self.collector = CollectorBot(self.device, self.screen, self.matcher, self.config)
        self.army = ArmyBot(self.device, self.screen, self.matcher, self.config)
        self.matchmaker = MatchmakerBot(self.device, self.screen, self.matcher, self.ocr, self.config)
        self.battle = BattleAI(self.device, self.screen, self.matcher, self.redline, self.ocr, self.config)

        # FSM Setup
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
        logger.info("=== [STATE] Village Collection & Daily Ores ===")
        if self.config.village.auto_collect_resources:
            self.collector.collect_all_resources()
            self.collector.request_clan_castle_troops()
        return BotState.ARMY_TRAIN

    def _handle_army_train(self) -> BotState:
        logger.info("=== [STATE] 0-Cost Quick Training ===")
        if self.config.village.auto_train:
            self.army.train_preset_army()
        return BotState.CHECK_ARMY_READY

    def _handle_check_army(self) -> BotState:
        logger.info("=== [STATE] Checking Army Readiness ===")
        if self.config.battle.strategy == "BUILDER_BASE_FAST_FARM":
            return BotState.BATTLE_DEPLOY

        if self.army.is_army_ready():
            return BotState.SEARCH_MATCH
        else:
            logger.info("Army queue in progress. Waiting 45s...")
            time.sleep(45.0)
            return BotState.VILLAGE_COLLECT

    def _handle_search_match(self) -> BotState:
        logger.info("=== [STATE] Starting Raid Matchmaking ===")
        self.matchmaker.start_matchmaking()
        if self.matchmaker.wait_for_battle_screen():
            return BotState.EVALUATE_BASE
        return BotState.ERROR_RECOVERY

    def _handle_evaluate_base(self) -> BotState:
        logger.info("=== [STATE] Evaluating Opponent Base ===")
        meets_criteria, loot = self.matchmaker.evaluate_current_base()

        if meets_criteria:
            logger.info(f"Target base accepted! Loot: {loot.total_standard_loot:,}")
            return BotState.BATTLE_DEPLOY

        if self.matchmaker.search_count >= self.config.farming.max_search_attempts:
            logger.warning("Max search limit reached. Returning home.")
            return BotState.RETURN_HOME

        logger.info("Loot below threshold. Skipping...")
        if self.matchmaker.next_base():
            return BotState.EVALUATE_BASE
        return BotState.ERROR_RECOVERY

    def _handle_battle_deploy(self) -> BotState:
        logger.info(f"=== [STATE] Deploying Battle AI: {self.config.battle.strategy} ===")
        strat = self.config.battle.strategy.upper()

        if strat == "OVERGROWTH_ROOT_RIDER":
            self.battle.execute_overgrowth_root_rider()
        elif strat == "TH17_HERO_SMASH":
            self.battle.execute_th17_hero_smash()
        elif strat == "SNEAKY_GOBLIN_ORE_FARM" or strat == "SNEAKY_GOBLIN":
            self.battle.execute_sneaky_goblin_ore_farm()
        elif strat == "BUILDER_BASE_FAST_FARM":
            self.battle.execute_builder_base_fast_farm()
        elif strat == "BARCH":
            self.battle.execute_barch_attack()
        else:
            self.battle.execute_overgrowth_root_rider()

        self.fsm.context["raids_completed"] += 1
        return BotState.RETURN_HOME

    def _handle_return_home(self) -> BotState:
        logger.info("=== [STATE] Returning to Village ===")
        time.sleep(2.5)
        elapsed_min = (time.time() - self.fsm.context["session_start_time"]) / 60.0
        if elapsed_min >= self.config.anti_detection.session_timeout_minutes:
            return BotState.REST_BREAK
        return BotState.VILLAGE_COLLECT

    def _handle_rest_break(self) -> BotState:
        logger.info(f"=== [STATE] Rest Break ({self.config.anti_detection.rest_break_minutes} mins) ===")
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
        logger.info("Starting Clash of Clans Modern AI Engine...")
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
    f.write(orch_content)

# 2. Update main.py with modern strategies
main_content = """\"\"\"
Modern Clash of Clans AI & Macro Automation Engine - Master CLI & Dashboard.
Supports TH16/TH17, Hero Hall, Equipment Ores, Overgrowth Spell, Builder Base 2.0.
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
from macro.builder import MacroFlow
from macro.node import MacroContext
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
       CLASH OF CLANS MODERN AI & MACRO ENGINE (TH16/TH17)       
  Ores & Star Bonus | Hero Equipment | Overgrowth Spell | BB 2.0 
=================================================================
\"\"\"
    if HAS_RICH:
        console.print(f"[bold cyan]{banner_text}[/bold cyan]")
    else:
        print(banner_text)


def display_dashboard(app_cfg: AppConfig):
    if HAS_RICH:
        table = Table(title="Active Modern Bot Configuration", show_header=True, header_style="bold magenta")
        table.add_column("Setting", style="cyan", width=26)
        table.add_column("Value", style="green", width=34)

        table.add_row("Device Target", f"{app_cfg.device.adb_host}:{app_cfg.device.adb_port}")
        table.add_row("Attack Strategy", f"[bold]{app_cfg.battle.strategy}[/bold]")
        table.add_row("Min Gold Target", f"{app_cfg.farming.min_gold:,}")
        table.add_row("Min Elixir Target", f"{app_cfg.farming.min_elixir:,}")
        table.add_row("Min Dark Elixir", f"{app_cfg.farming.min_dark_elixir:,}")
        table.add_row("Daily Star Bonus Ores", f"{app_cfg.farming.target_star_bonus_ores}")
        table.add_row("Overgrowth Spell AI", f"{app_cfg.battle.use_overgrowth_spell}")
        table.add_row("Hero Equipment Combos", f"{app_cfg.battle.use_hero_equipment_abilities}")
        table.add_row("Apprentice Builder Boost", f"{app_cfg.village.auto_assign_apprentice_builder}")
        table.add_row("Quick Train Slot", f"Preset #{app_cfg.village.army_slot} (0-Cost)")

        console.print(table)
    else:
        print("\\n--- ACTIVE MODERN CONFIGURATION ---")
        print(f"Target Host: {app_cfg.device.adb_host}:{app_cfg.device.adb_port}")
        print(f"Attack Strategy: {app_cfg.battle.strategy}")
        print(f"Min Loot: Gold={app_cfg.farming.min_gold:,} | Elixir={app_cfg.farming.min_elixir:,} | Dark={app_cfg.farming.min_dark_elixir:,}")
        print(f"Daily Star Bonus Ores: {app_cfg.farming.target_star_bonus_ores}")
        print(f"Overgrowth Spell AI: {app_cfg.battle.use_overgrowth_spell}")
        print(f"Hero Equipment Combos: {app_cfg.battle.use_hero_equipment_abilities}")
        print(f"Quick Train Slot: Preset #{app_cfg.village.army_slot} (0-Cost)")
        print("-----------------------------------\\n")


def main():
    parser = argparse.ArgumentParser(description="Modern Clash of Clans AI & Macro Engine (TH16/TH17 Ready)")
    parser.add_argument(
        "--mode",
        choices=["bot", "macro", "calibrate"],
        default="bot",
        help="Execution mode"
    )
    parser.add_argument(
        "--strategy",
        choices=[
            "OVERGROWTH_ROOT_RIDER",
            "SNEAKY_GOBLIN_ORE_FARM",
            "TH17_HERO_SMASH",
            "BUILDER_BASE_FAST_FARM",
            "BARCH"
        ],
        help="Attack strategy"
    )
    parser.add_argument("--min-gold", type=int, help="Minimum gold threshold")
    parser.add_argument("--min-elixir", type=int, help="Minimum elixir threshold")
    parser.add_argument("--min-dark", type=int, help="Minimum dark elixir threshold")
    parser.add_argument("--slot", type=int, choices=[1, 2, 3], help="0-Cost Quick Train slot")
    parser.add_argument("--cycles", type=int, default=None, help="Max cycles to run")
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

    if args.mode == "bot":
        print("[*] Starting Modern Clash of Clans AI Engine (Ctrl+C to stop)...")
        orchestrator = GameOrchestrator(config=app_cfg)
        orchestrator.start(max_cycles=args.cycles)


if __name__ == "__main__":
    main()
"""

with open("main.py", "w") as f:
    f.write(main_content)

print("Updated orchestrator and main.py successfully.")
