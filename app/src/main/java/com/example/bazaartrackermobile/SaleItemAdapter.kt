package com.example.bazaartrackermobile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bazaartrackermobile.data.remote.SaleItem
import com.example.bazaartrackermobile.databinding.ItemSaleProductBinding
import java.util.Locale

class SaleItemAdapter : ListAdapter<SaleItem, SaleItemAdapter.SaleItemViewHolder>(SaleItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaleItemViewHolder {
        val binding = ItemSaleProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SaleItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SaleItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SaleItemViewHolder(private val binding: ItemSaleProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SaleItem) {
            val context = binding.root.context
            binding.tvProductName.text = item.productName ?: "Unknown Product"
            // Reusing a similar format for quantity and price display
            binding.tvPriceQuantity.text = context.getString(R.string.cart_item_format, item.quantity, "", item.price).replace("  ", " ")
            binding.tvLineTotal.text = String.format(Locale.getDefault(), "₹%.2f", item.quantity * item.price)
        }
    }

    class SaleItemDiffCallback : DiffUtil.ItemCallback<SaleItem>() {
        override fun areItemsTheSame(oldItem: SaleItem, newItem: SaleItem): Boolean {
            return oldItem.productId == newItem.productId
        }

        override fun areContentsTheSame(oldItem: SaleItem, newItem: SaleItem): Boolean {
            return oldItem == newItem
        }
    }
}
