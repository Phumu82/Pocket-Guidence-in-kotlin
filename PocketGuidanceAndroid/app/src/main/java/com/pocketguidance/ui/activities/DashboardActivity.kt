package com.pocketguidance.ui.activities

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.ColorTemplate
import com.pocketguidance.databinding.ActivityDashboardBinding
import com.pocketguidance.utils.FormatUtils
import com.pocketguidance.utils.SessionManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DashboardActivity : BaseActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private val TAG = "DashboardActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requireLogin()

        binding.tvWelcome.text = "Welcome back, ${SessionManager.getUsername(this)} 👋"

        setupNavigation()
        observeData()
        Log.d(TAG, "DashboardActivity created for userId=$userId")
    }

    private fun setupNavigation() {
        binding.cardAddExpense.setOnClickListener    { navigateTo(AddExpenseActivity::class.java) }
        binding.cardViewExpenses.setOnClickListener  { navigateTo(ViewExpensesActivity::class.java) }
        binding.cardBudgets.setOnClickListener       { navigateTo(BudgetsActivity::class.java) }
        binding.cardGoals.setOnClickListener         { navigateTo(GoalsActivity::class.java) }
        binding.cardCategories.setOnClickListener    { navigateTo(CategoryActivity::class.java) }
        binding.cardProfile.setOnClickListener       { navigateTo(ProfileActivity::class.java) }
        binding.cardEarn.setOnClickListener          { navigateTo(EarnActivity::class.java) }
        binding.cardReports.setOnClickListener       { navigateTo(ReportsActivity::class.java) }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            financeRepo.checkAndAwardBadges(userId)
        }
    }

    private fun observeData() {
        // Observe transactions flow for live totals
        lifecycleScope.launch {
            financeRepo.getAllTransactions(userId).collectLatest { transactions ->
                val totalIncome   = transactions.filter { it.type == "income" || it.type == "received" }.sumOf { it.amount }
                val totalExpenses = transactions.filter { it.type == "expense" }.sumOf { it.amount }
                val totalGoalContrib = transactions.filter { it.type == "goal_contribution" }.sumOf { it.amount }
                val totalSettlements = transactions.filter { it.type == "settlement" }.sumOf { it.amount }
                val currentBalance = totalIncome - totalExpenses - totalGoalContrib - totalSettlements

                val prefs = financeRepo.getUserPrefsOnce(userId)
                val currency = prefs?.currency ?: "R"
                val fmt = { n: Double -> FormatUtils.formatCurrency(n, currency) }

                binding.tvBalance.text     = fmt(currentBalance)
                binding.tvIncome.text      = fmt(totalIncome)
                binding.tvExpenses.text    = fmt(totalExpenses)

                // Pie chart — spending by category
                val categoryTotals = financeRepo.getCategoryBreakdown(userId)
                if (categoryTotals.isNotEmpty()) {
                    val entries = categoryTotals.take(5).map { PieEntry(it.total.toFloat(), it.category) }
                    val dataSet = PieDataSet(entries, "Spending").apply {
                        colors = ColorTemplate.MATERIAL_COLORS.toList()
                        valueTextSize = 11f
                    }
                    binding.pieChart.apply {
                        data = PieData(dataSet)
                        description.isEnabled = false
                        isDrawHoleEnabled = true
                        holeRadius = 45f
                        setUsePercentValues(true)
                        legend.isEnabled = true
                        animateY(800)
                        invalidate()
                    }
                }

                // Bar chart — Income vs Expenses
                val barEntries = listOf(
                    BarEntry(0f, totalIncome.toFloat()),
                    BarEntry(1f, totalExpenses.toFloat()),
                    BarEntry(2f, currentBalance.coerceAtLeast(0.0).toFloat())
                )
                val barDataSet = BarDataSet(barEntries, "").apply {
                    colors = listOf(
                        android.graphics.Color.parseColor("#22C55E"),
                        android.graphics.Color.parseColor("#EF4444"),
                        android.graphics.Color.parseColor("#4F46E5")
                    )
                }
                binding.barChart.apply {
                    data = BarData(barDataSet).apply { barWidth = 0.5f }
                    description.isEnabled = false
                    legend.isEnabled = false
                    xAxis.setDrawLabels(false)
                    animateY(800)
                    invalidate()
                }

                Log.d(TAG, "Dashboard refreshed: balance=$currentBalance, income=$totalIncome, expenses=$totalExpenses")
            }
        }

        // Observe goals for progress summary
        lifecycleScope.launch {
            financeRepo.getGoals(userId).collectLatest { goals ->
                val avgProgress = if (goals.isEmpty()) 0
                else (goals.sumOf { (it.currentAmount / it.targetAmount) * 100 } / goals.size).toInt()
                binding.tvGoalProgress.text = "Savings Goals: $avgProgress% avg"
            }
        }
    }
}
