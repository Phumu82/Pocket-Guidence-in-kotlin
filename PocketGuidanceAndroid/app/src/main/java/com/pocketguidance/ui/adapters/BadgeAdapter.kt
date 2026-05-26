package com.pocketguidance.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pocketguidance.data.db.entities.BadgeEntity
import com.pocketguidance.databinding.ItemBadgeBinding

class BadgeAdapter(private var badges: List<BadgeEntity>) :
    RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder>() {

    class BadgeViewHolder(val binding: ItemBadgeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
        val binding = ItemBadgeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BadgeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
        val badge = badges[position]
        holder.binding.tvBadgeIcon.text = badge.icon
        holder.binding.tvBadgeName.text = badge.name
    }

    override fun getItemCount() = badges.size

    fun updateData(newBadges: List<BadgeEntity>) {
        badges = newBadges
        notifyDataSetChanged()
    }
}
