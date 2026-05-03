package com.example.bazaartrackermobile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bazaartrackermobile.data.remote.StockLog
import com.example.bazaartrackermobile.databinding.ItemStockLogBinding

class StockLogAdapter : ListAdapter<StockLog, StockLogAdapter.StockLogViewHolder>(StockLogDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockLogViewHolder {
        val binding = ItemStockLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StockLogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StockLogViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class StockLogViewHolder(private val binding: ItemStockLogBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(log: StockLog) {
            binding.tvReason.text = log.reason
            binding.tvDate.text = log.createdAt
            
            val isAddition = log.type == "IN"
            val prefix = if (isAddition) "+" else "-"
            binding.tvQuantity.text = "$prefix${log.quantity}"
            
            val colorRes = if (isAddition) android.R.color.holo_green_light else android.R.color.holo_red_light
            val color = ContextCompat.getColor(binding.root.context, colorRes)
            binding.tvQuantity.setTextColor(color)
            binding.viewTypeIndicator.setBackgroundColor(color)
        }
    }

    class StockLogDiffCallback : DiffUtil.ItemCallback<StockLog>() {
        override fun areItemsTheSame(oldItem: StockLog, newItem: StockLog): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: StockLog, newItem: StockLog): Boolean {
            return oldItem == newItem
        }
    }
}
