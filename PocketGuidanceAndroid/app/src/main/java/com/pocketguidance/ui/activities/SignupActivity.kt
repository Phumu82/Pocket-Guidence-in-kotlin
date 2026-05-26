package com.pocketguidance.ui.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.pocketguidance.R
import com.pocketguidance.databinding.ActivitySignupBinding
import com.pocketguidance.utils.SessionManager
import com.pocketguidance.utils.ValidationUtils
import kotlinx.coroutines.launch

class SignupActivity : BaseActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val TAG = "SignupActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnSignup.setOnClickListener { attemptSignup() }
        binding.tvLogin.setOnClickListener { finish() }   // go back to Login
    }

    private fun attemptSignup() {
        val username = binding.etUsername.text.toString().trim()
        val email    = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirm  = binding.etConfirmPassword.text.toString()

        var valid = true

        if (!ValidationUtils.isValidUsername(username)) {
            binding.tilUsername.error = "Username must be at least 2 characters"
            valid = false
        } else binding.tilUsername.error = null

        if (!ValidationUtils.isValidEmail(email)) {
            binding.tilEmail.error = "Enter a valid email address"
            valid = false
        } else binding.tilEmail.error = null

        if (!ValidationUtils.isValidPassword(password)) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            valid = false
        } else binding.tilPassword.error = null

        if (password != confirm) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            valid = false
        } else binding.tilConfirmPassword.error = null

        if (!valid) return

        setLoading(true)

        lifecycleScope.launch {
            val userId = authRepo.signup(email, password, username)
            setLoading(false)

            if (userId != null) {
                // Create default prefs and seed categories
                financeRepo.createDefaultPrefs(userId)
                financeRepo.seedDefaultCategories(userId)

                SessionManager.saveSession(
                    context = this@SignupActivity,
                    userId = userId,
                    username = username,
                    email = email
                )
                Log.i(TAG, "Signup success: userId=$userId")
                navigateTo(OnboardingActivity::class.java, finish = true)
            } else {
                Log.w(TAG, "Signup failed: email already registered")
                Toast.makeText(this@SignupActivity, "Email is already registered", Toast.LENGTH_SHORT).show()
                binding.tilEmail.error = "Email already registered"
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.btnSignup.isEnabled = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }
}
