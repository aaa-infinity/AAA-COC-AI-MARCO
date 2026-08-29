"""
Macrorify-style Macro Execution Graph.
Provides modular, condition-based nodes (Image Find, Click, Swipe, OCR, Loop, Branch).
"""

import time
import random
import logging
from typing import Dict, Any, Optional, Callable, List, Tuple

from core.device import DeviceController
from core.screen import ScreenCapture
from vision.template_matcher import TemplateMatcher, MatchResult
from vision.ocr_reader import OCRReader

logger = logging.getLogger(__name__)


class MacroContext:
    """Shared runtime state passed between macro nodes."""
    def __init__(
        self,
        device: DeviceController,
        screen: ScreenCapture,
        matcher: TemplateMatcher,
        ocr: OCRReader
    ):
        self.device = device
        self.screen = screen
        self.matcher = matcher
        self.ocr = ocr
        self.variables: Dict[str, Any] = {}
        self.last_match: Optional[MatchResult] = None
        self.last_frame: Any = None


class BaseMacroNode:
    """Base class for all macro execution nodes."""
    def __init__(self, name: str = ""):
        self.name = name or self.__class__.__name__

    def execute(self, ctx: MacroContext) -> bool:
        raise NotImplementedError


class FindImageNode(BaseMacroNode):
    """Finds a template image on the current screen."""
    def __init__(
        self,
        template_name: str,
        threshold: float = 0.80,
        roi: Optional[Tuple[int, int, int, int]] = None,
        save_var: Optional[str] = None,
        name: str = ""
    ):
        super().__init__(name or f"FindImage({template_name})")
        self.template_name = template_name
        self.threshold = threshold
        self.roi = roi
        self.save_var = save_var

    def execute(self, ctx: MacroContext) -> bool:
        ctx.last_frame = ctx.screen.capture()
        match = ctx.matcher.find_template(
            ctx.last_frame,
            self.template_name,
            threshold=self.threshold,
            roi=self.roi
        )
        ctx.last_match = match
        if self.save_var:
            ctx.variables[self.save_var] = match
        found = match is not None
        logger.debug(f"[{self.name}] Result: {'FOUND at ' + str(match.to_tuple()) if found else 'NOT FOUND'}")
        return found


class ClickNode(BaseMacroNode):
    """Clicks on last match, fixed coordinates, or saved variable match."""
    def __init__(
        self,
        target: Optional[Tuple[int, int]] = None,
        use_last_match: bool = True,
        from_var: Optional[str] = None,
        jitter: bool = True,
        delay_range: Tuple[float, float] = (0.4, 0.8),
        name: str = ""
    ):
        super().__init__(name or "Click")
        self.target = target
        self.use_last_match = use_last_match
        self.from_var = from_var
        self.jitter = jitter
        self.delay_range = delay_range

    def execute(self, ctx: MacroContext) -> bool:
        target_x, target_y = None, None
        if self.target is not None:
            target_x, target_y = self.target
        elif self.from_var and self.from_var in ctx.variables:
            m = ctx.variables[self.from_var]
            if isinstance(m, MatchResult):
                target_x, target_y = m.center_x, m.center_y
            elif isinstance(m, (tuple, list)) and len(m) == 2:
                target_x, target_y = m[0], m[1]
        elif self.use_last_match and ctx.last_match is not None:
            target_x, target_y = ctx.last_match.center_x, ctx.last_match.center_y

        if target_x is not None and target_y is not None:
            ctx.device.tap(target_x, target_y, jitter=self.jitter, delay_range=self.delay_range)
            logger.debug(f"[{self.name}] Tapped at ({target_x}, {target_y})")
            return True
        logger.warning(f"[{self.name}] No valid coordinate to click.")
        return False


class SwipeNode(BaseMacroNode):
    """Executes human-like swipe gesture."""
    def __init__(
        self,
        start: Tuple[int, int],
        end: Tuple[int, int],
        duration_ms: int = 350,
        name: str = ""
    ):
        super().__init__(name or f"Swipe({start}->{end})")
        self.start = start
        self.end = end
        self.duration_ms = duration_ms

    def execute(self, ctx: MacroContext) -> bool:
        ctx.device.swipe(self.start, self.end, duration_ms=self.duration_ms)
        return True


class WaitImageNode(BaseMacroNode):
    """Waits until a template appears on screen or timeout expires."""
    def __init__(
        self,
        template_name: str,
        timeout_sec: float = 10.0,
        check_interval: float = 0.5,
        threshold: float = 0.80,
        name: str = ""
    ):
        super().__init__(name or f"WaitImage({template_name})")
        self.template_name = template_name
        self.timeout_sec = timeout_sec
        self.check_interval = check_interval
        self.threshold = threshold

    def execute(self, ctx: MacroContext) -> bool:
        start_time = time.time()
        finder = FindImageNode(self.template_name, threshold=self.threshold)
        while time.time() - start_time < self.timeout_sec:
            if finder.execute(ctx):
                return True
            time.sleep(self.check_interval)
        logger.warning(f"[{self.name}] Timed out after {self.timeout_sec}s")
        return False


class DelayNode(BaseMacroNode):
    """Sleeps for a randomized duration."""
    def __init__(self, min_sec: float, max_sec: float, name: str = ""):
        super().__init__(name or f"Delay({min_sec}-{max_sec}s)")
        self.min_sec = min_sec
        self.max_sec = max_sec

    def execute(self, ctx: MacroContext) -> bool:
        dur = random.uniform(self.min_sec, self.max_sec)
        time.sleep(dur)
        return True


class ConditionalNode(BaseMacroNode):
    """Executes branch A if condition evaluates to True, else branch B."""
    def __init__(
        self,
        condition_node: BaseMacroNode,
        if_true_nodes: List[BaseMacroNode],
        if_false_nodes: Optional[List[BaseMacroNode]] = None,
        name: str = ""
    ):
        super().__init__(name or "IfElse")
        self.condition_node = condition_node
        self.if_true_nodes = if_true_nodes
        self.if_false_nodes = if_false_nodes or []

    def execute(self, ctx: MacroContext) -> bool:
        cond_result = self.condition_node.execute(ctx)
        if cond_result:
            for node in self.if_true_nodes:
                node.execute(ctx)
            return True
        else:
            for node in self.if_false_nodes:
                node.execute(ctx)
            return False
