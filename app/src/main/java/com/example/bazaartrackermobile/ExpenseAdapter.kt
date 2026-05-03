package com.example.bazaartrackermobile

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
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

            // Color indicator based on category (simple hash-based color for variety)
            val color = getCategoryColor(expense.category)
            binding.viewCategoryIndicator.setBackgroundColor(color)

            binding.root.setOnClickListener { onItemClick(expense) }
            binding.root.setOnLongClickListener {
                onDeleteClick(expense)
                true
            }
        }

        private fun getCategoryColor(category: String): Int {
            return when (category.lowercase()) {
                "rent" -> Color.parseColor("#E91E63")
                "salary" -> Color.parseColor("#4CAF50")
                "utility" -> Color.parseColor("#2196F3")
                "maintenance" -> Color.parseColor("#FF9800")
                "travel" -> Color.parseColor("#9C27B0")
                else -> Color.parseColor("#607D8B")
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
