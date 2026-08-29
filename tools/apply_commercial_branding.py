import os

pkg_dir = 'android_app/app/src/main/java/com/cocai/autoclicker'

# 1. Update strings.xml
strings_xml = """<resources>
    <string name="app_name">AAA COC AI MARCO</string>
    <string name="accessibility_service_description">AAA COC AI MARCO - Commercial Grade Autonomous Vision &amp; Touch Engine for Modern Clash of Clans (TH16/TH17, Ores, Hero Equipment, Overgrowth Spell).</string>
</resources>
"""

with open('android_app/app/src/main/res/values/strings.xml', 'w') as f:
    f.write(strings_xml)

# 2. Update activity_main.xml
activity_main_xml = """<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="#090D16"
    android:padding="22dp">

    <!-- Header Section -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center_vertical">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="⚔ AAA COC AI MARCO"
            android:textColor="#F59E0B"
            android:textSize="22sp"
            android:textStyle="bold" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginStart="8dp"
            android:background="#1E293B"
            android:paddingHorizontal="6dp"
            android:paddingVertical="2dp"
            android:text="PRO v2.0"
            android:textColor="#10B981"
            android:textSize="10sp"
            android:textStyle="bold" />
    </LinearLayout>

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="6dp"
        android:text="Commercial-Grade Autonomous AI Engine for Clash of Clans. Powered by on-device Computer Vision, 0-Cost Quick Training, Hero Equipment Combos, Star Bonus Ores &amp; Overgrowth Spell."
        android:textColor="#94A3B8"
        android:textSize="12sp" />

    <View
        android:layout_width="match_parent"
        android:layout_height="1dp"
        android:layout_marginVertical="16dp"
        android:background="#1E293B" />

    <!-- Status Cards -->
    <TextView
        android:id="@+id/tv_accessibility_status"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="#131C2E"
        android:padding="12dp"
        android:text="Accessibility Service: CHECKING..."
        android:textColor="#F59E0B"
        android:textSize="13sp" />

    <TextView
        android:id="@+id/tv_overlay_status"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:background="#131C2E"
        android:padding="12dp"
        android:text="Floating Overlay: CHECKING..."
        android:textColor="#F59E0B"
        android:textSize="13sp" />

    <!-- Action Buttons -->
    <Button
        android:id="@+id/btn_grant_accessibility"
        android:layout_width="match_parent"
        android:layout_height="48dp"
        android:layout_marginTop="16dp"
        android:backgroundTint="#2563EB"
        android:text="1. Grant Accessibility Service (Non-Root)"
        android:textColor="#FFFFFF"
        android:textSize="12sp" />

    <Button
        android:id="@+id/btn_grant_overlay"
        android:layout_width="match_parent"
        android:layout_height="48dp"
        android:layout_marginTop="8dp"
        android:backgroundTint="#2563EB"
        android:text="2. Grant Floating Overlay Permission"
        android:textColor="#FFFFFF"
        android:textSize="12sp" />

    <Button
        android:id="@+id/btn_start_floating_hud"
        android:layout_width="match_parent"
        android:layout_height="54dp"
        android:layout_marginTop="20dp"
        android:backgroundTint="#10B981"
        android:text="🚀 LAUNCH AAA COC AI MARCO"
        android:textColor="#FFFFFF"
        android:textSize="14sp"
        android:textStyle="bold" />

    <!-- Feature Highlights -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="20dp"
        android:text="Active AI Features:
• Town Hall 17 Hero Hall &amp; Minion Prince Flying Hero
• Overgrowth Spell Flank Freezing AI
• Druid + Root Rider Smasher
• Daily Star Bonus Ore Harvester (Shiny/Glowy/Starry)
• Apprentice Builder Daily 1-Hour Speedup
• Builder Base 2.0 Fast Surrender Loop"
        android:textColor="#64748B"
        android:textSize="11sp" />

</LinearLayout>
"""

with open('android_app/app/src/main/res/layout/activity_main.xml', 'w') as f:
    f.write(activity_main_xml)

