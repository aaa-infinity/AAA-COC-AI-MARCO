"""
Finite State Machine (FSM) for Autonomous Game Automation.
Manages state transitions, callbacks, and cycle tracking.
"""

import enum
import time
import logging
from typing import Callable, Dict, Optional, Any

logger = logging.getLogger(__name__)


class BotState(str, enum.Enum):
    IDLE = "IDLE"
    VILLAGE_COLLECT = "VILLAGE_COLLECT"
    VILLAGE_MAINTENANCE = "VILLAGE_MAINTENANCE"
    ARMY_TRAIN = "ARMY_TRAIN"
    CHECK_ARMY_READY = "CHECK_ARMY_READY"
    SEARCH_MATCH = "SEARCH_MATCH"
    EVALUATE_BASE = "EVALUATE_BASE"
    NEXT_BASE = "NEXT_BASE"
    BATTLE_DEPLOY = "BATTLE_DEPLOY"
    BATTLE_CLEANUP = "BATTLE_CLEANUP"
    RETURN_HOME = "RETURN_HOME"
    REST_BREAK = "REST_BREAK"
    ERROR_RECOVERY = "ERROR_RECOVERY"
    STOPPED = "STOPPED"


class StateMachine:
    """
    Manages state execution, history, transitions, and error handling.
    """
    def __init__(self, initial_state: BotState = BotState.IDLE):
        self.current_state: BotState = initial_state
        self.previous_state: Optional[BotState] = None
        self.handlers: Dict[BotState, Callable[[], Optional[BotState]]] = {}
        self.state_start_time: float = time.time()
        self.context: Dict[str, Any] = {
            "raids_completed": 0,
            "total_gold_farmed": 0,
            "total_elixir_farmed": 0,
            "total_dark_farmed": 0,
            "search_attempts": 0,
            "errors_count": 0,
            "session_start_time": time.time()
        }
        self.is_running: bool = False

    def register_handler(self, state: BotState, handler: Callable[[], Optional[BotState]]):
        """Registers a function to handle a given state."""
        self.handlers[state] = handler

    def transition_to(self, new_state: BotState):
        """Transitions to a new state."""
        logger.info(f"[FSM] Transition: {self.current_state.value} -> {new_state.value}")
        self.previous_state = self.current_state
        self.current_state = new_state
        self.state_start_time = time.time()

    def step(self) -> BotState:
        """Executes a single step for the current state."""
        handler = self.handlers.get(self.current_state)
        if not handler:
            logger.warning(f"[FSM] No handler registered for state {self.current_state.value}")
            time.sleep(1.0)
            return self.current_state

        try:
            next_state = handler()
            if next_state is not None and next_state != self.current_state:
                self.transition_to(next_state)
        except Exception as e:
            logger.error(f"[FSM] Error executing handler for {self.current_state.value}: {e}", exc_info=True)
            self.context["errors_count"] += 1
            self.transition_to(BotState.ERROR_RECOVERY)

        return self.current_state

    def get_stats(self) -> Dict[str, Any]:
        """Returns summary statistics of the bot's session."""
        elapsed_sec = int(time.time() - self.context["session_start_time"])
        hours, rem = divmod(elapsed_sec, 3600)
        minutes, seconds = divmod(rem, 60)
        return {
            **self.context,
            "current_state": self.current_state.value,
            "uptime": f"{hours:02d}:{minutes:02d}:{seconds:02d}"
        }
