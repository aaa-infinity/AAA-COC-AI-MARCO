package com.cocai.autoclicker.engine

data class ProMetaArmy(
    val name: String,
    val source: String, // "Judo Sloth Meta 2026", "Itzu Pro CWL", "CarbonFin 3-Star Guide"
    val description: String,
    val armyComp: List<String>,
    val spells: List<String>,
    val heroEquipment: List<String>,
    val targetObjective: String // "3-STAR PRO DESTRUCTION", "TROPHY PUSH", "FAST ORE FARM"
)

object ProMetaStrategyHub {

    val META_STRATEGIES = listOf(
        ProMetaArmy(
            name = "Root Rider & Valkyrie Overgrowth Smash (TH16/17 #1 Meta)",
            source = "Judo Sloth / Itzu CWL Championship Meta",
            description = "The undisputed #1 3-star tournament meta. Overgrowth freezes 40% of the base while Root Riders crush through walls, followed by Valkyrie rage blitz.",
            armyComp = listOf("8x Root Rider", "10x Valkyrie", "3x Apprentice Warden", "4x Super Barbarian", "2x Headhunter"),
            spells = listOf("2x Overgrowth Spell", "3x Rage Spell", "2x Freeze Spell", "1x Poison"),
            heroEquipment = listOf("King: Giant Gauntlet + Rage Vial", "Queen: Frozen Arrow + Healer Puppet", "Warden: Eternal Tome + Healing Tome", "RC: Rocket Spear + Haste Vial"),
            targetObjective = "3-STAR PRO DESTRUCTION"
        ),
        ProMetaArmy(
            name = "Zap Dragon & Dragon Rider Surge (Universal Fast Farm)",
            source = "CarbonFin High-Efficiency Farming Guide",
            description = "Precision lightning zap destroys 2 top Air Defenses followed by a 4-finger synchronized dragon wave and core rage surge.",
            armyComp = listOf("10x Dragon", "4x Dragon Rider", "8x Balloon", "2x Baby Dragon"),
            spells = listOf("6x Lightning Spell", "2x Rage Spell", "2x Freeze Spell"),
            heroEquipment = listOf("King: Spiky Ball + Vampstache", "Queen: Giant Arrow + Invisibility", "Warden: Eternal Tome + Fireball", "RC: Seeking Shield + Royal Gem"),
            targetObjective = "FAST ORE FARM & 3-STAR"
        ),
        ProMetaArmy(
            name = "Electro Dragon Chain Wipeout (Anti-Island Base)",
            source = "Eric OneHive Competitive Guide",
            description = "High chain-lightning damage wipes compact island bases. 2-finger funneling forces dragons straight through Town Hall and Monolith.",
            armyComp = listOf("7x Electro Dragon", "12x Balloon", "1x Baby Dragon"),
            spells = listOf("4x Rage Spell", "3x Freeze Spell"),
            heroEquipment = listOf("King: Giant Gauntlet", "Queen: Frozen Arrow", "Warden: Eternal Tome + Life Gem", "RC: Haste Vial"),
            targetObjective = "2-STAR / 3-STAR CORE WIPE"
        ),
        ProMetaArmy(
            name = "Sneaky Goblin Surgical Ore & Loot Harvest",
            source = "Clash Bashing Rapid Farming Strategy",
            description = "Instant 30-second raid. Multi-touch perimeter taps on all outer mines, drills, and Town Hall for maximum loot per hour.",
            armyComp = listOf("75x Sneaky Goblin", "6x Super Wall Breaker"),
            spells = listOf("4x Jump Spell", "3x Invisibility Spell", "1x Rage Spell"),
            heroEquipment = listOf("Queen: Giant Arrow (clears line across base)", "Warden: Eternal Tome"),
            targetObjective = "FASTEST LOOT & ORE HARVEST"
        )
    )
}
