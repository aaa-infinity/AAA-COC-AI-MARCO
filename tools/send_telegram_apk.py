#!/usr/bin/env python3
"""
Dispatches compiled APK binary to Telegram channel via direct multipart/form-data upload.
"""
import os
import sys
import glob
import urllib.request

def main():
    bot_token = os.environ.get("TELEGRAM_BOT_TOKEN", "8841143616:AAGbcJKf3MLTN17-tpmwhZKZQIIbErDT1PA")
    chat_id = os.environ.get("TELEGRAM_CHAT_ID", "-1004447017934")

    # Locate APK
    apk_candidates = glob.glob("dist/*.apk") + glob.glob("android_app/app/build/outputs/apk/debug/*.apk")
    if not apk_candidates:
        print("ERROR: No APK candidate found!")
        sys.exit(1)

    apk_path = apk_candidates[0]
    file_size_mb = os.path.getsize(apk_path) / (1024 * 1024)
    file_size_str = f"{file_size_mb:.2f} MB"
    print(f"Uploading APK: {apk_path} ({file_size_str}) to Telegram {chat_id}...")

    caption = f"""🏛️ 🧠 <b>Ai Marco coc v6.1 (Expanded Neural Ari Agent) Released!</b>
📦 <b>File:</b> Ai-Marco-coc-v6.1.apk ({file_size_str})
⚡ <b>Neural Vision Engine:</b> 12MB On-Device TFLite Core Defense Detector
🎨 <b>Expanded Assets:</b> 285+ High-Res Datamined Building & Defense Sprites
🧠 <b>Ari AI Agent:</b> Hermes-Class Autonomous ReAct Tool-Calling Engine
🏆 <b>Complex AI Engine:</b> TH16/17 Root Rider Overgrowth & Giga Protection
🏗️ <b>Auto-Builder:</b> Upgrades suggested defenses & traps automatically
🎁 <b>Daily Rewards:</b> Claims Free Trader Gifts & Star Bonus Ores
🛡️ <b>Clan Castle Counter:</b> Auto Poison/Freeze on defending troops
🎮 <b>Smooth Battle Pacer:</b> 7-Phase Humanized Coordinated Deployment
🖐️ <b>True Multi-Touch:</b> 4-Finger Line Wave & 2-Finger Funnel
🧱 <b>Auto Wall Dump:</b> Upgrades walls automatically when gold is full
🔍 <b>Smart Nexting:</b> Intelligent base search until rich loot found
🚨 <b>Panic Stop:</b> Physical Volume Down key override
🛡️ <b>Supervisor:</b> Auto-Crash & Disconnect Recovery
🤝 <b>Clan Suite:</b> Auto-Donate & CC Request
💎 <b>Village Clean:</b> Gem Boxes & Obstacle clearing
🎮 <b>Auto-Launch:</b> Direct Clash of Clans Game Launcher"""

    boundary = "----AiMarcoTelegramBoundary987654321"
    with open(apk_path, "rb") as f:
        file_bytes = f.read()

    body = []
    body.append(f"--{boundary}\r\nContent-Disposition: form-data; name=\"chat_id\"\r\n\r\n{chat_id}\r\n".encode("utf-8"))
    body.append(f"--{boundary}\r\nContent-Disposition: form-data; name=\"parse_mode\"\r\n\r\nHTML\r\n".encode("utf-8"))
    body.append(f"--{boundary}\r\nContent-Disposition: form-data; name=\"caption\"\r\n\r\n{caption}\r\n".encode("utf-8"))
    body.append(f"--{boundary}\r\nContent-Disposition: form-data; name=\"document\"; filename=\"Ai-Marco-coc-v6.1.apk\"\r\nContent-Type: application/vnd.android.package-archive\r\n\r\n".encode("utf-8"))
    body.append(file_bytes)
    body.append(f"\r\n--{boundary}--\r\n".encode("utf-8"))

    payload = b"".join(body)
    req = urllib.request.Request(
        f"https://api.telegram.org/bot{bot_token}/sendDocument",
        data=payload,
        headers={"Content-Type": f"multipart/form-data; boundary={boundary}"}
    )

    try:
        with urllib.request.urlopen(req, timeout=120) as resp:
            print("Telegram Dispatch Successful! Status code:", resp.status)
            print("Response:", resp.read().decode("utf-8")[:200])
    except Exception as e:
        print("ERROR uploading to Telegram:", e)
        sys.exit(1)

if __name__ == "__main__":
    main()
