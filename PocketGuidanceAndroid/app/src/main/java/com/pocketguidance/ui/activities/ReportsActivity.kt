package com.pocketguidance.ui.activities

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.pocketguidance.databinding.ActivityReportsBinding
import com.pocketguidance.utils.FormatUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.graphics.Color
import com.github.mikephil.charting.components.LimitLine

class ReportsActivity : BaseActivity() {

    private lateinit var binding: ActivityReportsBinding
    private var currency: String = "R"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requireLogin()

        setupChart()
        observeData()
        setupListeners()
    }

    private fun setupChart() {
        binding.spendingLineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            axisRight.isEnabled = false
            animateX(1000)
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            financeRepo.getUserPrefs(userId).collectLatest { prefs ->
                currency = prefs?.currency ?: "R"
                val minGoal = prefs?.minMonthlySpendingGoal ?: 0.0
                val maxGoal = prefs?.maxMonthlySpendingGoal ?: 0.0

                binding.etMinGoal.setText(minGoal.toString())
                binding.etMaxGoal.setText(maxGoal.toString())

                updateChartWithGoals(minGoal, maxGoal)
                calculatePerformance(minGoal, maxGoal)
            }
        }

        lifecycleScope.launch {
            financeRepo.getExpenses(userId).collectLatest { expenses ->
                updateSpendingTrend(expenses)
            }
        }
    }

    private fun updateChartWithGoals(min: Double, max: Double) {
        val yAxis = binding.spendingLineChart.axisLeft
        yAxis.removeAllLimitLines()

        if (max > 0) {
            val maxLine = LimitLine(max.toFloat(), "Max Goal").apply {
                lineColor = Color.parseColor("#EF4444")
                lineWidth = 2f
                textColor = Color.parseColor("#EF4444")
                textSize = 10f
            }
            yAxis.addLimitLine(maxLine)
        }

        if (min > 0) {
            val minLine = LimitLine(min.toFloat(), "Min Goal").apply {
                lineColor = Color.parseColor("#22C55E")
                lineWidth = 2f
                textColor = Color.parseColor("#22C55E")
                textSize = 10f
            }
            yAxis.addLimitLine(minLine)
        }
        binding.spendingLineChart.invalidate()
    }

    private fun updateSpendingTrend(expenses: List<com.pocketguidance.data.db.entities.TransactionEntity>) {
        // Group by month for the last 6 months
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val monthFormatter = DateTimeFormatter.ofPattern("MMM")

        val now = LocalDate.now()
        val last6Months = (0..5).map { now.minusMonths(it.toLong()) }.reversed()

        val entries = mutableListOf<Entry>()
        val labels = mutableListOf<String>()

        last6Months.forEachIndexed { index, date ->
            val monthStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM"))
            val total = expenses.filter { it.date.startsWith(monthStr) }.sumOf { it.amount }
            entries.add(Entry(index.toFloat(), total.toFloat()))
            labels.add(date.format(monthFormatter))
        }

        val dataSet = LineDataSet(entries, "Monthly Spending").apply {
            color = Color.parseColor("#4F46E5")
            setCircleColor(Color.parseColor("#4F46E5"))
            lineWidth = 3f
            circleRadius = 5f
            setDrawCircleHole(true)
            valueTextSize = 9f
            setDrawFilled(true)
            fillColor = Color.parseColor("#4F46E5")
            fillAlpha = 30
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        binding.spendingLineChart.apply {
            data = LineData(dataSet)
            xAxis.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return labels.getOrNull(value.toInt()) ?: ""
                }
            }
            invalidate()
        }
    }

    private fun calculatePerformance(min: Double, max: Double) {
        lifecycleScope.launch {
            val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
            val expenses = financeRepo.getExpensesByDateRange(userId, "$currentMonth-01", "$currentMonth-31")
            
            expenses.collectLatest { monthlyExpenses ->
                val totalSpent = monthlyExpenses.sumOf { it.amount }
                
                if (max == 0.0) {
                    binding.tvPerformanceStatus.text = "Set your spending goals to see performance."
                    binding.pbBudgetPerformance.progress = 0
                    return@collectLatest
                }

                val progress = ((totalSpent / max) * 100).toInt().coerceIn(0, 100)
                binding.pbBudgetPerformance.progress = progress

                val status = when {
                    totalSpent < min -> "Below minimum goal. Good savings, but ensure necessities are covered!"
                    totalSpent <= max -> "Excellent! You are within your target spending range."
                    else -> "Alert: You have exceeded your maximum spending goal."
                }
                
                binding.tvPerformanceStatus.text = "Spent this month: ${FormatUtils.formatCurrency(totalSpent, currency)}\n$status"
                binding.tvMinGoalLabel.text = "Min: ${FormatUtils.formatCurrency(min, currency)}"
                binding.tvMaxGoalLabel.text = "Max: ${FormatUtils.formatCurrency(max, currency)}"
            }
        }
    }

    private fun setupListeners() {
        binding.btnSaveGoals.setOnClickListener {
            val min = binding.etMinGoal.text.toString().toDoubleOrNull() ?: 0.0
            val max = binding.etMaxGoal.text.toString().toDoubleOrNull() ?: 0.0

            if (max > 0 && min >= max) {
                Toast.makeText(this, "Minimum goal should be less than maximum goal", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                financeRepo.updateSpendingGoals(userId, min, max)
                Toast.makeText(this@ReportsActivity, "Goals updated successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
