"""
Macro Flow Builder and Runner.
Allows declarative scripting of macro workflows similar to Macrorify's visual graph.
"""

import logging
from typing import List, Tuple, Optional
from macro.node import (
    BaseMacroNode, MacroContext, FindImageNode, ClickNode,
    SwipeNode, WaitImageNode, DelayNode, ConditionalNode
)

logger = logging.getLogger(__name__)


class MacroFlow:
    """A sequential chain of macro nodes."""
    def __init__(self, name: str = "MacroFlow"):
        self.name = name
        self.nodes: List[BaseMacroNode] = []

    def add(self, node: BaseMacroNode) -> "MacroFlow":
        self.nodes.append(node)
        return self

    def find_and_click(
        self,
        template_name: str,
        threshold: float = 0.80,
        jitter: bool = True,
        delay_range: Tuple[float, float] = (0.4, 0.8)
    ) -> "MacroFlow":
        """Helper to find an image and click it if present."""
        find_node = FindImageNode(template_name, threshold=threshold)
        click_node = ClickNode(use_last_match=True, jitter=jitter, delay_range=delay_range)
        cond = ConditionalNode(find_node, if_true_nodes=[click_node])
        self.nodes.append(cond)
        return self

    def wait_and_click(
        self,
        template_name: str,
        timeout_sec: float = 10.0,
        threshold: float = 0.80
    ) -> "MacroFlow":
        """Waits until template appears and clicks it."""
        self.nodes.append(WaitImageNode(template_name, timeout_sec=timeout_sec, threshold=threshold))
        self.nodes.append(ClickNode(use_last_match=True))
        return self

    def tap(self, x: int, y: int, jitter: bool = True) -> "MacroFlow":
        self.nodes.append(ClickNode(target=(x, y), jitter=jitter))
        return self

    def swipe(self, start: Tuple[int, int], end: Tuple[int, int], duration_ms: int = 350) -> "MacroFlow":
        self.nodes.append(SwipeNode(start, end, duration_ms))
        return self

    def delay(self, min_sec: float, max_sec: float) -> "MacroFlow":
        self.nodes.append(DelayNode(min_sec, max_sec))
        return self

    def run(self, ctx: MacroContext) -> bool:
        """Executes the macro sequence from start to finish."""
        logger.info(f"Running macro flow: '{self.name}' ({len(self.nodes)} steps)")
        for idx, node in enumerate(self.nodes):
            logger.debug(f"Step {idx+1}/{len(self.nodes)}: {node.name}")
            node.execute(ctx)
        return True
