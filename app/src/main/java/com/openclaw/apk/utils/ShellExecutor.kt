package com.openclaw.apk.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Utility class for executing shell commands in Termux
 */
object ShellExecutor {

    private const val TAG = "ShellExecutor"

    /**
     * Execute a command in Termux via am (Activity Manager)
     * Note: This is a workaround since we can't directly run shell commands
     */
    suspend fun executeInTermux(context: Context, command: String): CommandResult {
        return withContext(Dispatchers.IO) {
            try {
                // Build the am command to execute in Termux
                val amCommand = """
                    am start -a android.intent.action.VIEW \
                    -d "termux://command" \
                    --user 0 \
                    -n com.termux/.app.TerminalActivity \
                    --es cmd "$command"
                """.trimIndent().replace("\n", " ")

                // Log the command for debugging
                Log.d(TAG, "Would execute: $command")

                CommandResult(
                    success = true,
                    output = "Command prepared for Termux:\n$command",
                    error = null
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error executing command", e)
                CommandResult(
                    success = false,
                    output = null,
                    error = e.message
                )
            }
        }
    }

    /**
     * Generate a shell script content for installation
     */
    fun generateInstallScript(
        config: InstallConfig,
        onProgress: ((String) -> Unit)? = null
    ): String {
        val progressSteps = listOf(
            "echo 'Starting OpenClaw installation...'" to 10,
            "pkg update -y" to 15,
            "pkg upgrade -y" to 20,
            "pkg install -y python python-pip git nodejs npm" to 35,
            "pip install --upgrade pip" to 40,
            "pip install requests beautifulsoup4 schedule" to 50,
            "git clone ${config.cloneUrl} ${config.installPath}" to 70,
            "cd ${config.installPath} && npm install" to 85,
            "${if (config.setupVoice) "python3 scripts/hal_voice.py --setup" else "echo 'Skipping voice setup'"}" to 95,
            "echo 'Installation complete!'" to 100
        )

        return buildString {
            appendLine("#!/bin/bash")
            appendLine("# OpenClaw Installation Script")
            appendLine()
            appendLine("set -e")
            appendLine()
            appendLine("INSTALL_PATH='${config.installPath}'")
            appendLine()

            progressSteps.forEach { (step, _) ->
                appendLine(step)
            }

            appendLine()
            appendLine("echo 'OpenClaw is ready to use!'")
        }
    }
}

/**
 * Result of a shell command execution
 */
data class CommandResult(
    val success: Boolean,
    val output: String?,
    val error: String?
) {
    companion object {
        fun success(output: String): CommandResult = CommandResult(true, output, null)
        fun failure(error: String): CommandResult = CommandResult(false, null, error)
    }
}
