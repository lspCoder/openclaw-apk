package com.openclaw.apk

import android.app.Application
import android.util.Log

/**
 * Main Application class for OpenClaw APK
 */
class OpenClawApplication : Application() {

    companion object {
        private const val TAG = "OpenClawAPK"

        @Volatile
        private var instance: OpenClawApplication? = null

        fun getInstance(): OpenClawApplication {
            return instance ?: throw IllegalStateException("Application not initialized")
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.d(TAG, "OpenClaw APK Application started")
    }
}
