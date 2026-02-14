package com.openclaw.apk.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.openclaw.apk.R
import com.openclaw.apk.databinding.ActivitySetupBinding
import com.openclaw.apk.model.InstallConfig
import com.openclaw.apk.model.InstallStep
import com.openclaw.apk.model.InstallationStatus
import com.openclaw.apk.utils.PreferencesManager
import com.openclaw.apk.utils.TermuxUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Setup Activity - Guides user through OpenClaw installation
 */
class SetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupBinding
    private lateinit var preferencesManager: PreferencesManager

    private val installSteps = mutableListOf<InstallStep>()
    private var isInstalling = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesManager = PreferencesManager(this)

        setupToolbar()
        setupViews()
        loadInstallSteps()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.title_setup)
        }
    }

    private fun setupViews() {
        binding.btnInstall.setOnClickListener {
            if (isInstalling) {
                showCancelConfirmation()
            } else {
                startInstallation()
            }
        }

        binding.btnCopyScript.setOnClickListener {
            copyScriptToClipboard()
        }

        binding.btnOpenTermux.setOnClickListener {
            openTermuxForManualInstall()
        }

        updateInstallButton()
    }

    private fun loadInstallSteps() {
        installSteps.clear()
        installSteps.addAll(
            listOf(
                InstallStep(1, getString(R.string.step_update_packages), getString(R.string.step_update_packages_desc)),
                InstallStep(2, getString(R.string.step_install_deps), getString(R.string.step_install_deps_desc)),
                InstallStep(3, getString(R.string.step_clone_repo), getString(R.string.step_clone_repo_desc)),
                InstallStep(4, getString(R.string.step_install_npm), getString(R.string.step_install_npm_desc)),
                InstallStep(5, getString(R.string.step_setup_voice), getString(R.string.step_setup_voice_desc)),
                InstallStep(6, getString(R.string.step_complete), getString(R.string.step_complete_desc))
            )
        )

        binding.tvInstallScript.text = generateInstallScript()
    }

    private fun generateInstallScript(): String {
        val config = preferencesManager.getInstallConfig()
        return TermuxUtils.generateInstallScript(config)
    }

    private fun updateInstallButton() {
        val termuxInstalled = TermuxUtils.isTermuxInstalled(this)

        binding.apply {
            btnOpenTermux.visibility = if (termuxInstalled) View.GONE else View.VISIBLE
            btnCopyScript.visibility = View.VISIBLE
            btnInstall.visibility = View.VISIBLE

            btnInstall.text = when {
                !termuxInstalled -> getString(R.string.btn_install_termux_first)
                isInstalling -> getString(R.string.btn_cancel)
                else -> getString(R.string.btn_start_install)
            }
        }
    }

    private fun startInstallation() {
        val termuxInstalled = TermuxUtils.isTermuxInstalled(this)

        if (!termuxInstalled) {
            openTermuxForInstallation()
            return
        }

        isInstalling = true
        updateInstallButton()
        showInstallDialog()
    }

    private fun openTermuxForInstallation() {
        if (TermuxUtils.openTermux(this)) {
            Toast.makeText(this, getString(R.string.toast_run_script), Toast.LENGTH_LONG).show()
        } else {
            TermuxUtils.openTermuxOnF_Droid(this)
        }
    }

    private fun openTermuxForManualInstall() {
        if (TermuxUtils.openTermux(this)) {
            Toast.makeText(this, getString(R.string.toast_copy_script_first), Toast.LENGTH_LONG).show()
        } else {
            TermuxUtils.openTermuxOnF_Droid(this)
        }
    }

    private fun copyScriptToClipboard() {
        val script = generateInstallScript()
        val clipboard = getSystemService(android.content.ClipboardManager::class.java)
        val clip = android.content.ClipData.newPlainText("OpenClaw Install Script", script)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, getString(R.string.toast_script_copied), Toast.LENGTH_SHORT).show()
    }

    private fun showInstallDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_install_title)
            .setMessage(R.string.dialog_install_message)
            .setPositiveButton(R.string.btn_open_termux) { _, _ ->
                openTermuxForInstallation()
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    private fun showCancelConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_cancel_title)
            .setMessage(R.string.dialog_cancel_message)
            .setPositiveButton(R.string.btn_yes) { _, _ ->
                isInstalling = false
                updateInstallButton()
            }
            .setNegativeButton(R.string.btn_no, null)
            .show()
    }

    private fun completeInstallation() {
        lifecycleScope.launch {
            preferencesManager.isOpenClawInstalled = true
            isInstalling = false
            updateInstallButton()
            showCompletionDialog()
        }
    }

    private fun showCompletionDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_complete_title)
            .setMessage(R.string.dialog_complete_message)
            .setPositiveButton(R.string.btn_launch) { _, _ ->
                TermuxUtils.openTermux(this@SetupActivity)
            }
            .setNegativeButton(R.string.btn_done, null)
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
