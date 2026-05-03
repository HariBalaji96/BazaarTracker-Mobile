package com.example.bazaartrackermobile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bazaartrackermobile.data.remote.Sale
import com.example.bazaartrackermobile.databinding.ItemSaleBinding
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
            binding.tvSaleDate.text = sale.saleDate
            binding.tvVendorName.text = sale.vendorName ?: "Walking Customer"
            binding.tvTotalAmount.text = String.format(Locale.getDefault(), "₹%.2f", sale.totalAmount)
            binding.tvItemsCount.text = context.getString(R.string.items_count_format, sale.items.size)
            
            binding.tvSaleType.text = sale.saleType
            val typeColor = if (sale.saleType == "CASH") {
                android.R.color.holo_green_light
            } else {
                android.R.color.holo_orange_light
            }
            binding.tvSaleType.setBackgroundColor(ContextCompat.getColor(context, typeColor))

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
