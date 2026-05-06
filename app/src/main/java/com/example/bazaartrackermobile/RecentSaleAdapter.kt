package com.example.bazaartrackermobile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bazaartrackermobile.data.remote.RecentSale
import com.example.bazaartrackermobile.databinding.ItemRecentSaleBinding
import com.example.bazaartrackermobile.util.DateUtils
import java.util.Locale

class RecentSaleAdapter(private val onItemClick: (RecentSale) -> Unit) : ListAdapter<RecentSale, RecentSaleAdapter.RecentSaleViewHolder>(RecentSaleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentSaleViewHolder {
        val binding = ItemRecentSaleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecentSaleViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: RecentSaleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RecentSaleViewHolder(
        private val binding: ItemRecentSaleBinding,
        private val onItemClick: (RecentSale) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(sale: RecentSale) {
            val context = binding.root.context
            binding.tvDate.text = DateUtils.formatDateTime(sale.date)
            val status = sale.status.uppercase().trim()
            val (statusText, gradientRes) = when {
                status == "CASH" || status == "PAID" -> {
                    status to R.drawable.bg_gradient_success
                }
                status == "CREDIT" -> {
                    status to R.drawable.bg_gradient_warning
                }
                else -> {
                    status to R.drawable.bg_status_tag
                }
            }
            binding.tvStatus.text = statusText
            binding.tvStatus.setBackgroundResource(gradientRes)
            binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.white))
            
            binding.root.setOnClickListener {
                onItemClick(sale)
            }
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
