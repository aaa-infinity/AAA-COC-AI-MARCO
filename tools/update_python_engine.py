import os

# Update config/settings.py with modern strategies and options
settings_content = """\"\"\"
Configuration settings for Modern Clash of Clans AI & Macro Engine.
Supports TH16/TH17, Hero Hall, Equipment Ores, Apprentice Builder & Overgrowth Spell.
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
    min_dark_elixir: int = 5_000
    target_star_bonus_ores: bool = True  # Prioritize 1-star win to collect Daily Ores
    max_search_attempts: int = 60


@dataclass
class VillageConfig:
    auto_collect_ores: bool = True  # Star Bonus & Blacksmith
    auto_collect_resources: bool = True
    auto_train: bool = True
    army_slot: int = 1
    auto_assign_apprentice_builder: bool = True  # Boost upgrade 1hr daily
    auto_craft_capital_gold: bool = True  # Forge gold converter
    auto_request_troops: bool = True


@dataclass
class BattleConfig:
    strategy: str = "OVERGROWTH_ROOT_RIDER"  # OVERGROWTH_ROOT_RIDER, SNEAKY_GOBLIN_ORE_FARM, TH17_HERO_SMASH, BUILDER_BASE_FAST_FARM, BARCH
    use_overgrowth_spell: bool = True
    use_hero_equipment_abilities: bool = True
    hero_ability_delay_sec: int = 12
    deployment_delay: float = 0.10
    auto_surrender_on_loot: bool = True


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
    f.write(settings_content)

print("Updated config/settings.py with modern features")
