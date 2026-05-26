package com.pocketguidance.ui.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.pocketguidance.R
import com.pocketguidance.databinding.ActivityLoginBinding
import com.pocketguidance.utils.SessionManager
import com.pocketguidance.utils.ValidationUtils
import kotlinx.coroutines.launch

class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        Log.d(TAG, "LoginActivity created")
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener { attemptLogin() }
        binding.tvSignup.setOnClickListener { navigateTo(SignupActivity::class.java) }
        binding.tvForgotPassword.setOnClickListener { showForgotPasswordDialog() }
    }

    private fun attemptLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        // Validate
        if (!ValidationUtils.isValidEmail(email)) {
            binding.tilEmail.error = "Enter a valid email address"
            return
        } else {
            binding.tilEmail.error = null
        }

        if (!ValidationUtils.isValidPassword(password)) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            return
        } else {
            binding.tilPassword.error = null
        }

        setLoading(true)

        lifecycleScope.launch {
            val userId = authRepo.login(email, password)
            setLoading(false)

            if (userId != null) {
                val user = authRepo.getUserById(userId)
                SessionManager.saveSession(
                    context = this@LoginActivity,
                    userId = userId,
                    username = user?.username ?: "",
                    email = user?.email ?: ""
                )
                Log.i(TAG, "Login success for userId=$userId")

                // Check onboarding
                val prefs = financeRepo.getUserPrefsOnce(userId)
                if (prefs == null || !prefs.onboarded) {
                    Log.d(TAG, "User not onboarded → Onboarding")
                    navigateTo(OnboardingActivity::class.java, finish = true)
                } else {
                    Log.d(TAG, "User onboarded → Dashboard")
                    navigateTo(DashboardActivity::class.java, finish = true)
                }
            } else {
                Log.w(TAG, "Login failed for email=$email")
                Toast.makeText(this@LoginActivity, "Invalid email or password", Toast.LENGTH_SHORT).show()
                binding.tilPassword.error = "Incorrect email or password"
            }
        }
    }

    private fun showForgotPasswordDialog() {
        val emailField = binding.etEmail.text.toString().trim()
        // Simplified: just show a message. Real app would send reset email.
        Toast.makeText(
            this,
            if (emailField.isNotEmpty()) "Reset link sent to $emailField" else "Enter your email above first",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun setLoading(loading: Boolean) {
        binding.btnLogin.isEnabled = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }
}
