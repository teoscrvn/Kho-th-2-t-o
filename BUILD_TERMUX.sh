#!/bin/bash
# ============================================================
#  SCRIPT BUILD APK - CHẠY TRONG TERMUX
#  Copy từng lệnh vào Termux, chạy từng bước một
# ============================================================

# ===== BƯỚC 1: Cài môi trường (chỉ làm 1 lần) =====

pkg update -y && pkg upgrade -y
pkg install -y openjdk-17
pkg install -y gradle
pkg install -y wget unzip git

# Kiểm tra Java
java -version

# ===== BƯỚC 2: Copy project vào Termux =====
# Tải file LockApp.zip từ Claude về điện thoại
# Rồi copy vào Termux:

# Nếu file ở Downloads:
cp /sdcard/Download/LockApp.zip ~/
cd ~
unzip LockApp.zip
cd LockApp

# ===== BƯỚC 3: Cấp quyền cho gradlew =====
chmod +x gradlew

# ===== BƯỚC 4: Tải Android SDK (cần làm 1 lần) =====
mkdir -p ~/android-sdk/cmdline-tools
cd ~/android-sdk/cmdline-tools
wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
unzip commandlinetools-linux-11076708_latest.zip
mv cmdline-tools latest

# Set biến môi trường
export ANDROID_HOME=~/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
export PATH=$PATH:$ANDROID_HOME/platform-tools

# Lưu vào .bashrc để khỏi gõ lại
echo 'export ANDROID_HOME=~/android-sdk' >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin' >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_HOME/platform-tools' >> ~/.bashrc

# ===== BƯỚC 5: Cài Android SDK platform =====
sdkmanager --sdk_root=$ANDROID_HOME "platform-tools"
sdkmanager --sdk_root=$ANDROID_HOME "platforms;android-34"
sdkmanager --sdk_root=$ANDROID_HOME "build-tools;34.0.0"
# Nhấn y khi hỏi license

# ===== BƯỚC 6: BUILD APK =====
cd ~/LockApp
./gradlew assembleDebug

# APK output tại:
# app/build/outputs/apk/debug/app-debug.apk

# Copy ra Downloads để cài:
cp app/build/outputs/apk/debug/app-debug.apk /sdcard/Download/LockApp.apk

echo "✅ BUILD XONG! File: /sdcard/Download/LockApp.apk"
echo "Mở Files, vào Downloads, bấm vào LockApp.apk để cài!"
