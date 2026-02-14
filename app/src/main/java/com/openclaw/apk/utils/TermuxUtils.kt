package com.openclaw.apk.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.util.Log

/**
 * Comprehensive utility class for checking and managing Termux installation
 * with robust error handling
 */
object TermuxUtils {

    private const val TAG = "TermuxUtils"
    private const val TERMUX_PACKAGE = "com.termux"
    private const val TERMUX_API_PACKAGE = "com.termux.api"
    private const val F_DROID_URL = "https://f-droid.org/packages/com.termux/"
    private const val TERMUX_WEBSITE = "https://termux.dev"
    private const val GITHUB_URL = "https://github.com/openclaw/openclaw"

    /**
     * Result wrapper for Termux operations
     */
    sealed class TermuxResult {
        data class Success(val message: String) : TermuxResult()
        data class Error(val message: String, val errorCode: ErrorCode = ErrorCode.UNKNOWN) : TermuxResult()
        data class Warning(val message: String, val suggestion: String) : TermuxResult()
    }

    /**
     * Error codes for detailed error handling
     */
    enum class ErrorCode {
        PACKAGE_NOT_FOUND,
        ACTIVITY_NOT_FOUND,
        NETWORK_ERROR,
        PERMISSION_DENIED,
        STORAGE_FULL,
        TIMEOUT,
        UNKNOWN
    }

