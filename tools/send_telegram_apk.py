#!/usr/bin/env python3
"""
Dispatches compiled APK binary to Telegram channel via robust curl multipart streaming.
"""
import os
import sys
import glob
import json
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
    print(f"Uploading APK: {apk_path} ({file_size_str}) to Telegram {chat_id}...")

    # Telegram caption limit is 1024 characters
    caption_text = f"""👑 ⚡ <b>Ai Marco coc v9.2 (CognitivePeak Edition)</b>
📦 <b>File:</b> Ai-Marco-coc-v9.2.apk ({file_size_str})
⚡ <b>API Key Test Ping:</b> Live ms latency &amp; validation check
🧠 <b>Vision Failover:</b> Gemini -> OpenRouter -> Groq -> Local TFLite
🗺️ <b>Defense Heatmap:</b> 64-bit base DNA &amp; kill-zone avoidance
🌊 <b>3-Phase Wave Funnel:</b> Outer clear -> 4-finger push -> Hero surge
🔄 <b>Supercell ID Auto-Cycle:</b> 4-account rotation
🔋 <b>Health Watchdog:</b> 42°C overheat &amp; 15% battery safety halts
🛡️ <b>Clan Routines:</b> 12m CC Request, Capital Raids &amp; Clan Games
🎁 <b>24h Freebie Claimer:</b> Daily snacks, potions &amp; ores
🐉 <b>5th Hero &amp; Pet:</b> Dragon Duke + Greedy Raven frenzy
🚨 <b>Panic Stop:</b> Volume Down Key Override"""

    caption_file = "/tmp/telegram_caption.txt"
    with open(caption_file, "w", encoding="utf-8") as f:
        f.write(caption_text)

    cmd = [
        "curl", "-s", "-S", "--connect-timeout", "60", "--max-time", "600",
        "-F", f"chat_id={chat_id}",
        "-F", "parse_mode=HTML",
        "-F", f"caption=<{caption_file}",
        "-F", f"document=@{apk_path};filename=Ai-Marco-coc-v9.2.apk",
        f"https://api.telegram.org/bot{bot_token}/sendDocument"
    ]

    try:
        proc = subprocess.run(cmd, capture_output=True, text=True, check=True)
        print("Curl output:", proc.stdout[:250])
        res = json.loads(proc.stdout)
        if not res.get("ok"):
            print("Telegram API returned error:", res)
            sys.exit(1)
        print(f"Telegram Dispatch Successful! Message ID: {res['result']['message_id']}")
    except Exception as e:
        print(f"ERROR uploading to Telegram: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()