# 3. Update floating_hud.xml
floating_hud_xml = """<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:background="#F20A0F1D"
    android:padding="10dp"
    android:elevation="10dp">

    <!-- Header / Drag handle -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center_vertical">

        <TextView
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="⚔ AAA COC AI MARCO"
            android:textColor="#F59E0B"
            android:textSize="11sp"
            android:textStyle="bold" />

        <TextView
            android:id="@+id/tv_hud_status"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="[IDLE]"
            android:textColor="#10B981"
            android:textSize="10sp"
            android:textStyle="bold" />
    </LinearLayout>

    <TextView
        android:id="@+id/tv_ore_stats"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="3dp"
        android:text="💎 Ores: Ready | 0-Cost Quick Train"
        android:textColor="#94A3B8"
        android:textSize="9sp" />

    <!-- Button Row -->
    <LinearLayout
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:layout_marginTop="6dp">

        <Button
            android:id="@+id/btn_strategy"
            android:layout_width="wrap_content"
            android:layout_height="32dp"
            android:backgroundTint="#4F46E5"
            android:text="OVERGROWTH ROOT RIDER"
            android:textColor="#FFFFFF"
            android:textSize="9sp"
            android:paddingHorizontal="6dp" />

        <Button
            android:id="@+id/btn_toggle_play"
            android:layout_width="wrap_content"
            android:layout_height="32dp"
            android:layout_marginStart="6dp"
            android:backgroundTint="#10B981"
            android:text="▶ START"
            android:textColor="#FFFFFF"
            android:textSize="9sp"
            android:textStyle="bold"
            android:paddingHorizontal="8dp" />
    </LinearLayout>

</LinearLayout>
"""

with open('android_app/app/src/main/res/layout/floating_hud.xml', 'w') as f:
    f.write(floating_hud_xml)

# 4. Update settings.gradle.kts
settings_gradle = """pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "AAA-COC-AI-MARCO"
include(":app")
"""

with open('android_app/settings.gradle.kts', 'w') as f:
    f.write(settings_gradle)

# 5. Create .github/workflows/build-apk.yml
os.makedirs('.github/workflows', exist_ok=True)
workflow_yml = """name: Build AAA COC AI MARCO APK

on:
  push:
    branches: [ "main", "master" ]
  pull_request:
    branches: [ "main", "master" ]
  workflow_dispatch:

jobs:
  build-apk:
    name: Build Commercial APK on Cloud
    runs-on: ubuntu-latest

    steps:
      - name: Checkout Source Code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'

      - name: Setup Android SDK
        uses: android-actions/setup-android@v3

      - name: Grant Execute Permissions for Gradle Wrapper
        working-directory: android_app
        run: chmod +x gradlew || true

      - name: Compile Commercial APK
        working-directory: android_app
        run: ./gradlew assembleDebug --stacktrace --no-daemon

      - name: Upload AAA COC AI MARCO APK Artifact
        uses: actions/upload-artifact@v4
        with:
          name: AAA-COC-AI-MARCO-APK
          path: android_app/app/build/outputs/apk/debug/*.apk
          retention-days: 30

      - name: Send APK to Telegram Channel (Optional)
        if: success()
        env:
          TELEGRAM_BOT_TOKEN: ${{ secrets.TELEGRAM_BOT_TOKEN }}
          TELEGRAM_CHAT_ID: ${{ secrets.TELEGRAM_CHAT_ID }}
        run: |
          if [ -n "$TELEGRAM_BOT_TOKEN" ] && [ -n "$TELEGRAM_CHAT_ID" ]; then
            APK_FILE=$(find android_app/app/build/outputs/apk/debug -name "*.apk" | head -n 1)
            if [ -f "$APK_FILE" ]; then
              curl -F chat_id="${TELEGRAM_CHAT_ID}" \\
                   -F document=@"$APK_FILE" \\
                   -F caption="⚔️ AAA COC AI MARCO v2.0 Commercial APK Build Complete! (TH16/TH17 + Ores + Hero Equipment)" \\
                   https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendDocument
            fi
          fi
"""

with open('.github/workflows/build-apk.yml', 'w') as f:
    f.write(workflow_yml)

print("Applied commercial branding and GitHub cloud build workflow successfully.")
