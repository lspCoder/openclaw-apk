package com.openclaw.apk.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.openclaw.apk.R
import com.openclaw.apk.databinding.ActivityMainBinding
import com.openclaw.apk.model.InstallationStatus
import com.openclaw.apk.model.StatusMessage
import com.openclaw.apk.utils.PreferencesManager
import com.openclaw.apk.utils.TermuxUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Main Activity - Entry point for OpenClaw APK
 * Provides comprehensive error handling and user feedback
 */
class MainActivity : AppCompatActivity() {

    private const val TAG = "MainActivity"

    private lateinit var binding: ActivityMainBinding
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "MainActivity onCreate")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesManager = PreferencesManager(this)

        setupToolbar()
        setupViews()
        checkInstallationStatus()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "MainActivity onResume - refreshing status")
        checkInstallationStatus()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)
    }

    private fun setupViews() {
        // Setup button clicks with error handling
        binding.btnInstall.setOnClickListener {
            handleInstallClick()
        }

        binding.btnLaunch.setOnClickListener {
            handleLaunchClick()
        }

        binding.btnHelp.setOnClickListener {
            handleHelpClick()
        }

        binding.cardStatus.setOnClickListener {
            checkInstallationStatus()
        }

        // Progress bar setup
        binding.progressBar.max = 100
        binding.progressBar.isIndeterminate = false
    }

    private fun handleInstallClick() {
        val termuxInstalled = TermuxUtils.isTermuxInstalled(this)

        if (!termuxInstalled) {
            // Guide user to install Termux
            showTermuxRequiredDialog()
        } else {
            // Open setup activity
            startActivity(Intent(this, SetupActivity::class.java))
        }
    }

    private fun handleLaunchClick() {
        val result = TermuxUtils.openTermux(this)

        when (result) {
            is TermuxUtils.TermuxResult.Success -> {
                showLaunchDialog()
            }
            is TermuxUtils.TermuxResult.Error -> {
                showErrorDialog(result.message, result.errorCode.name)
                Log.e(TAG, "Launch error: ${result.message} (${result.errorCode})")
            }
            is TermuxUtils.TermuxResult.Warning -> {
                showWarningDialog(result.message, result.suggestion)
            }
        }
    }

    private fun handleHelpClick() {
        try {
            startActivity(Intent(this, HelpActivity::class.java))
        } catch (e: Exception) {
            Log.e(TAG, "Error opening help", e)
            showSnackbar(getString(R.string.error_generic))
        }
    }

    private fun checkInstallationStatus() {
        lifecycleScope.launch {
            try {
                val status = getInstallationStatus()
                updateUI(status)
            } catch (e: Exception) {
                Log.e(TAG, "Error checking installation status", e)
                showErrorStatus()
            }
        }
    }

    private suspend fun getInstallationStatus(): InstallationStatus {
        return withContext(Dispatchers.IO) {
            val termuxInstalled = TermuxUtils.isTermuxInstalled(this@MainActivity)
            val termuxApiInstalled = TermuxUtils.isTermuxApiInstalled(this@MainActivity)
            val isOpenClawInstalled = preferencesManager.isOpenClawInstalled

            Log.d(TAG, "Status: Termux=$termuxInstalled, API=$termuxApiInstalled, OpenClaw=$isOpenClawInstalled")

            InstallationStatus(
                termuxInstalled = termuxInstalled,
                openClawInstalled = isOpenClawInstalled,
                pythonInstalled = termuxInstalled, // Assumed if Termux is installed
                nodeJsInstalled = termuxInstalled,  // Assumed if Termux is installed
                dependenciesReady = isOpenClawInstalled
            )
        }
    }

    private fun updateUI(status: InstallationStatus) {
        Log.d(TAG, "Updating UI with status: ${status.statusMessage}")

        binding.apply {
            progressBar.progress = status.progressPercentage

            when (status.statusMessage) {
                StatusMessage.TERMUX_REQUIRED -> {
                    tvStatusTitle.text = getString(R.string.status_termux_required)
                    tvStatusDescription.text = getString(R.string.status_termux_description)
                    btnInstall.text = getString(R.string.btn_install_termux)
                    btnInstall.isEnabled = true
                    btnLaunch.visibility = View.GONE
                    ivStatusIcon.setImageResource(R.drawable.ic_warning)
                    progressBar.visibility = View.VISIBLE
                    progressBar.isIndeterminate = false
                }
                StatusMessage.SETUP_NEEDED -> {
                    tvStatusTitle.text = getString(R.string.status_setup_needed)
                    tvStatusDescription.text = getString(R.string.status_setup_description)
                    btnInstall.text = getString(R.string.btn_setup_openclaw)
                    btnInstall.isEnabled = true
                    btnLaunch.visibility = View.GONE
                    ivStatusIcon.setImageResource(R.drawable.ic_setup)
                    progressBar.visibility = View.VISIBLE
                    progressBar.isIndeterminate = false
                }
                StatusMessage.INCOMPLETE -> {
                    tvStatusTitle.text = getString(R.string.status_incomplete)
                    tvStatusDescription.text = getString(R.string.status_incomplete_description)
                    btnInstall.text = getString(R.string.btn_complete_setup)
                    btnInstall.isEnabled = true
                    btnLaunch.visibility = View.GONE
                    ivStatusIcon.setImageResource(R.drawable.ic_warning)
                    progressBar.visibility = View.VISIBLE
                    progressBar.isIndeterminate = false
                }
                StatusMessage.READY -> {
                    tvStatusTitle.text = getString(R.string.status_ready)
                    tvStatusDescription.text = getString(R.string.status_ready_description)
                    btnInstall.text = getString(R.string.btn_update)
                    btnInstall.isEnabled = true
                    btnLaunch.visibility = View.VISIBLE
                    ivStatusIcon.setImageResource(R.drawable.ic_check_circle)
                    progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun showErrorStatus() {
        binding.apply {
            tvStatusTitle.text = getString(R.string.status_error)
            tvStatusDescription.text = getString(R.string.status_error_description)
            btnInstall.text = getString(R.string.btn_retry)
            btnInstall.isEnabled = true
            btnLaunch.visibility = View.GONE
            ivStatusIcon.setImageResource(R.drawable.ic_warning)
            progressBar.visibility = View.GONE
        }
    }

    private fun showTermuxRequiredDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_termux_title)
            .setMessage(R.string.dialog_termux_message)
            .setPositiveButton(R.string.btn_install_termux) { _, _ ->
                openTermuxDownload()
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .setNeutralButton(R.string.btn_more_info) { _, _ ->
                TermuxUtils.openTermuxOnF_Droid(this)
            }
            .show()
    }

    private fun showLaunchDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_launch_title)
            .setMessage(R.string.dialog_launch_message)
            .setPositiveButton(R.string.btn_ok, null)
            .show()
    }

    private fun showErrorDialog(message: String, errorCode: String) {
        val fullMessage = buildString {
            appendLine(message)
            appendLine()
            appendLine(getString(R.string.error_code, errorCode))
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_error_title)
            .setMessage(fullMessage)
            .setPositiveButton(R.string.btn_ok, null)
            .setNeutralButton(R.string.btn_help) { _, _ ->
                startActivity(Intent(this, HelpActivity::class.java))
            }
            .show()
    }

    private fun showWarningDialog(message: String, suggestion: String) {
        val fullMessage = buildString {
            appendLine(message)
            appendLine()
            appendLine(getString(R.string.suggestion, suggestion))
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_warning_title)
            .setMessage(fullMessage)
            .setPositiveButton(R.string.btn_ok, null)
            .show()
    }

    private fun openTermuxDownload() {
        val result = TermuxUtils.openTermuxOnF_Droid(this)

        when (result) {
            is TermuxUtils.TermuxResult.Success -> {
                showSnackbar(getString(R.string.toast_opening_f_droid))
            }
            is TermuxUtils.TermuxResult.Error -> {
                showSnackbar(getString(R.string.toast_open_browser_manually))
            }
            else -> {}
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        try {
            menuInflater.inflate(R.menu.menu_main, menu)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error creating options menu", e)
            false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return try {
            when (item.itemId) {
                R.id.action_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                R.id.action_refresh -> {
                    checkInstallationStatus()
                    showSnackbar(getString(R.string.toast_refreshing))
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling menu item", e)
            false
        }
    }
}
