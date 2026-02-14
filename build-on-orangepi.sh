#!/bin/bash
# Simple Docker-based APK builder for Orange Pi

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

IMAGE_NAME="openclaw-android-builder"

echo "=========================================="
echo "OpenClaw APK Builder for ARM64"
echo "=========================================="

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "Error: Docker is not running or not accessible"
    exit 1
fi

# Build the image (if not exists)
if ! docker image inspect "$IMAGE_NAME" > /dev/null 2>&1; then
    echo ""
    echo "Building Docker image (this may take 10-15 minutes)..."
    docker build -t "$IMAGE_NAME" .
    echo "Image built successfully!"
else
    echo "Using cached Docker image: $IMAGE_NAME"
fi

echo ""
echo "Building APK..."
echo ""

# Run the build
docker run --rm \
    -v "$SCRIPT_DIR:/workspace" \
    -w /workspace \
    -e GRADLE_USER_HOME=/workspace/.gradle \
    "$IMAGE_NAME" \
    bash -c "chmod +x gradlew && ./gradlew assembleDebug --no-daemon --stacktrace"

echo ""
echo "=========================================="
echo "Build Complete!"
echo "=========================================="
echo ""
echo "APK Location: $SCRIPT_DIR/app/build/outputs/apk/debug/app-debug.apk"
echo ""
echo "To install on your phone:"
echo "1. Transfer the APK to your Android device"
echo "2. Enable 'Install from unknown sources'"
echo "3. Tap the APK to install"
