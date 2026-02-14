package com.openclaw.apk.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.openclaw.apk.R
import com.openclaw.apk.databinding.ActivityHelpBinding

/**
 * Help Activity - Shows help and documentation
 */
class HelpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHelpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupViews()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.title_help)
        }
    }

    private fun setupViews() {
        binding.cardTermux.setOnClickListener {
            openUrl(getString(R.string.url_termux_wiki))
        }

        binding.cardOpenClaw.setOnClickListener {
            openUrl(getString(R.string.url_openclaw_docs))
        }

        binding.cardGitHub.setOnClickListener {
            openUrl(getString(R.string.url_github))
        }

        binding.cardDiscord.setOnClickListener {
            openUrl(getString(R.string.url_discord))
        }
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            // Handle error
        }
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
