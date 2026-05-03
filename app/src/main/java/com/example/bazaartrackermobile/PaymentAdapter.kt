package com.example.bazaartrackermobile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bazaartrackermobile.data.remote.Payment
import com.example.bazaartrackermobile.databinding.ItemPaymentBinding
import java.util.Locale

class PaymentAdapter(
    private val onItemClick: (Payment) -> Unit,
    private val onDeleteClick: (Payment) -> Unit
) : ListAdapter<Payment, PaymentAdapter.PaymentViewHolder>(PaymentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val binding = ItemPaymentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PaymentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PaymentViewHolder(private val binding: ItemPaymentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(payment: Payment) {
            val context = binding.root.context
            binding.tvVendorName.text = payment.vendorName ?: "Unknown Vendor"
            binding.tvAmount.text = String.format(Locale.getDefault(), "₹%.2f", payment.amount)
            binding.tvPaymentMethod.text = payment.paymentMethod
            binding.tvDate.text = payment.date
            binding.tvPaymentId.text = context.getString(R.string.payment_id, payment.id.takeLast(8))

            binding.root.setOnClickListener { onItemClick(payment) }
            binding.root.setOnLongClickListener {
                onDeleteClick(payment)
                true
            }
        }
    }

    class PaymentDiffCallback : DiffUtil.ItemCallback<Payment>() {
        override fun areItemsTheSame(oldItem: Payment, newItem: Payment): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Payment, newItem: Payment): Boolean {
            return oldItem == newItem
        }
    }
}
