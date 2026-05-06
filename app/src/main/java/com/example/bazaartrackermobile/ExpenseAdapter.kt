package com.example.bazaartrackermobile

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bazaartrackermobile.data.remote.Expense
import com.example.bazaartrackermobile.databinding.ItemExpenseBinding
import java.util.Locale

class ExpenseAdapter(
    private val onItemClick: (Expense) -> Unit,
    private val onDeleteClick: (Expense) -> Unit
) : ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(ExpenseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ExpenseViewHolder(private val binding: ItemExpenseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(expense: Expense) {
            binding.tvCategory.text = expense.category
            binding.tvDescription.text = expense.description
            binding.tvDate.text = expense.date
            binding.tvAmount.text = String.format(Locale.getDefault(), "₹%.2f", expense.amount)

            // Color indicator based on category
            val indicatorRes = getCategoryGradient(expense.category)
            binding.viewCategoryIndicator.setBackgroundResource(indicatorRes)

            binding.tvCategory.setBackgroundResource(indicatorRes)
            binding.tvCategory.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))
            binding.tvCategory.backgroundTintList = null // Remove tint to show gradient

            binding.root.setOnClickListener { onItemClick(expense) }
            binding.root.setOnLongClickListener {
                onDeleteClick(expense)
                true
            }
        }

        private fun getCategoryGradient(category: String): Int {
            return when (category.lowercase()) {
                "rent" -> R.drawable.bg_gradient_danger
                "salary" -> R.drawable.bg_gradient_success
                "utility" -> R.drawable.bg_gradient_info
                "maintenance" -> R.drawable.bg_gradient_warning
                "travel" -> R.drawable.bg_gradient_primary
                else -> R.drawable.bg_status_tag
            }
        }
    }

    class ExpenseDiffCallback : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem == newItem
        }
    }
}
