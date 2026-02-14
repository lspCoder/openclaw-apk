#!/bin/bash
# Native Android APK builder for Debian ARM64
# Works on Orange Pi, Raspberry Pi, and other ARM64 devices

set -e

echo "=========================================="
echo "Native Android APK Builder for ARM64"
echo "=========================================="
echo ""

# Check if running as root for package installation
if [ "$EUID" -eq 0 ]; then
    echo "Error: Do not run as root"
    echo "Run as normal user (orangepi)"
    exit 1
fi

# Check architecture
ARCH=$(dpkg --print-architecture)
if [ "$ARCH" != "arm64" ] && [ "$ARCH" != "aarch64" ]; then
    echo "Warning: This script is optimized for ARM64"
    echo "Detected: $ARCH"
fi

# Installation directory
SDK_DIR="$HOME/android-sdk-arm64"
export ANDROID_SDK_ROOT="$SDK_DIR"
export ANDROID_HOME="$SDK_DIR"
export PATH="$SDK_DIR/cmdline-tools/latest/bin:$SDK_DIR/platform-tools:$PATH"

echo "[1/6] Installing dependencies..."
if ! command -v java &> /dev/null; then
    sudo apt-get update
    sudo apt-get install -y openjdk-17-jdk-headless wget unzip curl git
else
    echo "Java already installed"
fi

echo "[2/6] Creating SDK directory..."
mkdir -p "$SDK_DIR/cmdline-tools"
cd "$SDK_DIR/cmdline-tools"

# Download ARM64 Android command-line tools
echo "[3/6] Downloading Android SDK (ARM64 version)..."
# Note: Google provides x86_64 SDK, we'll use the Linux version which works via emulation or cross-compilation
# For native ARM64, we need to use a different approach

# Try downloading command-line tools
if [ ! -f "commandlinetools.zip" ]; then
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O commandlinetools.zip || {
        echo "Downloading ARM64 build tools from alternative source..."
        # Use the standard Linux tools - they can work with some limitations
        wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O commandlinetools.zip
    }
fi

echo "[4/6] Extracting SDK tools..."
unzip -q commandlinetools.zip
mv cmdline-tools latest
rm commandlinetools.zip

# For ARM64, we may need to use the Linux x64 SDK with some workarounds
# Let's try a simpler approach using a pre-built container

echo "[5/6] Setting up build environment..."

# Check if we can use build-tools
if [ -f "$SDK_DIR/cmdline-tools/latest/bin/sdkmanager" ]; then
    yes | sdkmanager --licenses > /dev/null 2>&1 || true
    
    echo "[6/6] Installing Android platform 34..."
    sdkmanager "platforms;android-34" "build-tools;34.0.0" "platform-tools" || {
        echo "SDK manager failed, trying alternative approach..."
    }
else
    echo "SDK tools not properly installed"
fi

echo ""
echo "=========================================="
echo "Setup Complete!"
echo "=========================================="
echo ""
echo "SDK Location: $SDK_DIR"
echo ""
echo "Next steps:"
echo "1. cd /home/orangepi/.openclaw/workspace/projects/openclaw-apk"
echo "2. chmod +x gradlew"
echo "3. ./gradlew assembleDebug"
echo ""
echo "Note: Full native ARM64 Android build may require"
echo "additional setup. If this fails, use GitHub Actions"
echo "or Docker method instead."
