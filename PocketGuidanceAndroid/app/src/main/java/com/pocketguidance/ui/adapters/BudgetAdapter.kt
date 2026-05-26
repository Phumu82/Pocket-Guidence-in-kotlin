package com.pocketguidance.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pocketguidance.data.db.entities.BudgetEntity
import com.pocketguidance.databinding.ItemBudgetBinding
import com.pocketguidance.utils.FormatUtils

class BudgetAdapter(private val currency: String) :
    ListAdapter<BudgetEntity, BudgetAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val b: ItemBudgetBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(budget: BudgetEntity) {
            val pct = if (budget.limitAmount > 0) ((budget.spent / budget.limitAmount) * 100).toInt().coerceAtMost(100) else 0
            val warning = pct >= 90

            b.tvCategory.text = budget.category
            b.tvSpent.text    = "Spent: ${FormatUtils.formatCurrency(budget.spent, currency)}"
            b.tvLimit.text    = "Limit: ${FormatUtils.formatCurrency(budget.limitAmount, currency)}"
            b.tvPercent.text  = "$pct%"
            b.progressBar.progress = pct

            val tint = if (warning) Color.parseColor("#F59E0B") else Color.parseColor("#4F46E5")
            b.progressBar.progressTintList = android.content.res.ColorStateList.valueOf(tint)
            b.tvPercent.setTextColor(tint)

            if (warning) {
                b.tvWarning.text = "⚠ You've used $pct% of your ${budget.category.lowercase()} budget"
                b.tvWarning.visibility = android.view.View.VISIBLE
            } else {
                b.tvWarning.visibility = android.view.View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemBudgetBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<BudgetEntity>() {
            override fun areItemsTheSame(a: BudgetEntity, b: BudgetEntity) = a.id == b.id
            override fun areContentsTheSame(a: BudgetEntity, b: BudgetEntity) = a == b
        }
    }
}
