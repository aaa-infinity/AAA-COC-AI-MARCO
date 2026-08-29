# Clash of Clans AI & Macro Automation Engine

An autonomous game agent and Macrorify-style image/text macro automation engine for **Clash of Clans**. Designed with **Computer Vision (OpenCV / Template Matching)**, **OCR Text Recognition**, **Finite State Machine (FSM)** orchestration, and **Human-like Bezier Gesture Emulation**.

---

## 📑 Architecture Overview

Inspired by mobile automation apps like **Macrorify - Image Auto Clicker**, this engine combines conditional vision triggers with tactical AI decision-making:

```
                          ┌────────────────────────┐
                          │  Android Device / EMU   │
                          └───────────┬────────────┘
                                      │ (ADB Screencap)
                                      ▼
                          ┌────────────────────────┐
                          │    Screen Capture      │
                          └───────────┬────────────┘
                                      │
              ┌───────────────────────┴───────────────────────┐
              ▼                                               ▼
   ┌────────────────────┐                          ┌────────────────────┐
   │  Template Matcher  │                          │     OCR Reader     │
   │  (OpenCV / NMS)    │                          │ (Gold/Elixir/Dark) │
   └──────────┬─────────┘                          └──────────┬─────────┘
              │                                               │
              └───────────────────────┬───────────────────────┘
                                      ▼
                        ┌───────────────────────────┐
                        │   Autonomous FSM Engine   │
                        │  (Village ↔ Train ↔ Raid) │
                        └─────────────┬─────────────┘
                                      │
              ┌───────────────────────┴───────────────────────┐
              ▼                                               ▼
   ┌────────────────────┐                          ┌────────────────────┐
   │ Tactical Battle AI │                          │ Macro Flow Builder │
   │ (Sneaky / BARCH)   │                          │ (Macrorify-Style)  │
   └──────────┬─────────┘                          └──────────┬─────────┘
              │                                               │
              └───────────────────────┬───────────────────────┘
                                      ▼
                        ┌───────────────────────────┐
                        │ Humanized Input (ADB)     │
                        │ - Gaussian Jitter Tap     │
                        │ - Bezier Curved Swipes    │
                        │ - Anti-Ban Rest Delays    │
                        └───────────────────────────┘
```

---

## 🛠 Features

1. **Autonomous Village Manager**:
   - Auto-collects Gold Mines, Elixir Collectors, Dark Drills, Gem Mine, and Loot Cart.
   - Auto-requests Clan Castle reinforcements.
   - Cleans obstacles (optional).

2. **Smart Army Manager**:
   - Trains configured Quick-Train presets (Slot 1, 2, or 3).
   - Verifies army readiness before starting matchmaking.

3. **OCR-Powered Matchmaker (Loot Filter)**:
   - Scans multiplayer bases using OCR.
   - Filters bases by minimum Gold, Elixir, and Dark Elixir thresholds.
   - Auto-skips ("Next") bases with randomized human delays.

4. **Tactical Battle AI Strategies**:
   - **`SNEAKY_GOBLIN`**: Fast perimeter collector harvesting + Jump/Haste core breach.
   - **`BARCH`**: 4-quadrant wave circle (Barbarians in front, Archers behind).
   - **`ZAP_DRAGONS`**: Lightning spells on Air Defenses + dragon funnel + Hero activation.
   - **Auto-Surrender**: Automatically exits battle once target loot is obtained to maximize farming speed.

5. **Macrorify-Style Conditional Graph**:
   - Construct custom macro sequences: `find_and_click()`, `wait_and_click()`, `delay()`, `swipe()`.

6. **Anti-Ban Protections**:
   - Gaussian coordinate randomization.
   - Bezier curve motion smoothing.
   - Randomized action intervals (0.6s - 1.4s).
   - Configurable session rest breaks (e.g., rest 15 mins every 3 hours).

---

## 🚀 Quick Start

### 1. Installation
Ensure Python 3.9+ is installed, then install requirements:

```bash
pip install -r requirements.txt
```

### 2. Connect Your Android Emulator or Device
Enable **USB Debugging** on your device or start your Android emulator:
- **LDPlayer / BlueStacks**: Default port `127.0.0.1:5555`
- **NoxPlayer**: Default port `127.0.0.1:62001`
- **MEmu**: Default port `127.0.0.1:21503`

Verify connection via ADB:
```bash
adb devices
```

### 3. Run Calibration Suite
Test connectivity, screen capture, OCR, and template matching:
```bash
python main.py --mode calibrate
```

