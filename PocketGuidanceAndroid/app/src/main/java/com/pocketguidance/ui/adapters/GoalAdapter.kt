package com.pocketguidance.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pocketguidance.data.db.entities.GoalEntity
import com.pocketguidance.databinding.ItemGoalBinding
import com.pocketguidance.utils.FormatUtils

class GoalAdapter(
    private val currency: String,
    private val onContribute: (GoalEntity) -> Unit
) : ListAdapter<GoalEntity, GoalAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val b: ItemGoalBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(goal: GoalEntity) {
            val pct = if (goal.targetAmount > 0) ((goal.currentAmount / goal.targetAmount) * 100).toInt().coerceAtMost(100) else 0

            b.tvGoalName.text    = goal.name
            b.tvProgress.text    = "${FormatUtils.formatCurrency(goal.currentAmount, currency)} / ${FormatUtils.formatCurrency(goal.targetAmount, currency)}"
            b.tvPercent.text     = "$pct%"
            b.tvDeadline.text    = "Due: ${FormatUtils.formatDisplayDate(goal.deadline)} (${goal.frequency})"
            b.progressBar.progress = pct

            val tint = if (goal.completed) Color.parseColor("#22C55E") else Color.parseColor("#4F46E5")
            b.progressBar.progressTintList = android.content.res.ColorStateList.valueOf(tint)

            if (goal.completed) {
                b.tvStatus.text = "✅ Completed!"
                b.tvStatus.setTextColor(Color.parseColor("#22C55E"))
                b.btnContribute.isEnabled = false
                b.btnContribute.text = "Done"
            } else {
                b.tvStatus.text = ""
                b.btnContribute.isEnabled = true
                b.btnContribute.text = "Contribute"
                b.btnContribute.setOnClickListener { onContribute(goal) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemGoalBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<GoalEntity>() {
            override fun areItemsTheSame(a: GoalEntity, b: GoalEntity) = a.id == b.id
            override fun areContentsTheSame(a: GoalEntity, b: GoalEntity) = a == b
        }
    }
}
