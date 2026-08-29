"""
Main Autonomous Game Orchestrator: Home Village Dragon Farming.
"""

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
