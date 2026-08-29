#!/usr/bin/env python3
"""
Dispatches compiled APK binary to Telegram channel via robust curl multipart streaming.
"""
import os
import sys
import glob
import subprocess

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
    print(f"Uploading APK: {apk_path} ({file_size_str}) to Telegram {chat_id} via robust curl stream...")

    caption = f"""🌾 🧱 <b>Ai Marco coc v6.4 (Home Village Farm &amp; Wall Maxer)</b>
📦 <b>File:</b> Ai-Marco-coc-v6.4.apk ({file_size_str})
🏡 <b>100% Home Village Focus:</b> Pure fast loot farming &amp; wall upgrades
🧱 <b>Continuous Wall Dump:</b> Upgrades walls with free builder every raid
⚡ <b>0-Cost Quick Train:</b> Infinite Root Rider, Zap Dragon &amp; Goblin farm
🔍 <b>Smart Nexting Search:</b> 500k+ Gold &amp; Elixir hunter
🖐️ <b>Multi-Touch Raid:</b> 4-Finger Line Wave &amp; 2-Finger Funnel
🎨 <b>New AAA Icon:</b> Glowing Neon Cyber-Dragon &amp; Royal Crown
🚨 <b>Panic Stop:</b> Volume Down Key Override"""

    cmd = [
        "curl", "-s", "-S", "--connect-timeout", "60", "--max-time", "600",
        "-F", f"chat_id={chat_id}",
        "-F", "parse_mode=HTML",
        "-F", f"caption={caption}",
        "-F", f"document=@{apk_path};filename=Ai-Marco-coc-v6.4.apk",
        f"https://api.telegram.org/bot{bot_token}/sendDocument"
    ]

    try:
        result = subprocess.run(cmd, capture_output=True, text=True, check=True)
        print("Telegram Dispatch Successful!")
        print("Response:", result.stdout[:200])
    except subprocess.CalledProcessError as e:
        print(f"ERROR uploading to Telegram: {e}")
        print("Stderr:", e.stderr)
        print("Stdout:", e.stdout)
        sys.exit(1)

if __name__ == "__main__":
    main()
