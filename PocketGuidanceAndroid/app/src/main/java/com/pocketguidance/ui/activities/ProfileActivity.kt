package com.pocketguidance.ui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.pocketguidance.R
import com.pocketguidance.databinding.ActivityProfileBinding
import com.pocketguidance.ui.adapters.BadgeAdapter
import com.pocketguidance.utils.PhotoUtils
import com.pocketguidance.utils.SessionManager
import com.pocketguidance.utils.ValidationUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

class ProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val TAG = "ProfileActivity"
    private lateinit var badgeAdapter: BadgeAdapter

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val destFile = PhotoUtils.createImageFile(this)
            contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output -> input.copyTo(output) }
            }
            val path = destFile.absolutePath
            Glide.with(this).load(destFile).circleCrop().into(binding.ivAvatar)
            lifecycleScope.launch {
                authRepo.updateAvatar(userId, path)
                Log.i(TAG, "Avatar updated: $path")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requireLogin()

        supportActionBar?.apply { title = "Profile"; setDisplayHomeAsUpEnabled(true) }

        setupBadges()
        loadUserData()
        setupListeners()
    }

    private fun setupBadges() {
        badgeAdapter = BadgeAdapter(emptyList())
        binding.rvBadges.adapter = badgeAdapter

        lifecycleScope.launch {
            financeRepo.getBadges(userId).collectLatest { badges ->
                badgeAdapter.updateData(badges)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }

    private fun loadUserData() {
        lifecycleScope.launch {
            val user = authRepo.getUserById(userId)
            val prefs = financeRepo.getUserPrefsOnce(userId)

            binding.etUsername.setText(user?.username ?: "")
            binding.tvEmail.text = user?.email ?: ""
            binding.etMonthlyIncome.setText(prefs?.monthlyIncome?.toString() ?: "")

            if (user?.avatarPath != null) {
                Glide.with(this@ProfileActivity).load(File(user.avatarPath)).circleCrop().into(binding.ivAvatar)
            }
        }
    }

    private fun setupListeners() {
        binding.ivAvatar.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        binding.btnSave.setOnClickListener {
            val username   = binding.etUsername.text.toString().trim()
            val incomeStr  = binding.etMonthlyIncome.text.toString().trim()

            var valid = true
            if (!ValidationUtils.isValidUsername(username)) {
                binding.tilUsername.error = "Username must be at least 2 characters"
                valid = false
            } else binding.tilUsername.error = null

            val income = ValidationUtils.parseAmount(incomeStr)
            if (income == null) {
                binding.tilMonthlyIncome.error = "Enter a valid income"
                valid = false
            } else binding.tilMonthlyIncome.error = null

            if (!valid) return@setOnClickListener

            lifecycleScope.launch {
                authRepo.updateUsername(userId, username)
                financeRepo.updateMonthlyIncome(userId, income!!)
                SessionManager.saveSession(this@ProfileActivity, userId, username, SessionManager.getEmail(this@ProfileActivity))
                Toast.makeText(this@ProfileActivity, "Profile updated!", Toast.LENGTH_SHORT).show()
                Log.i(TAG, "Profile saved: username=$username, income=$income")
            }
        }

        binding.btnLogout.setOnClickListener {
            SessionManager.clearSession(this)
            Log.i(TAG, "User logged out")
            navigateTo(LoginActivity::class.java, finish = true)
        }
    }
}
