package com.pocketguidance.ui.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.pocketguidance.databinding.ActivityOnboardingBinding
import com.pocketguidance.utils.ValidationUtils
import kotlinx.coroutines.launch

class OnboardingActivity : BaseActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private val TAG = "OnboardingActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requireLogin()

        binding.btnGetStarted.setOnClickListener { handleOnboarding() }
    }

    private fun handleOnboarding() {
        val incomeStr = binding.etMonthlyIncome.text.toString()
        val currency  = binding.spinnerCurrency.selectedItem?.toString() ?: "R"

        if (!ValidationUtils.isValidAmount(incomeStr)) {
            binding.tilMonthlyIncome.error = "Enter a valid monthly income"
            return
        }
        binding.tilMonthlyIncome.error = null

        val income = incomeStr.toDouble()

        lifecycleScope.launch {
            financeRepo.completeOnboarding(userId, income)
            financeRepo.updateCurrency(userId, currency)
            Log.i(TAG, "Onboarding done: userId=$userId, income=$income, currency=$currency")
            Toast.makeText(this@OnboardingActivity, "Welcome to Pocket Guidance! 🎉", Toast.LENGTH_LONG).show()
            navigateTo(DashboardActivity::class.java, finish = true)
        }
    }
}
