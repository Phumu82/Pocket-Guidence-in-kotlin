package com.pocketguidance.ui.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.pocketguidance.databinding.ActivityViewExpensesBinding
import com.pocketguidance.ui.adapters.TransactionAdapter
import com.pocketguidance.utils.FormatUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

class ViewExpensesActivity : BaseActivity() {

    private lateinit var binding: ActivityViewExpensesBinding
    private lateinit var adapter: TransactionAdapter
    private val TAG = "ViewExpensesActivity"

    private var startDate: String = FormatUtils.firstDayOfMonthIso()
    private var endDate: String   = FormatUtils.todayIso()
    private var observeJob: Job?  = null
    private var currency: String  = "R"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewExpensesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requireLogin()

        supportActionBar?.apply {
            title = "Transactions"
            setDisplayHomeAsUpEnabled(true)
        }

        lifecycleScope.launch {
            currency = financeRepo.getUserPrefsOnce(userId)?.currency ?: "R"
            setupRecyclerView()
            setupDateFilters()
            setupFilterButton()
            observeTransactions()
        }
        Log.d(TAG, "ViewExpensesActivity created for userId=$userId")
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter(currency) { tx ->
            // Click on receipt photo
            if (tx.receiptPhotoPath != null) {
                val intent = android.content.Intent(this, ReceiptViewActivity::class.java).apply {
                    putExtra(ReceiptViewActivity.EXTRA_PHOTO_PATH, tx.receiptPhotoPath)
                }
                startActivity(intent)
            }
        }
        binding.rvTransactions.layoutManager = LinearLayoutManager(this)
        binding.rvTransactions.adapter = adapter
    }

    private fun setupDateFilters() {
        binding.tvStartDate.text = startDate
        binding.tvEndDate.text   = endDate

        binding.tvStartDate.setOnClickListener { pickDate(isStart = true) }
        binding.tvEndDate.setOnClickListener   { pickDate(isStart = false) }
    }

    private fun setupFilterButton() {
        binding.btnFilter.setOnClickListener {
            if (startDate > endDate) {
                Toast.makeText(this, "Start date must be before end date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            observeTransactions()
        }

        binding.btnAllTime.setOnClickListener {
            startDate = "2000-01-01"
            endDate   = FormatUtils.todayIso()
            binding.tvStartDate.text = startDate
            binding.tvEndDate.text   = endDate
            observeTransactions()
        }
    }

    private fun pickDate(isStart: Boolean) {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            val picked = "%04d-%02d-%02d".format(y, m + 1, d)
            if (isStart) {
                startDate = picked
                binding.tvStartDate.text = startDate
            } else {
                endDate = picked
                binding.tvEndDate.text = endDate
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun observeTransactions() {
        observeJob?.cancel()
        observeJob = lifecycleScope.launch {
            Log.d(TAG, "Observing transactions: $startDate → $endDate")
            financeRepo.getTransactionsByDateRange(userId, startDate, endDate).collectLatest { txList ->
                adapter.submitList(txList)

                val totalExpenses = txList.filter { it.type == "expense" }.sumOf { it.amount }
                val totalIncome   = txList.filter { it.type == "income" || it.type == "received" }.sumOf { it.amount }

                binding.tvTotalExpenses.text = "Expenses: ${FormatUtils.formatCurrency(totalExpenses, currency)}"
                binding.tvTotalIncome.text   = "Income: ${FormatUtils.formatCurrency(totalIncome, currency)}"
                binding.tvTransactionCount.text = "${txList.size} transactions"

                binding.tvEmpty.visibility = if (txList.isEmpty()) View.VISIBLE else View.GONE
                Log.d(TAG, "Transactions loaded: ${txList.size}")
            }
        }
    }
}
