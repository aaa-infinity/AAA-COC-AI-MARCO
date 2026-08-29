import os

activity_main = """<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="#0F172A"
    android:padding="24dp">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="CoC AI AutoClicker"
        android:textColor="#F8FAFC"
        android:textSize="26sp"
        android:textStyle="bold" />

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:text="On-Device Macro &amp; Vision Engine for Modern Clash of Clans (Ores, Hero Equipment, 0-Cost Training, Root Riders &amp; Builder Base 2.0)"
        android:textColor="#94A3B8"
        android:textSize="13sp" />

    <View
        android:layout_width="match_parent"
        android:layout_height="1dp"
        android:layout_marginVertical="20dp"
        android:background="#334155" />

    <!-- Status Cards -->
    <TextView
        android:id="@+id/tv_accessibility_status"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="#1E293B"
        android:padding="12dp"
        android:text="Accessibility Service: CHECKING..."
        android:textColor="#F59E0B"
        android:textSize="14sp" />

    <TextView
        android:id="@+id/tv_overlay_status"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:background="#1E293B"
        android:padding="12dp"
        android:text="Overlay Permission: CHECKING..."
        android:textColor="#F59E0B"
        android:textSize="14sp" />

    <!-- Action Buttons -->
    <Button
        android:id="@+id/btn_grant_accessibility"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="20dp"
        android:backgroundTint="#3B82F6"
        android:text="1. Grant Accessibility Permission"
        android:textColor="#FFFFFF" />

    <Button
        android:id="@+id/btn_grant_overlay"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:backgroundTint="#3B82F6"
        android:text="2. Grant Floating Overlay Permission"
        android:textColor="#FFFFFF" />

    <Button
        android:id="@+id/btn_start_floating_hud"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="24dp"
        android:backgroundTint="#10B981"
        android:padding="14dp"
        android:text="🚀 Launch Floating Widget"
        android:textColor="#FFFFFF"
        android:textSize="16sp"
        android:textStyle="bold" />

</LinearLayout>
"""

floating_hud = """<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:background="#DD1E293B"
    android:padding="8dp"
    android:gravity="center_vertical">

    <TextView
        android:id="@+id/tv_hud_status"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Status: IDLE"
        android:textColor="#F8FAFC"
        android:textSize="12sp"
        android:textStyle="bold"
        android:paddingEnd="8dp" />

    <Button
        android:id="@+id/btn_strategy"
        android:layout_width="wrap_content"
        android:layout_height="36dp"
        android:backgroundTint="#6366F1"
        android:text="SNEAKY GOBLIN"
        android:textColor="#FFFFFF"
        android:textSize="11sp" />

    <Button
        android:id="@+id/btn_toggle_play"
        android:layout_width="wrap_content"
        android:layout_height="36dp"
        android:layout_marginStart="6dp"
        android:backgroundTint="#10B981"
        android:text="▶ START"
        android:textColor="#FFFFFF"
        android:textSize="11sp"
        android:textStyle="bold" />

</LinearLayout>
"""

with open('android_app/app/src/main/res/layout/activity_main.xml', 'w') as f:
    f.write(activity_main)

with open('android_app/app/src/main/res/layout/floating_hud.xml', 'w') as f:
    f.write(floating_hud)

# Root build.gradle.kts
root_gradle = """// Top-level build file
plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
}
"""

# app build.gradle.kts
app_gradle = """plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.cocai.autoclicker"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.cocai.autoclicker"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
}
"""

# settings.gradle.kts
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
rootProject.name = "CoC-AI-AutoClicker"
include(":app")
"""

with open('android_app/build.gradle.kts', 'w') as f:
    f.write(root_gradle)

with open('android_app/app/build.gradle.kts', 'w') as f:
    f.write(app_gradle)

with open('android_app/settings.gradle.kts', 'w') as f:
    f.write(settings_gradle)

print("Created layout and Gradle build files successfully.")
