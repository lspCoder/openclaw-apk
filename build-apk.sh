#!/bin/bash
# Build APK using Docker

set -e

# Get project directory
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$PROJECT_DIR"

# Build Docker image
echo "Building Android build environment..."
docker build -t openclaw-apk-builder .

# Run build in Docker
echo "Building APK..."
docker run --rm -v "$PROJECT_DIR:/project" \
    -w /project \
    openclaw-apk-builder \
    bash -c "chmod +x gradlew && ./gradlew assembleDebug --no-daemon"

# Copy APK to host
echo ""
echo "Build complete!"
echo "APK location: app/build/outputs/apk/debug/app-debug.apk"
