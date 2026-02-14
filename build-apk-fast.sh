#!/bin/bash
# Build APK using pre-built Docker image (faster)

set -e

# Get project directory
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$PROJECT_DIR"

echo "Using pre-built Android SDK Docker image..."

# Run build in Docker (using a pre-built image)
docker run --rm -v "$PROJECT_DIR:/app" \
    -w /app \
    ghcr.io/budtmo/docker-android:x86_64-11.0 \
    bash -c "apt-get update && apt-get install -y git && chmod +x gradlew && ./gradlew assembleDebug --no-daemon"

# Copy APK to host
echo ""
echo "Build complete!"
echo "APK location: app/build/outputs/apk/debug/app-debug.apk"
