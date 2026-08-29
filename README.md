# Ai Marco coc — Autonomous Game Agent & Vision Automation

Autonomous AI Game Agent and Multimodal Vision Automation Engine for **Clash of Clans**.
Featuring **180+ Real Datamined Transparent Supercell Assets**, **Screenshot-to-Code Multimodal Vision AI**, **Zap Dragon Home Village Farming Loop**, **Universal Screen Scaling**, **Google ML Kit OCR**, **Multi-Key 429 Auto-Rotator**, and **Self-Improving Memory Engine**.

---

## 🎨 100% Real Datamined Clash of Clans Assets

All placeholder and mock assets have been removed and replaced with 180+ transparent, pixel-exact game sprites sourced from Supercell datamined archives (`Statscell/clash-assets` & `ClashKingAssets`):
- **74 Troop & Spell Icons**: Barbarian, Archer, Baby Dragon, Dragon, Electro Dragon, Dragon Rider, Balloon, Sneaky Goblin, Root Rider, Lightning/Zap, Rage, Freeze, Jump, Bat, etc.
- **Town Halls & Builder Halls**: Exact building sprites for Town Hall 1 through 14.5 and Builder Hall 1 through 9.
- **Hero Equipments**: Giant Gauntlet, Spiky Ball, Frozen Arrow, Magic Mirror, Fireball, Eternal Tome, Haste Vial, Rocket Spear, etc.
- **Resources & Ores**: Gold, Elixir, Dark Elixir, Shiny Ore, Glowy Ore, Starry Ore, Gems.
- **UI Elements & Buttons**: Attack, Next, Return Home, Surrender, Donate, Clan War badges.

---

## 📸 Multimodal Screenshot-to-Code Vision Engine

Inspired by [abi/screenshot-to-code](https://github.com/abi/screenshot-to-code), this engine takes raw in-game screenshots, base layouts, and live screen captures, and uses Multimodal Vision Models (Google Gemini 2.0 / 1.5 Flash, GPT-4o, Claude 3.5 Sonnet, Groq Vision) to automatically:
1. **Detect Game State & Entities**: Identifies Town Hall level, Air Defenses, Sweepers, Monolith, Eagle Artillery, Clan Castle, and Hero placements.
2. **Extract Precise Coordinates**: Generates normalized and 1080p pixel coordinates with bounding boxes.
3. **Synthesize Executable Macro Actions**: Compiles tactical attack plans into executable Python and Kotlin macro scripts.
4. **Autonomous Ingestion & Watcher**: Automatically processes any screenshots placed into the `uploads/` directory.

---

## 🎮 Macrorify-Style 6-Button Floating Controller

Directly in the Android overlay HUD:
- 🖐️ **Vision Snap:** Real-time Screenshot-to-Code base analysis.
- ▶️ **Play / ⏸️ Pause:** Starts and pauses autonomous farming.
- ⚙️ **Settings:** Displays loot filters and attack delay settings.
- 🛠️ **Tools:** Runs Clan Chat Auto-Donate & Anti-AFK Base Patrol.
- 💻 **Console:** Real-time log inspector and macro action debugger.
- ❌ **Close:** Safely stops background services and dismisses overlay.

---

## 📂 Upload Folder Structure

| Directory | Purpose |
|---|---|
| [`uploads/screenshots/`](file:///root/projects/Ai-marco/uploads/screenshots) | Drop in-game raid screenshots for tactical analysis and action code generation. |
| [`uploads/bases/`](file:///root/projects/Ai-marco/uploads/bases) | Drop enemy base layouts or war bases for attack route planning. |
| [`uploads/templates/`](file:///root/projects/Ai-marco/uploads/templates) | 180+ Real transparent troop, spell, building, and equipment template assets. |
| [`uploads/processed/`](file:///root/projects/Ai-marco/uploads/processed) | Output directory where JSON analysis and Python action scripts are automatically saved. |

---

## 🚀 GitHub Actions Cloud Build & Telegram Dispatch

- **Repository**: [https://github.com/aaa-infinity/AAA-COC-AI-MARCO](https://github.com/aaa-infinity/AAA-COC-AI-MARCO)
- **Telegram Channel**: [https://t.me/aaafreecloud](https://t.me/aaafreecloud) (`@aaafreecloud`)
- Automatically compiles `Ai-Marco-coc-v4.0-APK` on cloud runners with Gradle 8.7 & JDK 17 and delivers the APK directly to the Telegram channel.
