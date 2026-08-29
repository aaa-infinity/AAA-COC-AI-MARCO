#!/usr/bin/env python3
"""
Dispatches compiled APK binary to Telegram channel via direct multipart/form-data upload.
"""
import os
import sys
import glob
import urllib.request
import urllib.error

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

    # Telegram caption MUST be under 1024 characters
    caption = f"""🏛️ 🐉 <b>Ai Marco coc v6.3 (Cyber Dragon Edition)</b>
📦 <b>File:</b> Ai-Marco-coc-v6.3.apk ({file_size_str})
🎨 <b>New AAA Icon:</b> Glowing Neon Cyber-Dragon &amp; Royal Crown
🔨 <b>Blacksmith Auto-Upgrader:</b> Hero Equipment &amp; Ores
🏰 <b>Clan Capital:</b> Auto Weekend Raid &amp; Gold Contribution
⚡ <b>Neural Vision:</b> 12MB TFLite On-Device Core Detector
🧠 <b>Ari AI Agent:</b> Hermes ReAct Autonomous Engine
🏆 <b>Pro Meta:</b> TH16/17 Root Rider &amp; Zap Dragons
🧪 <b>Spell Brewer &amp; Pass:</b> Auto-Brew &amp; Season Claims
🖐️ <b>Multi-Touch:</b> 4-Finger Line Wave &amp; 2-Finger Funnel
🚨 <b>Panic Stop:</b> Volume Down Key Override"""

    boundary = "----AiMarcoTelegramBoundary987654321"
    with open(apk_path, "rb") as f:
        file_bytes = f.read()

    body = []
    body.append(f"--{boundary}\r\nContent-Disposition: form-data; name=\"chat_id\"\r\n\r\n{chat_id}\r\n".encode("utf-8"))
    body.append(f"--{boundary}\r\nContent-Disposition: form-data; name=\"parse_mode\"\r\n\r\nHTML\r\n".encode("utf-8"))
    body.append(f"--{boundary}\r\nContent-Disposition: form-data; name=\"caption\"\r\n\r\n{caption}\r\n".encode("utf-8"))
    body.append(f"--{boundary}\r\nContent-Disposition: form-data; name=\"document\"; filename=\"Ai-Marco-coc-v6.3.apk\"\r\nContent-Type: application/vnd.android.package-archive\r\n\r\n".encode("utf-8"))
    body.append(file_bytes)
    body.append(f"\r\n--{boundary}--\r\n".encode("utf-8"))

    payload = b"".join(body)
    req = urllib.request.Request(
        f"https://api.telegram.org/bot{bot_token}/sendDocument",
        data=payload,
        headers={"Content-Type": f"multipart/form-data; boundary={boundary}"}
    )

    try:
        with urllib.request.urlopen(req, timeout=180) as resp:
            print("Telegram Dispatch Successful! Status code:", resp.status)
            print("Response:", resp.read().decode("utf-8")[:200])
    except urllib.error.HTTPError as e:
        err_msg = e.read().decode("utf-8")
        print(f"ERROR uploading to Telegram (HTTP {e.code}): {err_msg}")
        sys.exit(1)
    except Exception as e:
        print("ERROR uploading to Telegram:", e)
        sys.exit(1)

if __name__ == "__main__":
    main()
