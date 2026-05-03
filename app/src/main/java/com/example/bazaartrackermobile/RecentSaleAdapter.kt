package com.example.bazaartrackermobile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bazaartrackermobile.data.remote.RecentSale
import com.example.bazaartrackermobile.databinding.ItemRecentSaleBinding
import java.util.Locale

class RecentSaleAdapter : ListAdapter<RecentSale, RecentSaleAdapter.RecentSaleViewHolder>(RecentSaleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentSaleViewHolder {
        val binding = ItemRecentSaleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecentSaleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecentSaleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RecentSaleViewHolder(private val binding: ItemRecentSaleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(sale: RecentSale) {
            binding.tvDate.text = sale.date
            binding.tvStatus.text = sale.status
            binding.tvAmount.text = String.format(Locale.getDefault(), "₹%.2f", sale.totalAmount)
        }
    }

    class RecentSaleDiffCallback : DiffUtil.ItemCallback<RecentSale>() {
        override fun areItemsTheSame(oldItem: RecentSale, newItem: RecentSale): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RecentSale, newItem: RecentSale): Boolean {
            return oldItem == newItem
        }
    }
}
