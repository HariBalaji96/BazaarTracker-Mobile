package com.example.bazaartrackermobile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bazaartrackermobile.data.remote.Product
import com.example.bazaartrackermobile.databinding.ItemProductBinding
import java.util.Locale

class ProductAdapter(
    private val onItemClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            val context = binding.root.context
            binding.tvProductName.text = product.name
            binding.tvProductPrice.text = String.format(Locale.getDefault(), "₹%.2f", product.price)
            binding.tvProductStock.text = context.getString(R.string.product_stock_format, product.stock, product.unit)

            val statusColor = if (product.active) {
                android.R.color.holo_green_light
            } else {
                android.R.color.darker_gray
            }
            binding.viewActiveStatus.setBackgroundResource(statusColor)

            binding.root.setOnClickListener { onItemClick(product) }
            binding.btnDelete.setOnClickListener { onDeleteClick(product) }
        }
    }

    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
}
