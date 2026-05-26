package com.pocketguidance.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pocketguidance.data.db.entities.TransactionEntity
import com.pocketguidance.databinding.ItemTransactionBinding
import com.pocketguidance.utils.FormatUtils

class TransactionAdapter(
    private val currency: String,
    private val onReceiptClick: (TransactionEntity) -> Unit
) : ListAdapter<TransactionEntity, TransactionAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val b: ItemTransactionBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(tx: TransactionEntity) {
            val isPositive = tx.type == "income" || tx.type == "received"
            val prefix     = if (isPositive) "+" else "-"
            val color      = if (isPositive) Color.parseColor("#22C55E") else Color.parseColor("#EF4444")

            b.tvAmount.text      = "$prefix${FormatUtils.formatCurrency(tx.amount, currency)}"
            b.tvAmount.setTextColor(color)
            b.tvCategory.text    = tx.category
            b.tvDescription.text = tx.description.ifBlank { tx.category }
            b.tvDate.text        = FormatUtils.formatDisplayDate(tx.date)
            b.tvType.text        = tx.type.replace("_", " ").replaceFirstChar { it.uppercase() }

            val iconRes = if (isPositive) android.R.drawable.arrow_up_float else android.R.drawable.arrow_down_float
            b.ivTypeIcon.setImageResource(iconRes)

            if (tx.receiptPhotoPath != null) {
                b.ivReceiptIcon.visibility = View.VISIBLE
                b.ivReceiptIcon.setOnClickListener { onReceiptClick(tx) }
            } else {
                b.ivReceiptIcon.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<TransactionEntity>() {
            override fun areItemsTheSame(a: TransactionEntity, b: TransactionEntity) = a.id == b.id
            override fun areContentsTheSame(a: TransactionEntity, b: TransactionEntity) = a == b
        }
    }
}
