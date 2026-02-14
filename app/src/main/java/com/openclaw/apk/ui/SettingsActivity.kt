package com.openclaw.apk.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.openclaw.apk.R
import com.openclaw.apk.databinding.ActivitySettingsBinding
import com.openclaw.apk.utils.PreferencesManager

/**
 * Settings Activity - Configure installation options
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesManager = PreferencesManager(this)

        setupToolbar()
        loadSettings()
        setupListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.title_settings)
        }
    }

    private fun loadSettings() {
        binding.apply {
            etInstallPath.setText(preferencesManager.installPath)
            etCloneUrl.setText(preferencesManager.cloneUrl)
            switchAutoStart.isChecked = preferencesManager.autoStart
            switchSetupVoice.isChecked = preferencesManager.setupVoice
        }
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            saveSettings()
        }

        binding.btnReset.setOnClickListener {
            showResetConfirmation()
        }
    }

    private fun saveSettings() {
        preferencesManager.apply {
            installPath = binding.etInstallPath.text.toString().trim()
                .ifEmpty { "~/.openclaw" }
            cloneUrl = binding.etCloneUrl.text.toString().trim()
                .ifEmpty { "https://github.com/openclaw/openclaw.git" }
            autoStart = binding.switchAutoStart.isChecked
            setupVoice = binding.switchSetupVoice.isChecked
        }

        finish()
    }

    private fun showResetConfirmation() {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_reset_title)
            .setMessage(R.string.dialog_reset_message)
            .setPositiveButton(R.string.btn_yes) { _, _ ->
                preferencesManager.reset()
                loadSettings()
            }
            .setNegativeButton(R.string.btn_no, null)
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
