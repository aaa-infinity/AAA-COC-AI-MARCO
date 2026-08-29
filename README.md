# AAA COC AI MARCO - Commercial Edition

Autonomous AI Game Agent and Multimodal Vision Automation Engine for **Clash of Clans**.
Featuring **Screenshot-to-Code Multimodal Vision AI**, **Zap Dragon Home Village Farming Loop**, **Universal Screen Scaling**, **Google ML Kit OCR**, **Multi-Key 429 Auto-Rotator**, and **Self-Improving Memory Engine**.

---

## 📸 Multimodal Screenshot-to-Code Vision Engine

Inspired by [abi/screenshot-to-code](https://github.com/abi/screenshot-to-code), this engine takes raw in-game screenshots, base layouts, and live screen captures, and uses Multimodal Vision Models (Google Gemini 2.0 / 1.5 Flash, GPT-4o, Claude 3.5 Sonnet, Groq Vision, Ollama) to automatically:
1. **Detect Game State & Entities**: Identifies Town Hall level, Air Defenses, Sweepers, Monolith, Eagle Artillery, Clan Castle, and Hero placements.
2. **Extract Precise Coordinates**: Generates normalized and 1080p pixel coordinates with bounding boxes.
3. **Synthesize Executable Macro Actions**: Compiles tactical attack plans into executable Python and Kotlin macro scripts.
4. **Autonomous Ingestion & Watcher**: Automatically processes any screenshots placed into the `uploads/` directory.

```
                    ┌───────────────────────────────┐
                    │    In-Game Screenshot / Base   │
                    │   (uploads/ or Live Screen)   │
                    └───────────────┬───────────────┘
                                    │
                                    ▼
                    ┌───────────────────────────────┐
                    │  ScreenshotToCodeEngine (AI)  │
                    │ (Gemini 2.0 / GPT-4o / Claude)│
                    └───────────────┬───────────────┘
                                    │
           ┌────────────────────────┴────────────────────────┐
           ▼                                                 ▼
┌─────────────────────────────┐                   ┌─────────────────────────────┐
│    Structured JSON Map      │                   │ Executable Macro Action Code│
│ - Air Defense Coordinates   │                   │ - zap_targets([AD1, AD2])   │
│ - Town Hall Location        │                   │ - funnel_heroes(King, Queen)│
│ - Red Line Border           │                   │ - spread_dragons(line)      │
│ - Available Loot (OCR)      │                   │ - trigger_warden_ability()  │
└─────────────────────────────┘                   └─────────────────────────────┘
```

---

## 📂 Upload Folder Structure

Upload your screenshots, base layouts, or templates to the dedicated upload folders:

| Directory | Purpose |
|---|---|
| [`uploads/screenshots/`](file:///root/projects/Ai-marco/uploads/screenshots) | Drop in-game raid screenshots for tactical analysis and action code generation. |
| [`uploads/bases/`](file:///root/projects/Ai-marco/uploads/bases) | Drop enemy base layouts or war bases for attack route planning. |
| [`uploads/templates/`](file:///root/projects/Ai-marco/uploads/templates) | Custom UI buttons, spell icons, or unit templates. |
| [`uploads/processed/`](file:///root/projects/Ai-marco/uploads/processed) | Output directory where JSON analysis and Python action scripts are automatically saved. |

### Running the Upload Watcher:
```bash
python tools/watch_uploads.py
```

---

## 🛠 Core Features

1. **🐉 Home Village Zap Dragon Auto-Farming Loop**:
   - 0-Cost army quick-training.
   - Resource collection (Gold Mines, Elixir Collectors, Dark Drills, Gem Mine, Treasury, Daily Star Bonus Ores).
   - Multiplayer matchmaking with OCR loot filtering.
   - Lightning zap deployment on top Air Defenses.
   - Corner hero funneling (King & Queen).
   - Wide dragon line deployment, Grand Warden drop, Rage spell core breach, and Hero Equipment activation.
   - Automated surrender and return home.

2. **📱 Universal Phone & Screen Adapter**:
   - `UniversalScreenAdapter.kt` dynamically scales reference 1080p coordinates to any device resolution (16:9, 18:9, 19.5:9, 20:9, 21:9, and tablets).

3. **🛡️ Real-Time Telemetry & Self-Healing**:
   - `CrashTelemetryService.kt` registers global uncaught-exception handling and writes real-time diagnostics to `telemetry_diagnostics.json`.

4. **🧠 AI Provider Multi-Key Rotator**:
   - Maintains an active pool of API keys (Gemini, Groq, OpenRouter, OpenAI). Automatically switches keys upon encountering HTTP 429 rate limits.

5. **📈 Self-Improving AI Memory Engine**:
   - Records past attack outcomes (stars, destruction %, loot gained) and statistically computes the optimal attack entry side (`AiMemoryEngine.kt`).

---

## 🚀 GitHub Actions Cloud Build & Telegram Dispatch

- **Repository**: [https://github.com/aaa-infinity/AAA-COC-AI-MARCO](https://github.com/aaa-infinity/AAA-COC-AI-MARCO)
- **CI/CD Workflow**: `.github/workflows/build-apk.yml`
- **Telegram Channel**: [https://t.me/aaafreecloud](https://t.me/aaafreecloud) (`@aaafreecloud`)
- Automatically compiles `AAA-COC-AI-MARCO-v2.0-Debug.apk` on cloud runners with Gradle 8.7 & JDK 17 and delivers the APK directly to the Telegram channel.

---

## 📱 Standalone Android App (`android_app/`)

- **Accessibility Service**: Non-root touch emulation with Bezier curves and Gaussian jitter.
- **Floating Overlay**: Draggable floating HUD over Clash of Clans with start/pause and live strategy switcher.
- **Dual-Tab UI**: Bot Dashboard + AI Multimodal Vision & Key Rotator.
