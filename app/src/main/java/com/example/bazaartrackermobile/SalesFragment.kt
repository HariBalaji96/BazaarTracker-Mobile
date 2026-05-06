package com.example.bazaartrackermobile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bazaartrackermobile.data.remote.ApiService
import com.example.bazaartrackermobile.data.remote.RetrofitClient
import com.example.bazaartrackermobile.data.remote.Sale
import com.example.bazaartrackermobile.databinding.FragmentSalesBinding
import com.example.bazaartrackermobile.util.DateUtils
import com.example.bazaartrackermobile.util.ErrorHandler
import kotlinx.coroutines.launch

class SalesFragment : Fragment() {

    private var _binding: FragmentSalesBinding? = null
    private val binding get() = _binding!!

    private lateinit var apiService: ApiService
    private lateinit var adapter: SaleAdapter

    private var allSales: List<Sale> = emptyList()

    private var currentType: String? = null
    private var currentDateFilter: Int = R.id.chipThisMonth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSalesBinding.inflate(inflater, container, false)

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

        fetchSales()
    }

    private fun setupRecyclerView() {

        adapter = SaleAdapter(

            onItemClick = { sale ->
                navigateToDetail(sale)
            },

            onDeleteClick = { sale ->
                showDeleteConfirmation(sale)
            }
        )

        binding.rvSales.layoutManager =
            LinearLayoutManager(requireContext())

        binding.rvSales.adapter = adapter
    }

    private fun navigateToDetail(sale: Sale) {

        val bundle = Bundle().apply {
            putParcelable("sale", sale)
        }

        findNavController().navigate(
            R.id.action_navigation_sales_to_saleDetailFragment,
            bundle
        )
    }

    private fun setupFilters() {

        // TYPE FILTER
        binding.cgTypeFilter.setOnCheckedStateChangeListener { _, checkedIds ->

            currentType = when (checkedIds.firstOrNull()) {

                R.id.chipCash -> "CASH"

                R.id.chipCredit -> "CREDIT"

                else -> null
            }

            Log.d("FILTER_DEBUG", "TYPE FILTER = $currentType")

            applyLocalFilters()
        }

        // DATE FILTER
        binding.cgDateFilter.setOnCheckedStateChangeListener { _, checkedIds ->

            currentDateFilter =
                checkedIds.firstOrNull() ?: R.id.chipAllDates

            Log.d("FILTER_DEBUG", "DATE FILTER = $currentDateFilter")

            applyLocalFilters()
        }
    }

    private fun setupListeners() {

        binding.swipeRefresh.setOnRefreshListener {

            fetchSales()
        }

        binding.fabAddSale.setOnClickListener {

            findNavController().navigate(
                R.id.action_navigation_sales_to_createSaleFragment
            )
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

        val filteredList = allSales.filter { sale ->

            // SEARCH FILTER
            val matchesQuery =

                query.isEmpty() ||

                        sale.vendorName
                            ?.contains(query, ignoreCase = true) == true ||

                        sale.id.contains(query, ignoreCase = true)

            // TYPE FILTER
            val matchesType =

                currentType == null ||

                        sale.saleType
                            .trim()
                            .equals(currentType, ignoreCase = true)

            // DATE PARSE
            val saleTime = try {

                DateUtils.parseDate(sale.saleDate)

            } catch (e: Exception) {

                0L
            }

            // DATE FILTER
            val matchesDate = when (currentDateFilter) {

                R.id.chipToday -> {

                    saleTime >=
                            DateUtils.getStartOfToday().timeInMillis
                }

                R.id.chipThisWeek -> {

                    saleTime >=
                            DateUtils.getStartOfWeek().timeInMillis
                }

                R.id.chipThisMonth -> {

                    saleTime >=
                            DateUtils.getStartOfMonth().timeInMillis
                }

                else -> true
            }

            // DEBUG LOGS
            Log.d(
                "FILTER_DEBUG",
                """
                SALE ID = ${sale.id}
                SALE DATE = ${sale.saleDate}
                SALE TIME = $saleTime
                MATCH QUERY = $matchesQuery
                MATCH TYPE = $matchesType
                MATCH DATE = $matchesDate
                """.trimIndent()
            )

            matchesQuery &&
                    matchesType &&
                    matchesDate
        }

        Log.d("FILTER_DEBUG", "ALL SALES = ${allSales.size}")

        Log.d("FILTER_DEBUG", "FILTERED SALES = ${filteredList.size}")

        adapter.submitList(filteredList)

        adapter.notifyDataSetChanged()

        binding.tvEmptyState.visibility =

            if (filteredList.isEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    private fun fetchSales() {

        viewLifecycleOwner.lifecycleScope.launch {

            try {

                showLoading(true)

                val response = apiService.getSales()

                if (response.isSuccessful) {

                    allSales =
                        response.body() ?: emptyList()

                    Log.d(
                        "FILTER_DEBUG",
                        "FETCHED SALES = ${allSales.size}"
                    )

                    applyLocalFilters()

                } else {

                    allSales = emptyList()

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

                allSales = emptyList()

                applyLocalFilters()

                Toast.makeText(
                    requireContext(),
                    ErrorHandler.getErrorMessage(e),
                    Toast.LENGTH_SHORT
                ).show()

                Log.e(
                    "FILTER_DEBUG",
                    "ERROR = ${e.message}"
                )

            } finally {

                showLoading(false)
            }
        }
    }

    private fun showDeleteConfirmation(sale: Sale) {

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_sale)
            .setMessage(R.string.delete_sale_confirmation)
            .setPositiveButton(R.string.delete) { _, _ ->

                deleteSale(sale)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun deleteSale(sale: Sale) {

        viewLifecycleOwner.lifecycleScope.launch {

            try {

                val response =
                    apiService.deleteSale(sale.id)

                if (response.isSuccessful) {

                    Toast.makeText(
                        requireContext(),
                        getString(R.string.sale_deleted),
                        Toast.LENGTH_SHORT
                    ).show()

                    fetchSales()

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