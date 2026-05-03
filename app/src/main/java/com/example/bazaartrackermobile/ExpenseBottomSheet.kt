package com.example.bazaartrackermobile

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.bazaartrackermobile.data.remote.ApiService
import com.example.bazaartrackermobile.data.remote.Expense
import com.example.bazaartrackermobile.data.remote.ExpenseRequest
import com.example.bazaartrackermobile.data.remote.RetrofitClient
import com.example.bazaartrackermobile.databinding.BottomSheetExpenseBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class ExpenseBottomSheet(
    private val expense: Expense? = null,
    private val onDismiss: () -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetExpenseBinding? = null
    private val binding get() = _binding!!
    private lateinit var apiService: ApiService
    private var selectedDate: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiService = RetrofitClient.getClient(requireContext()).create(ApiService::class.java)

        setupCategoryDropdown()
        setupUI()
        setupListeners()
    }

    private fun setupCategoryDropdown() {
        // Simplified category list. In a real app, this might have icons via a custom adapter.
        val categories = arrayOf("Rent", "Utilities", "Supplies", "Transport", "Salary", "Maintenance", "Travel", "Other")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        binding.actvCategory.setAdapter(adapter)
    }

    private fun setupUI() {
        if (expense != null) {
            binding.tvTitle.text = getString(R.string.edit_expense)
            binding.tvExpenseId.text = getString(R.string.expense_id, expense.id)
            binding.tvExpenseId.visibility = View.VISIBLE
            
            binding.actvCategory.setText(expense.category, false)
            binding.etAmount.setText(String.format(Locale.US, "%.2f", expense.amount))
            binding.etDescription.setText(expense.description)
            binding.etDate.setText(expense.date)
            selectedDate = expense.date
            
            binding.btnSave.text = getString(R.string.update_product) // Reusing 'Update' label
            binding.btnDelete.visibility = View.VISIBLE
        } else {
            binding.tvTitle.text = getString(R.string.add_expense)
            binding.btnSave.text = getString(R.string.submit)
            
            val calendar = Calendar.getInstance()
            updateDateLabel(calendar)
        }
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            if (validateInputs()) {
                saveExpense()
            }
        }

        binding.btnDelete.setOnClickListener {
            deleteExpense()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.etDate.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        // If editing, try to parse current date
        if (selectedDate.isNotEmpty()) {
            val parts = selectedDate.split("-")
            if (parts.size == 3) {
                calendar.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
            }
        }

        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val selected = Calendar.getInstance()
                selected.set(year, month, day)
                updateDateLabel(selected)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateLabel(calendar: Calendar) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
        binding.etDate.setText(selectedDate)
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (binding.actvCategory.text.isNullOrBlank()) {
            binding.tilCategory.error = getString(R.string.field_required)
            isValid = false
        } else {
            binding.tilCategory.error = null
        }

        val amountStr = binding.etAmount.text.toString()
        if (amountStr.isBlank()) {
            binding.tilAmount.error = getString(R.string.field_required)
            isValid = false
        } else {
            val amount = amountStr.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                binding.tilAmount.error = getString(R.string.invalid_amount)
                isValid = false
            } else {
                binding.tilAmount.error = null
            }
        }

        if (binding.etDescription.text.isNullOrBlank()) {
            binding.tilDescription.error = getString(R.string.field_required)
            isValid = false
        } else {
            binding.tilDescription.error = null
        }

        return isValid
    }

    private fun saveExpense() {
        val request = ExpenseRequest(
            category = binding.actvCategory.text.toString(),
            amount = binding.etAmount.text.toString().toDouble(),
            description = binding.etDescription.text.toString(),
            date = selectedDate
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                showLoading(true)
                val response = if (expense == null) {
                    apiService.createExpense(request)
                } else {
                    apiService.updateExpense(expense.id, request)
                }

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Expense saved successfully", Toast.LENGTH_SHORT).show()
                    onDismiss()
                    dismiss()
                } else {
                    Toast.makeText(requireContext(), "Failed to save expense", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun deleteExpense() {
        expense?.let {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    showLoading(true)
                    val response = apiService.deleteExpense(it.id)
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), getString(R.string.expense_deleted), Toast.LENGTH_SHORT).show()
                        onDismiss()
                        dismiss()
                    } else {
                        Toast.makeText(requireContext(), "Failed to delete expense", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    showLoading(false)
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.layoutButtons.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
        isCancelable = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ExpenseBottomSheet"
    }
}
