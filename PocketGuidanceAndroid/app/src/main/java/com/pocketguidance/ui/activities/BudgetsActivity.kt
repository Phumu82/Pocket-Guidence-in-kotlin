package com.pocketguidance.ui.activities

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.pocketguidance.data.db.entities.BudgetEntity
import com.pocketguidance.databinding.ActivityBudgetsBinding
import com.pocketguidance.ui.adapters.BudgetAdapter
import com.pocketguidance.utils.FormatUtils
import com.pocketguidance.utils.ValidationUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BudgetsActivity : BaseActivity() {

    private lateinit var binding: ActivityBudgetsBinding
    private lateinit var adapter: BudgetAdapter
    private val TAG = "BudgetsActivity"
    private var currency = "R"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBudgetsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requireLogin()

        supportActionBar?.apply { title = "Budgets"; setDisplayHomeAsUpEnabled(true) }

        lifecycleScope.launch {
            currency = financeRepo.getUserPrefsOnce(userId)?.currency ?: "R"
            setupRecyclerView()
            loadCategorySpinner()
            setupAddButton()
            observeBudgets()
        }
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }

    private fun setupRecyclerView() {
        adapter = BudgetAdapter(currency)
        binding.rvBudgets.layoutManager = LinearLayoutManager(this)
        binding.rvBudgets.adapter = adapter
    }

    private fun loadCategorySpinner() {
        lifecycleScope.launch {
            val cats = financeRepo.getCategoriesOnce(userId).map { it.name }
            binding.spinnerCategory.adapter = ArrayAdapter(this@BudgetsActivity, android.R.layout.simple_spinner_dropdown_item, cats)
        }
    }

    private fun setupAddButton() {
        binding.btnAddBudget.setOnClickListener {
            val category = binding.spinnerCategory.selectedItem?.toString() ?: ""
            val limitStr = binding.etLimit.text.toString().trim()

            val limit = ValidationUtils.parseAmount(limitStr)
            if (limit == null) {
                binding.tilLimit.error = "Enter a valid limit"
                return@setOnClickListener
            }
            binding.tilLimit.error = null

            lifecycleScope.launch {
                financeRepo.addBudget(BudgetEntity(userId = userId, category = category, limitAmount = limit))
                binding.etLimit.text?.clear()
                Toast.makeText(this@BudgetsActivity, "Budget for \"$category\" set!", Toast.LENGTH_SHORT).show()
                Log.i(TAG, "Budget added: category=$category, limit=$limit")
            }
        }
    }

    private fun observeBudgets() {
        lifecycleScope.launch {
            financeRepo.getBudgets(userId).collectLatest { budgets ->
                adapter.submitList(budgets)
                Log.d(TAG, "Budgets updated: ${budgets.size}")
            }
        }
    }
}
