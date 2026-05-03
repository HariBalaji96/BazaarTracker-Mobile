package com.example.bazaartrackermobile

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bazaartrackermobile.data.remote.ApiService
import com.example.bazaartrackermobile.data.remote.CreateSaleItemRequest
import com.example.bazaartrackermobile.data.remote.CreateSaleRequest
import com.example.bazaartrackermobile.data.remote.Product
import com.example.bazaartrackermobile.data.remote.RetrofitClient
import com.example.bazaartrackermobile.data.remote.Vendor
import com.example.bazaartrackermobile.databinding.FragmentCreateSaleBinding
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class CreateSaleFragment : Fragment() {

    private var _binding: FragmentCreateSaleBinding? = null
    private val binding get() = _binding!!
    private lateinit var apiService: ApiService
    private lateinit var cartAdapter: CartAdapter

    private var currentStep = 1
    private var vendors: List<Vendor> = emptyList()
    private var products: List<Product> = emptyList()
    
    private var selectedVendor: Vendor? = null
    private var selectedProduct: Product? = null
    private var cartItems = mutableListOf<CartItem>()
    private var saleDate: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateSaleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiService = RetrofitClient.getClient(requireContext()).create(ApiService::class.java)
        
        setupRecyclerView()
        setupListeners()
        fetchData()
        updateStepUI()
        
        // Initialize date
        val calendar = Calendar.getInstance()
        updateDateLabel(calendar)
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter { item ->
            cartItems.remove(item)
            updateCartUI()
        }
        binding.rvCart.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCart.adapter = cartAdapter
    }

    private fun setupListeners() {
        binding.btnNext.setOnClickListener {
            if (validateCurrentStep()) {
                if (currentStep < 4) {
                    currentStep++
                    updateStepUI()
                } else {
                    submitSale()
                }
            }
        }

        binding.btnBack.setOnClickListener {
            if (currentStep > 1) {
                currentStep--
                updateStepUI()
            }
        }

        binding.btnAddProduct.setOnClickListener {
            addToCart()
        }

        binding.actvVendor.setOnItemClickListener { _, _, position, _ ->
            selectedVendor = vendors[position]
        }

        binding.actvProduct.setOnItemClickListener { _, _, position, _ ->
            selectedProduct = products[position]
            updateProductInfo()
        }

        binding.etDate.setOnClickListener {
            showDatePicker()
        }
        
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun updateStepUI() {
        binding.layoutStep1.visibility = if (currentStep == 1) View.VISIBLE else View.GONE
        binding.layoutStep2.visibility = if (currentStep == 2) View.VISIBLE else View.GONE
        binding.layoutStep3.visibility = if (currentStep == 3) View.VISIBLE else View.GONE
        binding.layoutStep4.visibility = if (currentStep == 4) View.VISIBLE else View.GONE

        binding.btnBack.visibility = if (currentStep > 1) View.VISIBLE else View.INVISIBLE
        binding.btnNext.text = if (currentStep == 4) getString(R.string.submit) else getString(R.string.next)
        
        if (currentStep == 4) prepareReview()
    }

    private fun validateCurrentStep(): Boolean {
        return when (currentStep) {
            1 -> {
                if (selectedVendor == null) {
                    binding.tilVendor.error = getString(R.string.field_required)
                    false
                } else {
                    binding.tilVendor.error = null
                    true
                }
            }
            2 -> true
            3 -> {
                if (cartItems.isEmpty()) {
                    Toast.makeText(requireContext(), "Add at least one product", Toast.LENGTH_SHORT).show()
                    false
                } else {
                    true
                }
            }
            else -> true
        }
    }

    private fun addToCart() {
        val product = selectedProduct ?: return
        val qtyText = binding.etQuantity.text.toString()
        if (qtyText.isEmpty()) {
            binding.tilQuantity.error = getString(R.string.field_required)
            return
        }
        
        val qty = qtyText.toInt()
        if (qty > product.stock) {
            binding.tilQuantity.error = getString(R.string.insufficient_stock)
            return
        }
        
        binding.tilQuantity.error = null
        
        val existing = cartItems.find { it.productId == product.id }
        if (existing != null) {
            cartItems.remove(existing)
            cartItems.add(existing.copy(quantity = existing.quantity + qty))
        } else {
            cartItems.add(CartItem(product.id, product.name, qty, product.price, product.unit))
        }
        
        updateCartUI()
        binding.etQuantity.text?.clear()
    }

    private fun updateCartUI() {
        cartAdapter.submitList(cartItems.toList())
        val total = cartItems.sumOf { it.quantity * it.price }
        binding.tvTotalAmount.text = getString(R.string.total_format, total)
    }

    private fun updateProductInfo() {
        selectedProduct?.let {
            binding.tvProductInfo.text = getString(R.string.price_stock_format, it.price, it.stock, it.unit)
        }
    }

    private fun prepareReview() {
        val type = if (binding.rbCash.isChecked) "CASH" else "CREDIT"
        binding.tvReviewSummary.text = getString(
            R.string.review_summary_format,
            selectedVendor?.name ?: "",
            type,
            cartItems.size
        )
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
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
        saleDate = String.format("%04d-%02d-%02d", year, month, day)
        binding.etDate.setText(saleDate)
    }

    private fun fetchData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                showLoading(true)
                val vResponse = apiService.getVendors()
                if (vResponse.isSuccessful) {
                    vendors = vResponse.body() ?: emptyList()
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, vendors.map { it.name })
                    binding.actvVendor.setAdapter(adapter)
                }

                val pResponse = apiService.getProducts()
                if (pResponse.isSuccessful) {
                    products = pResponse.body() ?: emptyList()
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, products.map { it.name })
                    binding.actvProduct.setAdapter(adapter)
                }
            } catch (e: Exception) {
                // Error
            } finally {
                showLoading(false)
            }
        }
    }

    private fun submitSale() {
        val type = if (binding.rbCash.isChecked) "CASH" else "CREDIT"
        val request = CreateSaleRequest(
            vendorId = selectedVendor!!.id,
            saleType = type,
            items = cartItems.map { CreateSaleItemRequest(it.productId, it.quantity, it.price) }
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                showLoading(true)
                val response = apiService.createSale(request)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), getString(R.string.sale_created), Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(requireContext(), getString(R.string.failed_to_create_sale), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnNext.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
