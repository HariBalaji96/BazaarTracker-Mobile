package com.example.bazaartrackermobile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bazaartrackermobile.databinding.ItemCartProductBinding
import java.util.Locale

data class CartItem(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val price: Double,
    val unit: String
)

class CartAdapter(
    private val onRemoveClick: (CartItem) -> Unit
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(CartDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CartViewHolder(private val binding: ItemCartProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) {
            val context = binding.root.context
            binding.tvProductName.text = item.productName
            binding.tvPriceQuantity.text = context.getString(R.string.cart_item_format, item.quantity, item.unit, item.price)
            binding.tvLineTotal.text = String.format(Locale.getDefault(), "₹%.2f", item.quantity * item.price)
            
            binding.btnRemove.setOnClickListener { onRemoveClick(item) }
        }
    }

    class CartDiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.productId == newItem.productId
        }

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem == newItem
        }
    }
}
