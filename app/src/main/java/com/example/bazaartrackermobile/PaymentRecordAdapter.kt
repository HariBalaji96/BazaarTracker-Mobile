package com.example.bazaartrackermobile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bazaartrackermobile.data.remote.PaymentRecord
import com.example.bazaartrackermobile.databinding.ItemPaymentRecordBinding
import com.example.bazaartrackermobile.util.DateUtils
import java.util.Locale

class PaymentRecordAdapter(
    private val onLongClick: (PaymentRecord) -> Unit = {}
) : ListAdapter<PaymentRecord, PaymentRecordAdapter.PaymentRecordViewHolder>(PaymentRecordDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentRecordViewHolder {
        val binding = ItemPaymentRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PaymentRecordViewHolder(binding, onLongClick)
    }

    override fun onBindViewHolder(holder: PaymentRecordViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PaymentRecordViewHolder(
        private val binding: ItemPaymentRecordBinding,
        private val onLongClick: (PaymentRecord) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(payment: PaymentRecord) {
            binding.tvDate.text = DateUtils.formatDateTime(payment.date)
            binding.tvMethod.text = payment.method
            binding.tvAmount.text = String.format(Locale.getDefault(), "₹%.2f", payment.amount)
            
            binding.root.setOnLongClickListener {
                onLongClick(payment)
                true
            }
        }
    }

    class PaymentRecordDiffCallback : DiffUtil.ItemCallback<PaymentRecord>() {
        override fun areItemsTheSame(oldItem: PaymentRecord, newItem: PaymentRecord): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PaymentRecord, newItem: PaymentRecord): Boolean {
            return oldItem == newItem
        }
    }
}
