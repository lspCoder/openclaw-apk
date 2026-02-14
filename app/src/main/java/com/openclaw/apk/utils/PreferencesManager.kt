package com.openclaw.apk.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * SharedPreferences manager for OpenClaw APK
 */
class PreferencesManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "openclaw_apk_prefs"

        // Keys
        private const val KEY_TERMUX_INSTALLED = "termux_installed"
        private const val KEY_OPENCLAW_INSTALLED = "openclaw_installed"
        private const val KEY_INSTALL_PATH = "install_path"
        private const val KEY_CLONE_URL = "clone_url"
        private const val KEY_AUTO_START = "auto_start"
        private const val KEY_SETUP_VOICE = "setup_voice"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_LAST_UPDATE_CHECK = "last_update_check"
        private const val KEY_INSTALLATION_STATUS = "installation_status"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isTermuxInstalled: Boolean
        get() = prefs.getBoolean(KEY_TERMUX_INSTALLED, false)
        set(value) = prefs.edit { putBoolean(KEY_TERMUX_INSTALLED, value) }

    var isOpenClawInstalled: Boolean
        get() = prefs.getBoolean(KEY_OPENCLAW_INSTALLED, false)
        set(value) = prefs.edit { putBoolean(KEY_OPENCLAW_INSTALLED, value) }

    var installPath: String
        get() = prefs.getString(KEY_INSTALL_PATH, "~/.openclaw") ?: "~/.openclaw"
        set(value) = prefs.edit { putString(KEY_INSTALL_PATH, value) }

    var cloneUrl: String
        get() = prefs.getString(KEY_CLONE_URL, "https://github.com/openclaw/openclaw.git") ?: "https://github.com/openclaw/openclaw.git"
        set(value) = prefs.edit { putString(KEY_CLONE_URL, value) }

    var autoStart: Boolean
        get() = prefs.getBoolean(KEY_AUTO_START, false)
        set(value) = prefs.edit { putBoolean(KEY_AUTO_START, value) }

    var setupVoice: Boolean
        get() = prefs.getBoolean(KEY_SETUP_VOICE, true)
        set(value) = prefs.edit { putBoolean(KEY_SETUP_VOICE, value) }

    var isFirstLaunch: Boolean
        get() = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        set(value) = prefs.edit { putBoolean(KEY_FIRST_LAUNCH, value) }

    var lastUpdateCheck: Long
        get() = prefs.getLong(KEY_LAST_UPDATE_CHECK, 0)
        set(value) = prefs.edit { putLong(KEY_LAST_UPDATE_CHECK, value) }

    var installationStatus: String
        get() = prefs.getString(KEY_INSTALLATION_STATUS, "not_started") ?: "not_started"
        set(value) = prefs.edit { putString(KEY_INSTALLATION_STATUS, value) }

    /**
     * Reset all preferences
     */
    fun reset() {
        prefs.edit { clear() }
    }

    /**
     * Get installation config
     */
    fun getInstallConfig(): InstallConfig {
        return InstallConfig(
            cloneUrl = cloneUrl,
            installPath = installPath,
            autoStart = autoStart,
            setupVoice = setupVoice
        )
    }

    /**
     * Save installation config
     */
    fun saveInstallConfig(config: InstallConfig) {
        prefs.edit {
            putString(KEY_CLONE_URL, config.cloneUrl)
            putString(KEY_INSTALL_PATH, config.installPath)
            putBoolean(KEY_AUTO_START, config.autoStart)
            putBoolean(KEY_SETUP_VOICE, config.setupVoice)
        }
    }
}

/**
 * Extension function to get PreferencesManager from Context
 */
val Context.preferencesManager: PreferencesManager
    get() = PreferencesManager(this)
