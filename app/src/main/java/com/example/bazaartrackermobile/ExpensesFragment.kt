package com.example.bazaartrackermobile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bazaartrackermobile.data.local.AppDatabase
import com.example.bazaartrackermobile.data.remote.ApiService
import com.example.bazaartrackermobile.data.remote.Expense
import com.example.bazaartrackermobile.data.remote.RetrofitClient
import com.example.bazaartrackermobile.databinding.FragmentExpensesBinding
import com.example.bazaartrackermobile.util.toDomain
import com.example.bazaartrackermobile.util.toEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class ExpensesFragment : Fragment() {

    private var _binding: FragmentExpensesBinding? = null
    private val binding get() = _binding!!
    private lateinit var apiService: ApiService
    private lateinit var database: AppDatabase
    private lateinit var adapter: ExpenseAdapter
    private var allExpenses: List<Expense> = emptyList()
    
    private var currentCategory: String? = null
    private var startDate: String? = null
    private var endDate: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpensesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiService = RetrofitClient.getClient(requireContext()).create(ApiService::class.java)
        database = AppDatabase.getDatabase(requireContext())

        setupRecyclerView()
        setupFilters()
        setupListeners()
        loadCachedData()
        fetchExpenses()
    }

    private fun setupRecyclerView() {
        adapter = ExpenseAdapter(
            onItemClick = { expense ->
                showExpenseBottomSheet(expense)
            },
            onDeleteClick = { expense ->
                showDeleteConfirmation(expense)
            }
        )
        binding.rvExpenses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExpenses.adapter = adapter
    }

    private fun setupFilters() {
        val categories = arrayOf("All", "Rent", "Salary", "Utility", "Maintenance", "Travel", "Other")
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        binding.actvCategory.setAdapter(categoryAdapter)
        binding.actvCategory.setText(categories[0], false)

        binding.actvCategory.setOnItemClickListener { _, _, position, _ ->
            currentCategory = if (position == 0) null else categories[position]
            fetchExpenses()
        }

        binding.cgDateFilter.setOnCheckedChangeListener { _, checkedId ->
            val calendar = Calendar.getInstance()
            endDate = null
            
            when (checkedId) {
                R.id.chipToday -> {
                    startDate = formatDate(calendar)
                }
                R.id.chipThisWeek -> {
                    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                    startDate = formatDate(calendar)
                }
                R.id.chipThisMonth -> {
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    startDate = formatDate(calendar)
                }
                else -> {
                    startDate = null
                }
            }
            fetchExpenses()
        }
    }

    private fun formatDate(calendar: Calendar): String {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
    }

    private fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            fetchExpenses()
        }

        binding.fabAddExpense.setOnClickListener {
            showExpenseBottomSheet(null)
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyLocalFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun applyLocalFilters() {
        val query = binding.etSearch.text?.toString() ?: ""
        val filteredList = allExpenses.filter { expense ->
            expense.description.contains(query, ignoreCase = true)
        }
        adapter.submitList(filteredList)
        updateTotalDisplay(filteredList)
        binding.tvEmptyState.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showExpenseBottomSheet(expense: Expense?) {
        val bottomSheet = ExpenseBottomSheet(expense) {
            fetchExpenses()
        }
        bottomSheet.show(parentFragmentManager, ExpenseBottomSheet.TAG)
    }

    private fun loadCachedData() {
        viewLifecycleOwner.lifecycleScope.launch {
            val cachedEntities = database.expenseDao().getAllExpenses().first()
            if (cachedEntities.isNotEmpty()) {
                allExpenses = cachedEntities.map { it.toDomain() }
                applyLocalFilters()
            }
        }
    }

    private fun fetchExpenses() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                showLoading(true)
                val response = apiService.getExpenses(currentCategory, startDate, endDate)
                if (response.isSuccessful && response.body() != null) {
                    val expenses = response.body()!!
                    
                    if (currentCategory == null && startDate == null) {
                        database.expenseDao().deleteAllExpenses()
                        database.expenseDao().insertExpenses(expenses.map { it.toEntity() })
                    }
                    
                    allExpenses = expenses
                    applyLocalFilters()
                } else {
                    if (response.code() == 204 || response.body()?.isEmpty() == true) {
                        allExpenses = emptyList()
                        applyLocalFilters()
                    } else {
                        handleFetchError()
                    }
                }
            } catch (e: Exception) {
                handleFetchError()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun updateTotalDisplay(expenses: List<Expense>) {
        val total = expenses.sumOf { it.amount }
        binding.tvTotalExpenses.text = getString(R.string.total_expenses_period, String.format(Locale.getDefault(), "₹%.2f", total))
    }

    private fun handleFetchError() {
        if (allExpenses.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.failed_to_fetch_expenses), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Showing offline data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteConfirmation(expense: Expense) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_expense)
            .setMessage(R.string.delete_expense_confirmation)
            .setPositiveButton(R.string.delete) { _, _ ->
                deleteExpense(expense)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun deleteExpense(expense: Expense) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = apiService.deleteExpense(expense.id)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), getString(R.string.expense_deleted), Toast.LENGTH_SHORT).show()
                    fetchExpenses()
                } else {
                    Toast.makeText(requireContext(), "Failed to delete expense", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (binding.swipeRefresh.isRefreshing) {
            if (!isLoading) binding.swipeRefresh.isRefreshing = false
        } else {
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
