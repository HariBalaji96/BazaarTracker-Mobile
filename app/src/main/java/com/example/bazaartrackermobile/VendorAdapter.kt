package com.example.bazaartrackermobile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bazaartrackermobile.data.remote.Vendor
import com.example.bazaartrackermobile.databinding.ItemVendorBinding
import java.util.Locale

class VendorAdapter(
    private val onItemClick: (Vendor) -> Unit,
    private val onEditClick: (Vendor) -> Unit,
    private val onDeleteClick: (Vendor) -> Unit
) : ListAdapter<Vendor, VendorAdapter.VendorViewHolder>(VendorDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VendorViewHolder {
        val binding = ItemVendorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VendorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VendorViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VendorViewHolder(private val binding: ItemVendorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(vendor: Vendor) {
            binding.tvVendorName.text = vendor.name
            binding.tvPhone.text = vendor.phone
            binding.tvAddress.text = vendor.address
            
            binding.tvTotalCredit.text = String.format(Locale.getDefault(), "₹%.2f", vendor.totalCreditGiven)
            binding.tvTotalPaid.text = String.format(Locale.getDefault(), "₹%.2f", vendor.totalPaidAmount)
            binding.tvPending.text = String.format(Locale.getDefault(), "₹%.2f", vendor.pendingAmount)

            if (vendor.pendingAmount > 0) {
                binding.tvPending.setTextColor(ContextCompat.getColor(binding.root.context, android.R.color.holo_red_light))
            } else {
                binding.tvPending.setTextColor(ContextCompat.getColor(binding.root.context, android.R.color.black))
            }

            binding.root.setOnClickListener { onItemClick(vendor) }
            binding.btnEdit.setOnClickListener { onEditClick(vendor) }
            binding.root.setOnLongClickListener {
                onDeleteClick(vendor)
                true
            }
        }
    }

    class VendorDiffCallback : DiffUtil.ItemCallback<Vendor>() {
        override fun areItemsTheSame(oldItem: Vendor, newItem: Vendor): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Vendor, newItem: Vendor): Boolean {
            return oldItem == newItem
        }
    }
}
