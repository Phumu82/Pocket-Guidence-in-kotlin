package com.pocketguidance.ui.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.pocketguidance.data.db.entities.GoalEntity
import com.pocketguidance.databinding.ActivityGoalsBinding
import com.pocketguidance.ui.adapters.GoalAdapter
import com.pocketguidance.utils.FormatUtils
import com.pocketguidance.utils.ValidationUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

class GoalsActivity : BaseActivity() {

    private lateinit var binding: ActivityGoalsBinding
    private lateinit var adapter: GoalAdapter
    private val TAG = "GoalsActivity"
    private var selectedDeadline: String = FormatUtils.todayIso()
    private var currency = "R"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoalsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requireLogin()

        supportActionBar?.apply { title = "Savings Goals"; setDisplayHomeAsUpEnabled(true) }

        lifecycleScope.launch {
            currency = financeRepo.getUserPrefsOnce(userId)?.currency ?: "R"
            setupRecyclerView()
            setupForm()
            observeGoals()
        }
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }

    private fun setupRecyclerView() {
        adapter = GoalAdapter(currency) { goal ->
            lifecycleScope.launch {
                val contribution = minOf((goal.targetAmount - goal.currentAmount), 500.0)
                if (contribution <= 0) {
                    Toast.makeText(this@GoalsActivity, "Goal already completed!", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                financeRepo.contributeToGoal(goal.id, contribution, userId, goal.name)
                Toast.makeText(
                    this@GoalsActivity,
                    "Contributed ${FormatUtils.formatCurrency(contribution, currency)} to ${goal.name}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.i(TAG, "Contributed $contribution to goal ${goal.id}")
            }
        }
        binding.rvGoals.layoutManager = LinearLayoutManager(this)
        binding.rvGoals.adapter = adapter
    }

    private fun setupForm() {
        // Deadline date picker
        binding.tvDeadline.text = selectedDeadline
        binding.tvDeadline.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                selectedDeadline = "%04d-%02d-%02d".format(y, m + 1, d)
                binding.tvDeadline.text = selectedDeadline
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Frequency spinner
        val frequencies = listOf("monthly", "weekly", "daily")
        binding.spinnerFrequency.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, frequencies)

        binding.btnAddGoal.setOnClickListener {
            val name      = binding.etGoalName.text.toString().trim()
            val targetStr = binding.etTargetAmount.text.toString().trim()
            val frequency = binding.spinnerFrequency.selectedItem?.toString() ?: "monthly"

            var valid = true
            if (name.length < 2) { binding.tilGoalName.error = "Enter a goal name"; valid = false }
            else binding.tilGoalName.error = null

            val target = ValidationUtils.parseAmount(targetStr)
            if (target == null) { binding.tilTargetAmount.error = "Enter a valid target amount"; valid = false }
            else binding.tilTargetAmount.error = null

            if (!valid) return@setOnClickListener

            lifecycleScope.launch {
                financeRepo.addGoal(
                    GoalEntity(
                        userId = userId,
                        name = name,
                        targetAmount = target!!,
                        deadline = selectedDeadline,
                        frequency = frequency
                    )
                )
                binding.etGoalName.text?.clear()
                binding.etTargetAmount.text?.clear()
                Toast.makeText(this@GoalsActivity, "Goal \"$name\" created!", Toast.LENGTH_SHORT).show()
                Log.i(TAG, "Goal created: $name, target=$target")
            }
        }
    }

    private fun observeGoals() {
        lifecycleScope.launch {
            financeRepo.getGoals(userId).collectLatest { goals ->
                adapter.submitList(goals)
                Log.d(TAG, "Goals updated: ${goals.size}")
            }
        }
    }
}
