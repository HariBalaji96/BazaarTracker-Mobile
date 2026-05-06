package com.example.bazaartrackermobile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bazaartrackermobile.data.remote.ApiService
import com.example.bazaartrackermobile.data.remote.Expense
import com.example.bazaartrackermobile.data.remote.RetrofitClient
import com.example.bazaartrackermobile.databinding.FragmentExpensesBinding
import com.example.bazaartrackermobile.util.DateUtils
import com.example.bazaartrackermobile.util.ErrorHandler
import kotlinx.coroutines.launch
import java.util.Locale

class ExpensesFragment : Fragment() {

    private var _binding: FragmentExpensesBinding? = null
    private val binding get() = _binding!!

    private lateinit var apiService: ApiService
    private lateinit var adapter: ExpenseAdapter

    private var allExpenses: List<Expense> = emptyList()

    private var currentCategory: String? = null
    private var currentDateFilter: Int = R.id.chipThisMonth

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

        apiService =
            RetrofitClient.getClient(requireContext())
                .create(ApiService::class.java)

        setupRecyclerView()

        setupFilters()

        setupListeners()

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

        binding.rvExpenses.layoutManager =
            LinearLayoutManager(requireContext())

        binding.rvExpenses.adapter = adapter
    }

    private fun setupFilters() {

        val categories = arrayOf(
            "All",
            "Rent",
            "Utilities",
            "Supplies",
            "Transport",
            "Salary",
            "Maintenance",
            "Travel",
            "Other"
        )

        val categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            categories
        )

        binding.actvCategory.setAdapter(categoryAdapter)

        binding.actvCategory.setText(categories[0], false)

        // CATEGORY FILTER
        binding.actvCategory.setOnItemClickListener { _, _, position, _ ->

            currentCategory = if (position == 0) {
                null
            } else {
                categories[position]
            }

            Log.d(
                "EXPENSE_FILTER",
                "CATEGORY = $currentCategory"
            )

            applyLocalFilters()
        }

        // DATE FILTER
        binding.cgDateFilter.setOnCheckedStateChangeListener { _, checkedIds ->

            currentDateFilter =
                checkedIds.firstOrNull() ?: R.id.chipAll

            Log.d(
                "EXPENSE_FILTER",
                "DATE FILTER = $currentDateFilter"
            )

            applyLocalFilters()
        }
    }

    private fun setupListeners() {

        binding.swipeRefresh.setOnRefreshListener {

            fetchExpenses()
        }

        binding.fabAddExpense.setOnClickListener {

            showExpenseBottomSheet(null)
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {

                applyLocalFilters()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun applyLocalFilters() {

        val query =
            binding.etSearch.text?.toString()?.trim() ?: ""

        val filteredList = allExpenses.filter { expense ->

            // SEARCH FILTER
            val matchesQuery =

                query.isEmpty() ||

                        expense.description.contains(
                            query,
                            ignoreCase = true
                        ) ||

                        expense.category.contains(
                            query,
                            ignoreCase = true
                        )

            // CATEGORY FILTER
            val matchesCategory =

                currentCategory == null ||

                        expense.category.equals(
                            currentCategory,
                            ignoreCase = true
                        )

            // DATE PARSE
            val expenseTime = try {

                DateUtils.parseDate(expense.date)

            } catch (e: Exception) {

                0L
            }

            // DATE FILTER
            val matchesDate = when (currentDateFilter) {

                R.id.chipToday -> {

                    expenseTime >=
                            DateUtils.getStartOfToday().timeInMillis
                }

                R.id.chipThisWeek -> {

                    expenseTime >=
                            DateUtils.getStartOfWeek().timeInMillis
                }

                R.id.chipThisMonth -> {

                    expenseTime >=
                            DateUtils.getStartOfMonth().timeInMillis
                }

                else -> true
            }

            // DEBUG LOGS
            Log.d(
                "EXPENSE_FILTER",
                """
                EXPENSE ID = ${expense.id}
                EXPENSE DATE = ${expense.date}
                EXPENSE TIME = $expenseTime
                MATCH QUERY = $matchesQuery
                MATCH CATEGORY = $matchesCategory
                MATCH DATE = $matchesDate
                """.trimIndent()
            )

            matchesQuery &&
                    matchesCategory &&
                    matchesDate
        }

        Log.d(
            "EXPENSE_FILTER",
            "ALL EXPENSES = ${allExpenses.size}"
        )

        Log.d(
            "EXPENSE_FILTER",
            "FILTERED EXPENSES = ${filteredList.size}"
        )

        adapter.submitList(filteredList)

        adapter.notifyDataSetChanged()

        updateTotalDisplay(filteredList)

        binding.tvEmptyState.visibility =

            if (filteredList.isEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    private fun showExpenseBottomSheet(expense: Expense?) {

        val bottomSheet = ExpenseBottomSheet(expense) {

            fetchExpenses()
        }

        bottomSheet.show(
            parentFragmentManager,
            ExpenseBottomSheet.TAG
        )
    }

    private fun fetchExpenses() {

        viewLifecycleOwner.lifecycleScope.launch {

            try {

                showLoading(true)

                val response = apiService.getExpenses()

                if (response.isSuccessful) {

                    allExpenses =
                        response.body() ?: emptyList()

                    Log.d(
                        "EXPENSE_FILTER",
                        "FETCHED EXPENSES = ${allExpenses.size}"
                    )

                    applyLocalFilters()

                } else {

                    allExpenses = emptyList()

                    applyLocalFilters()

                    val message =
                        ErrorHandler.parseError(
                            response.code(),
                            response.errorBody()?.string()
                        )

                    Toast.makeText(
                        requireContext(),
                        message,
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {

                allExpenses = emptyList()

                applyLocalFilters()

                Toast.makeText(
                    requireContext(),
                    ErrorHandler.getErrorMessage(e),
                    Toast.LENGTH_SHORT
                ).show()

                Log.e(
                    "EXPENSE_FILTER",
                    "ERROR = ${e.message}"
                )

            } finally {

                showLoading(false)
            }
        }
    }

    private fun updateTotalDisplay(expenses: List<Expense>) {

        val total =
            expenses.sumOf { it.amount }

        binding.tvTotalExpenses.text =
            getString(
                R.string.total_expenses_period,
                String.format(
                    Locale.getDefault(),
                    "₹%.2f",
                    total
                )
            )
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

                val response =
                    apiService.deleteExpense(expense.id)

                if (response.isSuccessful) {

                    Toast.makeText(
                        requireContext(),
                        getString(R.string.expense_deleted),
                        Toast.LENGTH_SHORT
                    ).show()

                    fetchExpenses()

                } else {

                    val message =
                        ErrorHandler.parseError(
                            response.code(),
                            response.errorBody()?.string()
                        )

                    Toast.makeText(
                        requireContext(),
                        message,
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {

                Toast.makeText(
                    requireContext(),
                    ErrorHandler.getErrorMessage(e),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {

        if (binding.swipeRefresh.isRefreshing) {

            if (!isLoading) {

                binding.swipeRefresh.isRefreshing = false
            }

        } else {

            binding.progressBar.visibility =

                if (isLoading) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}