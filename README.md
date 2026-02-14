# OpenClaw APK

A mobile Android launcher for OpenClaw AI assistant, built on Termux.

## Overview

This APK serves as a setup wizard and launcher for OpenClaw on Android devices. It provides:
- Termux detection and installation guidance
- One-click OpenClaw installation
- Easy launch access to OpenClaw
- Configuration options

## Prerequisites

- Android device running API 24+ (Android 7.0+)
- Termux app installed (available from F-Droid)
- At least 500MB of free storage

## Installation

### Method 1: Download Pre-built APK

1. Check the [Releases](https://github.com/openclaw/openclaw/releases) page
2. Download the latest `openclaw-apk-release.apk`
3. Enable "Install from unknown sources" in Android settings
4. Tap the APK to install

## Building on Orange Pi (ARM64)

### Option 1: Docker Build (Recommended)

Since Android Studio doesn't run on ARM64, we use Docker containers with Android SDK:

```bash
cd /home/orangepi/.openclaw/workspace/projects/openclaw-apk

# Make build script executable
chmod +x build-on-orangepi.sh

# Build APK (first build downloads ~2GB, takes 15-20 minutes)
./build-on-orangepi.sh
```

**What happens:**
1. Docker downloads Ubuntu with JDK
2. Installs Android SDK command-line tools
3. Accepts SDK licenses
4. Downloads Android platform 34 and build tools
5. Builds the APK

**Output:**
```
APK Location: app/build/outputs/apk/debug/app-debug.apk
```

### Option 2: Cloud Build (Faster Alternative)

Use a free CI/CD service to build the APK:

**GitHub Actions (recommended):**
1. Push the project to GitHub
2. Enable GitHub Actions
3. APK builds automatically on every push

See `.github/workflows/build-apk.yml` for the workflow file.

## Features

### Main Screen
- **Status indicator**: Shows installation status
  - 🔴 Red: Termux not installed
  - 🟡 Yellow: OpenClaw not installed
  - 🟢 Green: Ready to launch
- **Setup button**: Initiates OpenClaw installation
- **Launch button**: Opens OpenClaw in Termux
- **Help button**: Access documentation

### Setup Screen
- Installation progress tracking
- Copy installation script to clipboard
- Direct link to Termux
- 6-step installation process

### Settings Screen
- Customizable installation path
- Repository URL configuration
- Auto-start option
- HAL voice setup toggle

## Installation Process

The APK guides users through these steps:

1. **Update Termux packages** - Ensures latest dependencies
2. **Install Python, Git, Node.js** - Core requirements
3. **Clone OpenClaw repository** - Downloads the source
4. **Install npm dependencies** - Sets up Node.js packages
5. **Setup HAL voice** - Configures voice synthesis (optional)
6. **Complete** - Ready to launch

## Termux Setup

If Termux is not installed, the APK redirects users to F-Droid:

1. Tap "Install Termux"
2. Install from F-Droid (recommended)
3. Return to OpenClaw APK after installation
4. Complete OpenClaw setup

## Usage

### First Launch
1. Install Termux from F-Droid
2. Open OpenClaw APK
3. Tap "Set Up OpenClaw"
4. Follow the setup wizard
5. Tap "Launch OpenClaw" to start

### Regular Use
1. Open OpenClaw APK
2. Tap "Launch OpenClaw"
3. Use OpenClaw in Termux as normal

### Updating OpenClaw
1. Open OpenClaw APK
2. Tap "Update OpenClaw"
3. Script will pull latest changes

## Architecture

```
openclaw-apk/
├── app/
│   ├── src/main/
│   │   ├── java/com/openclaw/apk/
│   │   │   ├── OpenClawApplication.kt    # Application class
│   │   │   ├── model/
│   │   │   │   └── InstallationModels.kt # Data models
│   │   │   ├── ui/
│   │   │   │   ├── MainActivity.kt      # Main launcher
│   │   │   │   ├── SetupActivity.kt     # Installation wizard
│   │   │   │   ├── HelpActivity.kt      # Help & resources
│   │   │   │   └── SettingsActivity.kt  # Configuration
│   │   │   └── utils/
│   │   │       ├── TermuxUtils.kt       # Termux integration
│   │   │       ├── PreferencesManager.kt # Settings storage
│   │   │       └── ShellExecutor.kt      # Command execution
│   │   └── res/
│   │       ├── layout/                  # XML layouts
│   │       ├── values/                  # Strings, colors, themes
│   │       ├── drawable/                # Icons
│   │       └── menu/                    # Menus
│   └── build.gradle.kts                 # App-level build config
├── build.gradle.kts                     # Project-level build config
├── settings.gradle.kts
├── gradle.properties
└── gradlew                              # Gradle wrapper
```

## Dependencies

### AndroidX
- appcompat: Android compatibility
- material: Material Design components
- constraintlayout: Layout management
- lifecycle: Activity lifecycle
- preferences: Settings storage
- browser: URL opening

### Kotlin
- coroutines: Async operations
- All standard Kotlin libraries

## Configuration

### Default Settings
- **Installation Path**: `~/.openclaw`
- **Repository**: `https://github.com/openclaw/openclaw.git`
- **Auto-start**: Disabled
- **HAL Voice**: Enabled

### Custom Installation Path
Users can customize the installation location in Settings:
```
Settings → Installation Path → Enter custom path
```

### Custom Repository
For development or forks:
```
Settings → Repository URL → Enter custom git URL
```

## Troubleshooting

### Termux Not Opening
1. Verify Termux is installed from F-Droid
2. Check Termux permissions
3. Restart both apps

### Installation Fails
1. Ensure stable internet connection
2. Check available storage (500MB+)
3. Try manual installation via copied script

### OpenClaw Won't Start
1. Check Python installation in Termux
2. Verify all dependencies installed
3. Check OpenClaw logs in Termux

### APK Won't Install
1. Enable "Install from unknown sources"
2. Check Android security settings
3. Verify sufficient storage

## Development

### Adding New Features

1. Create new Activity in `ui/` package
2. Add layout XML in `res/layout/`
3. Update AndroidManifest.xml
4. Add navigation in existing Activities

### Modifying Installation Script

Edit `TermuxUtils.kt`:
```kotlin
fun generateInstallScript(config: InstallConfig): String {
    return """#!/bin/bash
        # Your custom installation steps
    """.trimMargin()
}
```

### Styling

Edit `res/values/themes.xml`:
```xml
<style name="Theme.OpenClawAPK" parent="Theme.Material3.Light.NoActionBar">
    <item name="colorPrimary">@color/primary</item>
    <!-- Customize colors -->
</style>
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License

OpenClaw APK is part of the OpenClaw project and follows the same license.

## Support

- **Discord**: [OpenClaw Community](https://discord.com/invite/clawd)
- **GitHub**: [Issues](https://github.com/openclaw/openclaw/issues)
- **Documentation**: [docs.openclaw.ai](https://docs.openclaw.ai)

---

Built with ❤️ by the OpenClaw Team