    /**
     * Check if Termux is installed on the device
     */
    fun isTermuxInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(TERMUX_PACKAGE, 0)
            Log.d(TAG, "Termux is installed")
            true
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w(TAG, "Termux is NOT installed")
            false
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error checking Termux", e)
            false
        }
    }

    /**
     * Check if Termux:API is installed
     */
    fun isTermuxApiInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(TERMUX_API_PACKAGE, 0)
            Log.d(TAG, "Termux:API is installed")
            true
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w(TAG, "Termux:API is NOT installed")
            false
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error checking Termux:API", e)
            false
        }
    }

    /**
     * Get detailed installation status with error information
     */
    fun getDetailedStatus(context: Context): TermuxInstallStatus {
        val termuxInstalled = isTermuxInstalled(context)
        val termuxApiInstalled = isTermuxApiInstalled(context)

        val status = TermuxInstallStatus(
            isTermuxInstalled = termuxInstalled,
            isTermuxApiInstalled = termuxApiInstalled
        )

        Log.d(TAG, "Status: Termux=$termuxInstalled, API=$termuxApiInstalled")
        return status
    }

    /**
     * Open Termux app with comprehensive error handling
     */
    fun openTermux(context: Context): TermuxResult {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(TERMUX_PACKAGE)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                context.startActivity(intent)
                Log.d(TAG, "Successfully opened Termux")
                TermuxResult.Success("Opened Termux successfully")
            } else {
                Log.e(TAG, "Termux launch intent not found")
                TermuxResult.Error(
                    "Could not find Termux launcher",
                    ErrorCode.ACTIVITY_NOT_FOUND
                )
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied to open Termux", e)
            TermuxResult.Error(
                "Permission denied to open Termux. Please check app permissions.",
                ErrorCode.PERMISSION_DENIED
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error opening Termux", e)
            TermuxResult.Error(
                "Failed to open Termux: ${e.message}",
                ErrorCode.UNKNOWN
            )
        }
    }

    /**
     * Open F-Droid page for Termux with fallback
     */
    fun openTermuxOnF_Droid(context: Context): TermuxResult {
        return tryAllUrls(context) { url ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    /**
     * Open OpenClaw GitHub page
     */
    fun openOpenClawGitHub(context: Context): TermuxResult {
        return tryAllUrls(context) { url ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    /**
     * Helper function to try multiple URLs with fallbacks
     */
    private fun tryAllUrls(context: Context, action: (String) -> Unit): TermuxResult {
        val urls = listOf(F_DROID_URL, TERMUX_WEBSITE, GITHUB_URL)

        for (url in urls) {
            try {
                action(url)
                return TermuxResult.Success("Opened $url")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to open $url, trying next...")
                continue
            }
        }

        return TermuxResult.Error(
            "Could not open any browser",
            ErrorCode.NETWORK_ERROR
        )
    }

    /**
     * Check network connectivity
     */
    fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking network", e)
            true // Assume available on error
        }
    }

    /**
     * Validate installation path
     */
    fun validateInstallPath(path: String): TermuxResult {
        if (path.isBlank()) {
            return TermuxResult.Error(
                "Installation path cannot be empty",
                ErrorCode.UNKNOWN
            )
        }

        // Check for invalid characters
        val invalidChars = listOf(" ", ";", "&", "|", "$", "`", "(", ")", "{", "}", "[", "]", "<", ">")
        for (char in invalidChars) {
            if (path.contains(char)) {
                return TermuxResult.Error(
                    "Installation path contains invalid character: $char",
                    ErrorCode.UNKNOWN
                )
            }
        }

        // Check path length
        if (path.length > 255) {
            return TermuxResult.Error(
                "Installation path is too long (max 255 characters)",
                ErrorCode.UNKNOWN
            )
        }

        return TermuxResult.Success("Path is valid")
    }

    /**
     * Validate repository URL
     */
    fun validateCloneUrl(url: String): TermuxResult {
        if (url.isBlank()) {
            return TermuxResult.Error(
                "Repository URL cannot be empty",
                ErrorCode.UNKNOWN
            )
        }

        // Basic URL validation
        if (!url.startsWith("https://") && !url.startsWith("http://")) {
            return TermuxResult.Error(
                "URL must start with https:// or http://",
                ErrorCode.UNKNOWN
            )
        }

        if (!url.contains("://")) {
            return TermuxResult.Error(
                "Invalid URL format",
                ErrorCode.UNKNOWN
            )
        }

        return TermuxResult.Success("URL is valid")
    }

    /**
     * Generate the installation script for OpenClaw with error checking
     */
    fun generateInstallScript(config: InstallConfig): String {
        // Validate inputs
        val pathResult = validateInstallPath(config.installPath)
        val urlResult = validateCloneUrl(config.cloneUrl)

        if (pathResult is TermuxResult.Error) {
            Log.e(TAG, "Invalid install path: ${pathResult.message}")
        }
        if (urlResult is TermuxResult.Error) {
            Log.e(TAG, "Invalid clone URL: ${urlResult.message}")
        }

        return buildString {
            appendLine("#!/bin/bash")
            appendLine("# OpenClaw Installation Script for Termux")
            appendLine("# Generated by OpenClaw APK")
            appendLine("#")
            appendLine("# This script installs OpenClaw AI assistant")
            appendLine("# Run this in Termux after installation")
            appendLine()
            appendLine("set -e  # Exit on error")
            appendLine()
            appendLine("# Colors for output")
            appendLine("RED='\\033[0;31m'")
            appendLine("GREEN='\\033[0;32m'")
            appendLine("YELLOW='\\033[1;33m'")
            appendLine("NC='\\033[0m' # No Color")
            appendLine()
            appendLine("echo -e '${'$'}{GREEN}==========================================${'$'}{NC}'")
            appendLine("echo 'OpenClaw Installation Script'")
            appendLine("echo -e '${'$'}{GREEN}==========================================${'$'}{NC}'")
            appendLine()
            appendLine("# Error handling function")
            appendLine("error_exit() {")
            appendLine("    echo -e '${'$'}{RED}Error: ${'$'}1${'$'}{NC}' >&2")
            appendLine("    exit 1")
            appendLine("}")
            appendLine()
            appendLine("# Check if running in Termux")
            appendLine("if [ ! -d \"${'$'}HOME/.termux\" ]; then")
            appendLine("    error_exit 'This script must be run in Termux!'")
            appendLine("fi")
            appendLine()
            appendLine("# Check network connectivity")
            appendLine("if ! ping -c 1 google.com > /dev/null 2>&1; then")
            appendLine("    echo -e '${'$'}{YELLOW}Warning: No internet connection${'$'}{NC}'")
            appendLine("    echo 'Some downloads may fail'")
            appendLine("fi")
            appendLine()
            appendLine("# Update Termux packages")
            appendLine("echo -e '${'$'}{YELLOW}[1/6] Updating Termux packages...${'$'}{NC}'")
            appendLine("pkg update -y || error_exit 'Failed to update packages'")
            appendLine("pkg upgrade -y || error_exit 'Failed to upgrade packages'")
            appendLine()
            appendLine("# Install Python and required packages")
            appendLine("echo -e '${'$'}{YELLOW}[2/6] Installing Python and dependencies...${'$'}{NC}'")
            appendLine("pkg install -y python python-pip git nodejs npm || error_exit 'Failed to install packages'")
            appendLine("pip install --upgrade pip || echo 'Warning: pip upgrade failed, continuing...'")
            appendLine()
            appendLine("# Install pip dependencies")
            appendLine("echo -e '${'$'}{YELLOW}[3/6] Installing Python packages...${'$'}{NC}'")
            appendLine("pip install requests beautifulsoup4 schedule || error_exit 'Failed to install Python packages'")
            appendLine()
            appendLine("# Clone or update OpenClaw repository")
            appendLine("echo -e '${'$'}{YELLOW}[4/6] ${'$'}{config.cloneUrl}${'$'}{NC}'")
            appendLine("if [ -d \"${'$'}${config.installPath}/.git\" ]; then")
            appendLine("    echo 'Updating existing installation...'")
            appendLine("    cd \"${'$'}${config.installPath}\"")
            appendLine("    git pull || echo 'Warning: git pull failed, using existing installation'")
            appendLine("else")
            appendLine("    echo 'Cloning OpenClaw repository...'")
            appendLine("    git clone ${config.cloneUrl} \"${'$'}${config.installPath}\" || error_exit 'Failed to clone repository'")
            appendLine("fi")
            appendLine()
            appendLine("# Install npm dependencies")
            appendLine("echo -e '${'$'}{YELLOW}[5/6] Installing npm dependencies...${'$'}{NC}'")
            appendLine("cd \"${'$'}${config.installPath}\"")
            appendLine("npm install || error_exit 'Failed to install npm packages'")
            appendLine()
            appendLine("# Setup HAL voice (if enabled)")
            appendLine("if [ \"${'$'}${config.setupVoice}\" = \"true\" ]; then")
            appendLine("    echo -e '${'$'}{YELLOW}[6/6] Setting up HAL voice...${'$'}{NC}'")
            appendLine("    if [ -f \"scripts/hal_voice.py\" ]; then")
            appendLine("        python3 scripts/hal_voice.py --setup || echo 'Warning: HAL voice setup failed'")
            appendLine("    else")
            appendLine("        echo 'HAL voice script not found, skipping...'")
            appendLine("    fi")
            appendLine("else")
            appendLine("    echo -e '${'$'}{YELLOW}[6/6] Skipping HAL voice setup...${'$'}{NC}'")
            appendLine("fi")
            appendLine()
            appendLine("echo -e '${'$'}{GREEN}==========================================${'$'}{NC}'")
            appendLine("echo 'OpenClaw installation completed!'")
            appendLine("echo -e '${'$'}{GREEN}==========================================${'$'}{NC}'")
            appendLine()
            appendLine("echo 'To run OpenClaw:'")
            appendLine("echo \"  cd ${'$'}${config.installPath}\"")
            appendLine("echo '  python3 main.py'")
            appendLine()
            appendLine("echo -e '${'$'}{GREEN}Enjoy using OpenClaw!${'$'}{NC}'")
        }
    }

    /**
     * Generate the run script for OpenClaw
     */
    fun generateRunScript(installPath: String = "~/.openclaw"): String {
        val validation = validateInstallPath(installPath)
        if (validation is TermuxResult.Error) {
            Log.w(TAG, "Invalid install path: ${validation.message}")
        }

        return buildString {
            appendLine("#!/bin/bash")
            appendLine("# OpenClaw Run Script")
            appendLine("# Generated by OpenClaw APK")
            appendLine()
            appendLine("set -e")
            appendLine()
            appendLine("INSTALL_PATH=\"$installPath\"")
            appendLine()
            appendLine("# Check if OpenClaw is installed")
            appendLine("if [ ! -d \"$INSTALL_PATH\" ]; then")
            appendLine("    echo 'OpenClaw is not installed!'")
            appendLine("    echo 'Please run the installation script first.'")
            appendLine("    exit 1")
            appendLine("fi")
            appendLine()
            appendLine("# Check if main.py exists")
            appendLine("if [ ! -f \"$INSTALL_PATH/main.py\" ]; then")
            appendLine("    echo 'OpenClaw main.py not found!'")
            appendLine("    echo 'Please reinstall OpenClaw.'")
            appendLine("    exit 1")
            appendLine("fi")
            appendLine()
            appendLine("cd \"$INSTALL_PATH\"")
            appendLine("python3 main.py")
        }
    }
}

/**
 * Data class for Termux installation status
 */
data class TermuxInstallStatus(
    val isTermuxInstalled: Boolean,
    val isTermuxApiInstalled: Boolean
) {
    val allInstalled: Boolean
        get() = isTermuxInstalled && isTermuxApiInstalled

    val missingPackages: List<String>
        get() = buildList {
            if (!isTermuxInstalled) add("Termux")
            if (!isTermuxApiInstalled) add("Termux:API")
        }
}
