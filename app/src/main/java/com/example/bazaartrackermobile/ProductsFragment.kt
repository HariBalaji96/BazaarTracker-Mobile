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
import com.example.bazaartrackermobile.data.remote.ApiService
import com.example.bazaartrackermobile.data.remote.Product
import com.example.bazaartrackermobile.data.remote.RetrofitClient
import com.example.bazaartrackermobile.databinding.FragmentProductsBinding
import com.example.bazaartrackermobile.util.ErrorHandler
import kotlinx.coroutines.launch

class ProductsFragment : Fragment() {

    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!
    private lateinit var apiService: ApiService
    private lateinit var adapter: ProductAdapter
    private var allProducts: List<Product> = emptyList()
    private var selectedUnit: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiService = RetrofitClient.getClient(requireContext()).create(ApiService::class.java)
        
        setupRecyclerView()
        setupListeners()
        fetchProducts()
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter(
            onItemClick = { product ->
                navigateToDetail(product)
            },
            onDeleteClick = { product ->
                showDeleteConfirmation(product)
            }
        )
        binding.rvProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProducts.adapter = adapter
    }

    private fun navigateToDetail(product: Product) {
        val bundle = Bundle().apply {
            putParcelable("product", product)
        }
        findNavController().navigate(R.id.action_navigation_products_to_productDetailFragment, bundle)
    }

    private fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            fetchProducts()
        }
        
        binding.fabAddProduct.setOnClickListener {
            showProductBottomSheet(null)
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.chipActive.setOnCheckedChangeListener { _, _ -> applyFilters() }
        binding.chipInStock.setOnCheckedChangeListener { _, _ -> applyFilters() }
        
        binding.chipCategory.setOnClickListener {
            showUnitFilterDialog()
        }
    }

    private fun showUnitFilterDialog() {
        val units = allProducts.map { it.unit }.distinct().toMutableList()
        units.add(0, "All Units")
        
        AlertDialog.Builder(requireContext())
            .setTitle("Select Unit")
            .setItems(units.toTypedArray()) { _, which ->
                selectedUnit = if (which == 0) null else units[which]
                binding.chipCategory.text = selectedUnit ?: "All Units"
                applyFilters()
            }
            .show()
    }

    private fun applyFilters() {
        val query = binding.etSearch.text?.toString() ?: ""
        val activeOnly = binding.chipActive.isChecked
        val inStockOnly = binding.chipInStock.isChecked

        val filteredList = allProducts.filter { product ->
            val matchesQuery = product.name.contains(query, ignoreCase = true)
            val matchesActive = !activeOnly || product.active
            val matchesStock = !inStockOnly || product.stock > 0
            val matchesUnit = selectedUnit == null || product.unit == selectedUnit
            
            matchesQuery && matchesActive && matchesStock && matchesUnit
        }

        adapter.submitList(filteredList)
        binding.tvEmptyState.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showProductBottomSheet(product: Product?) {
        val bottomSheet = ProductBottomSheet(product) {
            fetchProducts()
        }
        bottomSheet.show(parentFragmentManager, ProductBottomSheet.TAG)
    }

    private fun fetchProducts() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                showLoading(true)
                val response = apiService.getProducts()
                if (response.isSuccessful && response.body() != null) {
                    allProducts = response.body()!!
                    applyFilters()
                } else {
                    if (response.code() == 204 || response.body()?.isEmpty() == true) {
                        allProducts = emptyList()
                        applyFilters()
                    } else {
                        val message = ErrorHandler.parseError(response.code(), response.errorBody()?.string())
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), ErrorHandler.getErrorMessage(e), Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showDeleteConfirmation(product: Product) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_product)
            .setMessage(getString(R.string.delete_product_confirmation, product.name))
            .setPositiveButton(R.string.delete) { _, _ ->
                deleteProduct(product)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun deleteProduct(product: Product) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = apiService.deleteProduct(product.id)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), getString(R.string.product_deleted), Toast.LENGTH_SHORT).show()
                    fetchProducts()
                } else {
                    val message = ErrorHandler.parseError(response.code(), response.errorBody()?.string())
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), ErrorHandler.getErrorMessage(e), Toast.LENGTH_SHORT).show()
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
