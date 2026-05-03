package com.example.bazaartrackermobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.bazaartrackermobile.data.remote.ApiService
import com.example.bazaartrackermobile.data.remote.Product
import com.example.bazaartrackermobile.data.remote.ProductRequest
import com.example.bazaartrackermobile.data.remote.RetrofitClient
import com.example.bazaartrackermobile.databinding.BottomSheetProductBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class ProductBottomSheet(
    private val product: Product? = null,
    private val onDismiss: () -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetProductBinding? = null
    private val binding get() = _binding!!
    private lateinit var apiService: ApiService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiService = RetrofitClient.getClient(requireContext()).create(ApiService::class.java)

        setupUnitDropdown()
        setupUI()
        setupListeners()
    }

    private fun setupUnitDropdown() {
        val units = arrayOf("kg", "liter", "piece", "box", "packet", "dozen")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, units)
        binding.actvUnit.setAdapter(adapter)
    }

    private fun setupUI() {
        if (product != null) {
            binding.tvTitle.text = getString(R.string.edit_product)
            binding.tvProductId.text = getString(R.string.product_id, product.id)
            binding.tvProductId.visibility = View.VISIBLE
            binding.etName.setText(product.name)
            binding.etPrice.setText(product.price.toString())
            binding.etStock.setText(product.stock.toString())
            binding.actvUnit.setText(product.unit, false)
            binding.btnSave.text = getString(R.string.update_product)
            binding.btnDelete.visibility = View.VISIBLE
        } else {
            binding.tvTitle.text = getString(R.string.create_product)
            binding.btnSave.text = getString(R.string.create_product)
        }
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            if (validateInputs()) {
                saveProduct()
            }
        }

        binding.btnDelete.setOnClickListener {
            deleteProduct()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (binding.etName.text.isNullOrBlank()) {
            binding.tilName.error = getString(R.string.field_required)
            isValid = false
        } else {
            binding.tilName.error = null
        }

        if (binding.etPrice.text.isNullOrBlank()) {
            binding.tilPrice.error = getString(R.string.field_required)
            isValid = false
        } else {
            binding.tilPrice.error = null
        }

        if (binding.etStock.text.isNullOrBlank()) {
            binding.tilStock.error = getString(R.string.field_required)
            isValid = false
        } else {
            binding.tilStock.error = null
        }

        if (binding.actvUnit.text.isNullOrBlank()) {
            binding.tilUnit.error = getString(R.string.field_required)
            isValid = false
        } else {
            binding.tilUnit.error = null
        }

        return isValid
    }

    private fun saveProduct() {
        val request = ProductRequest(
            name = binding.etName.text.toString(),
            price = binding.etPrice.text.toString().toDouble(),
            stock = binding.etStock.text.toString().toInt(),
            unit = binding.actvUnit.text.toString(),
            active = product?.active ?: true
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                showLoading(true)
                val response = if (product == null) {
                    apiService.createProduct(request)
                } else {
                    apiService.updateProduct(product.id, request)
                }

                if (response.isSuccessful) {
                    val message = if (product == null) R.string.product_created else R.string.product_updated
                    Toast.makeText(requireContext(), getString(message), Toast.LENGTH_SHORT).show()
                    onDismiss()
                    dismiss()
                } else {
                    Toast.makeText(requireContext(), getString(R.string.failed_to_save_product), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun deleteProduct() {
        product?.let {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    showLoading(true)
                    val response = apiService.deleteProduct(it.id)
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), getString(R.string.product_deleted), Toast.LENGTH_SHORT).show()
                        onDismiss()
                        dismiss()
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.failed_to_delete_product), Toast.LENGTH_SHORT).show()
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
        const val TAG = "ProductBottomSheet"
    }
}
