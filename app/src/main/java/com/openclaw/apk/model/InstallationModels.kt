package com.openclaw.apk.model

/**
 * Represents the installation status of OpenClaw with comprehensive tracking
 */
data class InstallationStatus(
    val termuxInstalled: Boolean = false,
    val openClawInstalled: Boolean = false,
    val pythonInstalled: Boolean = false,
    val nodeJsInstalled: Boolean = false,
    val dependenciesReady: Boolean = false,
    val lastCheckTime: Long = System.currentTimeMillis()
) {
    val isReadyToRun: Boolean
        get() = termuxInstalled && openClawInstalled && dependenciesReady

    val progressPercentage: Int
        get() {
            var progress = 0
            if (termuxInstalled) progress += 20
            if (pythonInstalled) progress += 15
            if (nodeJsInstalled) progress += 15
            if (openClawInstalled) progress += 25
            if (dependenciesReady) progress += 25
            return progress
        }

    val completedSteps: List<String>
        get() = buildList {
            if (termuxInstalled) add("Termux installed")
            if (pythonInstalled) add("Python installed")
            if (nodeJsInstalled) add("Node.js installed")
            if (openClawInstalled) add("OpenClaw installed")
            if (dependenciesReady) add("Dependencies ready")
        }

    val pendingSteps: List<String>
        get() = buildList {
            if (!termuxInstalled) add("Install Termux")
            if (!pythonInstalled) add("Install Python")
            if (!nodeJsInstalled) add("Install Node.js")
            if (!openClawInstalled) add("Install OpenClaw")
            if (!dependenciesReady) add("Setup dependencies")
        }

    val statusMessage: StatusMessage
        get() = when {
            !termuxInstalled -> StatusMessage.TERMUX_REQUIRED
            !openClawInstalled -> StatusMessage.SETUP_NEEDED
            !dependenciesReady -> StatusMessage.INCOMPLETE
            else -> StatusMessage.READY
        }
}

/**
 * Status messages for UI display
 */
enum class StatusMessage {
    TERMUX_REQUIRED,
    SETUP_NEEDED,
    INCOMPLETE,
    READY
}

/**
 * Represents a step in the installation process
 */
data class InstallStep(
    val id: Int,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val isInProgress: Boolean = false,
    val hasError: Boolean = false,
    val errorMessage: String? = null,
    val canRetry: Boolean = false
) {
    val status: StepStatus
        get() = when {
            hasError && canRetry -> StepStatus.ERROR_RETRY
            hasError -> StepStatus.ERROR
            isInProgress -> StepStatus.IN_PROGRESS
            isCompleted -> StepStatus.COMPLETED
            else -> StepStatus.PENDING
        }
}

/**
 * Step status enumeration
 */
enum class StepStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    ERROR,
    ERROR_RETRY
}

/**
 * Configuration for the installation
 */
data class InstallConfig(
    val cloneUrl: String = "https://github.com/openclaw/openclaw.git",
    val installPath: String = "~/.openclaw",
    val autoStart: Boolean = false,
    val setupVoice: Boolean = true,
    val installPython: Boolean = true,
    val installNodeJs: Boolean = true
) {
    /**
     * Validate configuration
     */
    fun validate(): ValidationResult {
        val errors = mutableListOf<String>()

        if (cloneUrl.isBlank()) {
            errors.add("Repository URL cannot be empty")
        } else if (!cloneUrl.startsWith("https://") && !cloneUrl.startsWith("http://")) {
            errors.add("Repository URL must start with https:// or http://")
        }

        if (installPath.isBlank()) {
            errors.add("Installation path cannot be empty")
        } else if (installPath.contains(" ") || installPath.contains(";")) {
            errors.add("Installation path contains invalid characters")
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
}

/**
 * Validation result wrapper
 */
sealed class ValidationResult {
    data object Valid : ValidationResult()
    data class Invalid(val errors: List<String>) : ValidationResult()
}

/**
 * Represents an error that occurred during installation
 */
data class InstallationError(
    val stepId: Int,
    val title: String,
    val message: String,
    val suggestion: String,
    val canRetry: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Installation log entry
 */
data class InstallLogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel,
    val message: String
)

/**
 * Log levels for installation
 */
enum class LogLevel {
    INFO,
    SUCCESS,
    WARNING,
    ERROR
}
