"""
AAA COC AI MARCO - Home Village Dragon Farming Engine.
Automated Dragon & Zap Air Assault with 0-Cost Quick Training & Star Bonus Ores.
"""

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
    banner_text = """
=================================================================
             ⚔ AAA COC AI MARCO - DRAGON FARMING PRO ⚔          
      Home Village Auto-Farming | 0-Cost Zap Dragons | Ores      
=================================================================
"""
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
        print("\n--- AAA COC AI MARCO CONFIGURATION ---")
        print(f"Village Target: Home Village Only")
        print(f"Attack Strategy: {app_cfg.battle.strategy}")
        print(f"Min Loot: Gold={app_cfg.farming.min_gold:,} | Elixir={app_cfg.farming.min_elixir:,} | Dark={app_cfg.farming.min_dark_elixir:,}")
        print(f"Air Defense Zap: {app_cfg.battle.use_lightning_zap}")
        print(f"Quick Train Slot: Preset #{app_cfg.village.army_slot} (0-Cost Dragons)")
        print("--------------------------------------\n")


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
