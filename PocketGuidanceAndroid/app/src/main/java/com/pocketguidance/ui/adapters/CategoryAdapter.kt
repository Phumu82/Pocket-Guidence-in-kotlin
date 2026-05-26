package com.pocketguidance.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pocketguidance.data.db.entities.CategoryEntity
import com.pocketguidance.databinding.ItemCategoryBinding

class CategoryAdapter(
    private val onDelete: (CategoryEntity) -> Unit
) : ListAdapter<CategoryEntity, CategoryAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val b: ItemCategoryBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(cat: CategoryEntity) {
            b.tvCategoryName.text = cat.name
            b.tvType.text = if (cat.isCustom) "Custom" else "Default"
            b.btnDelete.visibility = if (cat.isCustom) View.VISIBLE else View.GONE
            b.btnDelete.setOnClickListener { onDelete(cat) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<CategoryEntity>() {
            override fun areItemsTheSame(a: CategoryEntity, b: CategoryEntity) = a.id == b.id
            override fun areContentsTheSame(a: CategoryEntity, b: CategoryEntity) = a == b
        }
    }
}
