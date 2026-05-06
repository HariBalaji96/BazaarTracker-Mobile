package com.example.bazaartrackermobile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bazaartrackermobile.data.remote.Sale
import com.example.bazaartrackermobile.databinding.ItemSaleBinding
import com.example.bazaartrackermobile.util.DateUtils
import java.util.Locale

class SaleAdapter(
    private val onItemClick: (Sale) -> Unit,
    private val onDeleteClick: (Sale) -> Unit
) : ListAdapter<Sale, SaleAdapter.SaleViewHolder>(SaleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaleViewHolder {
        val binding = ItemSaleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SaleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SaleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SaleViewHolder(private val binding: ItemSaleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(sale: Sale) {
            val context = binding.root.context
            binding.tvSaleId.text = "#${sale.id.takeLast(6)}"
            binding.tvSaleDate.text = DateUtils.formatDateTime(sale.saleDate)
            binding.tvVendorName.text = sale.vendorName ?: context.getString(R.string.all)
            binding.tvTotalAmount.text = String.format(Locale.getDefault(), "₹%.2f", sale.totalAmount)
            binding.tvItemsCount.text = context.getString(R.string.items_count_format, sale.items.size)
            
            val type = sale.saleType.uppercase().trim()
            // If pendingAmount is missing from API, assume full total is pending for CREDIT sales
            val pending = sale.pendingAmount ?: if (type == "CREDIT") sale.totalAmount else 0.0
            
            val (statusText, gradientRes) = when {
                type == "CASH" -> {
                    context.getString(R.string.cash).uppercase() to R.drawable.bg_gradient_success
                }
                type == "CREDIT" && pending <= 0 -> {
                    context.getString(R.string.paid).uppercase() to R.drawable.bg_gradient_success
                }
                type == "CREDIT" -> {
                    context.getString(R.string.credit).uppercase() to R.drawable.bg_gradient_warning
                }
                else -> {
                    type to R.drawable.bg_status_tag
                }
            }

            binding.tvSaleType.text = statusText
            binding.tvSaleType.setBackgroundResource(gradientRes)
            binding.tvSaleType.setTextColor(ContextCompat.getColor(context, R.color.white))

            // Update status indicator color based on type
            val indicatorRes = when {
                type == "CASH" || (type == "CREDIT" && pending <= 0) -> R.drawable.bg_gradient_success
                type == "CREDIT" -> R.drawable.bg_gradient_warning
                else -> R.drawable.bg_gradient_primary
            }
            binding.statusIndicator.setBackgroundResource(indicatorRes)

            binding.root.setOnClickListener { onItemClick(sale) }
            binding.root.setOnLongClickListener {
                onDeleteClick(sale)
                true
            }
        }
    }

    class SaleDiffCallback : DiffUtil.ItemCallback<Sale>() {
        override fun areItemsTheSame(oldItem: Sale, newItem: Sale): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Sale, newItem: Sale): Boolean {
            return oldItem == newItem
        }
    }
}
