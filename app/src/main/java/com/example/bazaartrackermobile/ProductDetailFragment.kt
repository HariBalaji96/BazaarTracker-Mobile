package com.example.bazaartrackermobile

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bazaartrackermobile.data.remote.ApiService
import com.example.bazaartrackermobile.data.remote.Product
import com.example.bazaartrackermobile.data.remote.RetrofitClient
import com.example.bazaartrackermobile.databinding.FragmentProductDetailBinding
import kotlinx.coroutines.launch
import java.util.Locale

class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var apiService: ApiService
    private lateinit var adapter: StockLogAdapter
    private lateinit var currentProduct: Product

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiService = RetrofitClient.getClient(requireContext()).create(ApiService::class.java)
        
        val productArg = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("product", Product::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("product")
        }

        if (productArg == null) {
            findNavController().navigateUp()
            return
        }
        
        currentProduct = productArg

        setupToolbar()
        setupRecyclerView()
        setupUI(currentProduct)
        setupListeners()
        fetchStockLogs()
    }

    private fun setupToolbar() {
        binding.toolbar.title = getString(R.string.products)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        adapter = StockLogAdapter()
        binding.rvStockLogs.layoutManager = LinearLayoutManager(requireContext())
        binding.rvStockLogs.adapter = adapter
    }

    private fun setupUI(product: Product) {
        binding.tvProductName.text = product.name
        binding.tvPrice.text = String.format(Locale.getDefault(), "₹%.2f", product.price)
        binding.tvStock.text = getString(R.string.product_stock_format, product.stock, product.unit)

        if (product.active) {
            binding.tvStatus.text = getString(R.string.active)
            binding.tvStatus.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_light))
        } else {
            binding.tvStatus.text = getString(R.string.inactive)
            binding.tvStatus.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
        }
    }

    private fun setupListeners() {
        binding.btnEdit.setOnClickListener {
            showEditBottomSheet()
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun showEditBottomSheet() {
        val bottomSheet = ProductBottomSheet(currentProduct) {
            refreshProductData()
        }
        bottomSheet.show(parentFragmentManager, ProductBottomSheet.TAG)
    }

    private fun refreshProductData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = apiService.getProduct(currentProduct.id)
                if (response.isSuccessful && response.body() != null) {
                    currentProduct = response.body()!!
                    setupUI(currentProduct)
                    fetchStockLogs()
                }
            } catch (e: Exception) {
                // Silently fail or log
            }
        }
    }

    private fun fetchStockLogs() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = apiService.getProductStockLogs(currentProduct.id)
                if (response.isSuccessful && response.body() != null) {
                    val logs = response.body()!!
                    adapter.submitList(logs)
                    binding.tvNoLogs.visibility = if (logs.isEmpty()) View.VISIBLE else View.GONE
                }
            } catch (e: Exception) {
                // Silently fail or log
            }
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_product)
            .setMessage(getString(R.string.delete_product_confirmation, currentProduct.name))
            .setPositiveButton(R.string.delete) { _, _ ->
                deleteProduct()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun deleteProduct() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = apiService.deleteProduct(currentProduct.id)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), getString(R.string.product_deleted), Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
