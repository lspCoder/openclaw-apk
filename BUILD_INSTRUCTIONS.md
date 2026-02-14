# OpenClaw APK Build Instructions

## Quick Start (Android Studio)

1. **Download Android Studio**
   - Visit: https://developer.android.com/studio
   - Install the latest version for your OS

2. **Clone and Open**
   ```bash
   git clone https://github.com/openclaw/openclaw.git
   cd openclaw/projects/openclaw-apk
   ```
   Then open this folder in Android Studio.

3. **Build APK**
   - Wait for Gradle sync (may take a few minutes)
   - Build → Build Bundle(s) / APK(s) → Build APK(s)
   - APK located at: `app/build/outputs/apk/debug/app-debug.apk`

## Command Line Build

### Linux/macOS

```bash
# Navigate to project
cd /path/to/openclaw-apk

# Make gradlew executable
chmod +x gradlew

# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing)
./gradlew assembleRelease
```

### Windows

```powershell
# Open PowerShell or Command Prompt
cd path\to\openclaw-apk

# Build debug APK
gradlew.bat assembleDebug

# Build release APK
gradlew.bat assembleRelease
```

## Gradle Configuration

### System Requirements
- **JDK**: 17 or higher
- **Gradle**: 8.0+
- **Android SDK**: API 24+ (Android 7.0)

### Environment Variables

```bash
export JAVA_HOME=/path/to/jdk-17
export ANDROID_HOME=/path/to/android-sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/tools
```

### Android SDK Setup

1. Install Android SDK command-line tools
2. Accept licenses:
   ```bash
   yes | sdkmanager --licenses
   ```
3. Install required SDK:
   ```bash
   sdkmanager "platforms;android-34" "build-tools;34.0.0"
   ```

## Signing Release APK

### 1. Generate Signing Key

```bash
keytool -genkeypair \
    -v -keystore openclaw-release-key.jks \
    -keyalg RSA -keysize 2048 -validity 10000 \
    -alias openclaw-key
```

### 2. Configure Signing

Edit `app/build.gradle.kts`:

```kotlin
android {
    signingConfigs {
        create("release") {
            keyAlias = "openclaw-key"
            keyPassword = "your-key-password"
            storeFile = file("openclaw-release-key.jks")
            storePassword = "your-store-password"
        }
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

### 3. Build Signed APK

```bash
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

## CI/CD Build (GitHub Actions)

Example workflow:

```yaml
name: Build APK

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup JDK
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
          
      - name: Setup Android SDK
        uses: android-actions/setup-android@v2
        
      - name: Build APK
        run: ./gradlew assembleDebug
        
      - name: Upload APK
        uses: actions/upload-artifact@v3
        with:
          name: openclaw-apk
          path: app/build/outputs/apk/debug/
```

## Troubleshooting

### Gradle Sync Fails
- Check JAVA_HOME is set correctly
- Ensure JDK 17+ is installed
- Delete `.gradle` folder and retry

### Android SDK Not Found
- Set ANDROID_HOME environment variable
- Accept SDK licenses: `yes | sdkmanager --licenses`

### Build Errors
- Clean project: `./gradlew clean`
- Invalidate caches: File → Invalidate Caches in Android Studio
- Check `build.gradle.kts` for typos

### Slow Builds
- Enable Gradle daemon
- Use parallel execution: `org.gradle.parallel=true`
- Increase JVM memory: `org.gradle.jvmargs=-Xmx4g`

## Version Information

- **minSdk**: 24 (Android 7.0)
- **targetSdk**: 34 (Android 14)
- **Compile SDK**: 34
- **Kotlin**: 1.9.0
- **Gradle**: 8.0
- **Android Gradle Plugin**: 8.1.0

## Output Files

| Build Type | Location | Size |
|------------|----------|------|
| Debug | `app/build/outputs/apk/debug/app-debug.apk` | ~10MB |
| Release | `app/build/outputs/apk/release/app-release.apk` | ~5MB |
| App Bundle | `app/build/outputs/bundle/release/app-release.aab` | ~10MB |

## Additional Resources

- Android Developer Docs: https://developer.android.com/docs
- Gradle Documentation: https://docs.gradle.org
- Kotlin Documentation: https://kotlinlang.org/docs