### 4. Start Autonomous Farming Bot
```bash
# Farm with Sneaky Goblins (minimum 600k Gold & Elixir)
python main.py --mode bot --strategy SNEAKY_GOBLIN --min-gold 600000 --min-elixir 600000 --min-dark 4000

# Farm with BARCH army using Army Slot 2
python main.py --mode bot --strategy BARCH --slot 2

# Farm with Zap Dragons
python main.py --mode bot --strategy ZAP_DRAGONS
```

### 5. Run Custom Macrorify-Style Macro
```bash
python main.py --mode macro
```

---

## 📝 Custom Macro Scripting Example

You can write custom macro sequences in Python just like building rules in Macrorify:

```python
from macro.builder import MacroFlow
from macro.node import MacroContext

# Initialize context
ctx = MacroContext(device=device, screen=screen, matcher=matcher, ocr=ocr)

# Define custom flow
flow = MacroFlow(name="CustomCollectorRoutine")
flow.find_and_click("bubble_gold", threshold=0.75)
flow.delay(0.5, 0.8)
flow.find_and_click("bubble_elixir", threshold=0.75)
flow.delay(0.5, 0.8)
flow.find_and_click("btn_train_army", threshold=0.75)
flow.delay(1.0, 1.5)
flow.find_and_click("tab_quick_train", threshold=0.75)
flow.find_and_click("btn_close_window", threshold=0.75)

# Execute
flow.run(ctx)
```

---

## ⚙️ Configuration (`config.json`)

Settings are persisted in `config.json` and can be customized:

```json
{
    "device": {
        "adb_host": "127.0.0.1",
        "adb_port": 5555,
        "ref_width": 1920,
        "ref_height": 1080
    },
    "farming": {
        "min_gold": 500000,
        "min_elixir": 500000,
        "min_dark_elixir": 4000,
        "max_search_attempts": 60
    },
    "village": {
        "auto_collect": true,
        "auto_train": true,
        "army_slot": 1,
        "wait_for_full_army": true
    },
    "battle": {
        "strategy": "SNEAKY_GOBLIN",
        "deployment_delay": 0.12,
        "use_heroes": true
    },
    "anti_detection": {
        "enable_jitter": true,
        "jitter_std_dev": 3.5,
        "bezier_swipes": true,
        "session_timeout_minutes": 180,
        "rest_break_minutes": 15
    }
}
```

---

## ⚠️ Important Disclaimer & Terms of Service

* This project is created for **educational and research purposes** exploring computer vision, optical character recognition, and input automation.
* Using bots or automated tools in **Clash of Clans** violates Supercell's Terms of Service and can result in **permanent bans**.
* Use at your own risk. Test only on throwaway accounts.

---

## 📱 Standalone Android APK App (`android_app/`)

An on-device Android app (similar to **Macrorify**) that runs directly on your Android phone or tablet without needing a PC or ADB connection.

### Core Android Components
- **`AutoClickAccessibilityService`**: Uses Android's native `AccessibilityService.dispatchGesture()` to perform humanized jitter taps, curved Bezier swipes, and multi-touch troop deployments without root.
- **`FloatingOverlayService`**: Renders a draggable floating widget on top of Clash of Clans with `[▶ START / ⏸ PAUSE]` and `[Strategy Selector]`.
- **`CocFarmingEngine`**: Modern Clash of Clans logic adapted for modern game mechanics.
- **`FastTemplateMatcher`**: Real-time on-device pixel matching on Android Bitmaps.

### Supported Modern Clash of Clans Features
1. **0-Cost Instant Army Training**: Handles modern zero-cost Quick-Train presets.
2. **Ore & Star Bonus Harvesting**: Collects daily Star Bonuses (Shiny, Glowy, Starry Ores) for Blacksmith equipment upgrades.
3. **Hero Equipment Active Abilities**: Coordinates Hero ability activation timings.
4. **Builder Base 2.0 Fast Farming**: Drops army and triggers immediate loot collection without training delays.
5. **Modern Meta Strategies**:
   - `SNEAKY_GOBLIN` (High-efficiency resource sniping)
   - `ROOT_RIDER_SPAM` (Modern Town Hall 15/16/17 core smash)
   - `BUILDER_BASE_FARM` (Infinite instant builder elixir/gold)
   - `ORE_STAR_BONUS` (Daily star bonus automation)

### Compiling the APK
1. Open the [`android_app`](file:///root/projects/Ai-marco/android_app) folder in **Android Studio**.
2. Click **Build** -> **Build Bundle(s) / APK(s)** -> **Build APK(s)**.
3. Transfer the generated `.apk` to your phone and install.
4. Open the app, grant **Accessibility Permission** and **Floating Overlay Permission**, then tap **Launch Floating Widget**.
