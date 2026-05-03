package com.example.bazaartrackermobile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bazaartrackermobile.data.local.AppDatabase
import com.example.bazaartrackermobile.data.remote.ApiService
import com.example.bazaartrackermobile.data.remote.RetrofitClient
import com.example.bazaartrackermobile.data.remote.Sale
import com.example.bazaartrackermobile.databinding.FragmentSalesBinding
import com.example.bazaartrackermobile.util.toDomain
import com.example.bazaartrackermobile.util.toEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class SalesFragment : Fragment() {

    private var _binding: FragmentSalesBinding? = null
    private val binding get() = _binding!!
    private lateinit var apiService: ApiService
    private lateinit var database: AppDatabase
    private lateinit var adapter: SaleAdapter
    private var allSales: List<Sale> = emptyList()

    private var currentType: String? = null
    private var startDate: String? = null
    private var endDate: String? = null

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
        apiService = RetrofitClient.getClient(requireContext()).create(ApiService::class.java)
        database = AppDatabase.getDatabase(requireContext())

        setupRecyclerView()
        setupFilters()
        setupListeners()
        loadCachedData()
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
        binding.rvSales.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSales.adapter = adapter
    }

    private fun navigateToDetail(sale: Sale) {
        val bundle = Bundle().apply {
            putParcelable("sale", sale)
        }
        findNavController().navigate(R.id.action_navigation_sales_to_saleDetailFragment, bundle)
    }

    private fun setupFilters() {
        binding.cgTypeFilter.setOnCheckedChangeListener { _, checkedId ->
            currentType = when (checkedId) {
                R.id.chipCash -> "CASH"
                R.id.chipCredit -> "CREDIT"
                else -> null
            }
            fetchSales()
        }

        binding.cgDateFilter.setOnCheckedChangeListener { _, checkedId ->
            val calendar = Calendar.getInstance()
            endDate = null
            
            when (checkedId) {
                R.id.chipLast7Days -> {
                    calendar.add(Calendar.DAY_OF_YEAR, -7)
                    startDate = formatDate(calendar)
                }
                R.id.chipLast30Days -> {
                    calendar.add(Calendar.DAY_OF_YEAR, -30)
                    startDate = formatDate(calendar)
                }
                R.id.chipCustomRange -> {
                    Toast.makeText(requireContext(), "Custom range coming soon", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    startDate = null
                }
            }
            fetchSales()
        }
    }

    private fun formatDate(calendar: Calendar): String {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return String.format("%04d-%02d-%02d", year, month, day)
    }

    private fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            fetchSales()
        }

        binding.fabAddSale.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_sales_to_createSaleFragment)
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
        val filteredList = allSales.filter { sale ->
            val matchesVendor = sale.vendorName?.contains(query, ignoreCase = true) == true
            val matchesId = sale.id.contains(query, ignoreCase = true)
            matchesVendor || matchesId
        }
        adapter.submitList(filteredList)
        binding.tvEmptyState.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun loadCachedData() {
        viewLifecycleOwner.lifecycleScope.launch {
            val cachedEntities = database.saleDao().getAllSales().first()
            if (cachedEntities.isNotEmpty()) {
                allSales = cachedEntities.map { it.toDomain() }
                applyLocalFilters()
            }
        }
    }

    private fun fetchSales() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                showLoading(true)
                val response = apiService.getSales(currentType, startDate, endDate)
                if (response.isSuccessful && response.body() != null) {
                    val sales = response.body()!!
                    
                    if (currentType == null && startDate == null) {
                        database.saleDao().deleteAllSales()
                        database.saleDao().insertSales(sales.map { it.toEntity() })
                    }
                    
                    allSales = sales
                    applyLocalFilters()
                } else {
                    if (response.code() == 204 || response.body()?.isEmpty() == true) {
                        allSales = emptyList()
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

    private fun handleFetchError() {
        if (allSales.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.failed_to_fetch_sales), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Showing offline data", Toast.LENGTH_SHORT).show()
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
                val response = apiService.deleteSale(sale.id)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), getString(R.string.sale_deleted), Toast.LENGTH_SHORT).show()
                    fetchSales()
                } else {
                    Toast.makeText(requireContext(), "Failed to delete sale", Toast.LENGTH_SHORT).show()
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
