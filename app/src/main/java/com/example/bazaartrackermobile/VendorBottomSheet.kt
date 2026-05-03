package com.example.bazaartrackermobile

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.bazaartrackermobile.data.remote.ApiService
import com.example.bazaartrackermobile.data.remote.RetrofitClient
import com.example.bazaartrackermobile.data.remote.Vendor
import com.example.bazaartrackermobile.data.remote.VendorRequest
import com.example.bazaartrackermobile.databinding.BottomSheetVendorBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class VendorBottomSheet(
    private val vendor: Vendor? = null,
    private val onDismiss: () -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetVendorBinding? = null
    private val binding get() = _binding!!
    private lateinit var apiService: ApiService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetVendorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiService = RetrofitClient.getClient(requireContext()).create(ApiService::class.java)

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        if (vendor != null) {
            binding.tvTitle.text = getString(R.string.edit_vendor)
            binding.etName.setText(vendor.name)
            binding.etPhone.setText(vendor.phone)
            binding.etAddress.setText(vendor.address)
            binding.btnSave.text = getString(R.string.update_product) // Reusing existing string for 'Update'
        } else {
            binding.tvTitle.text = getString(R.string.add_vendor)
            binding.btnSave.text = getString(R.string.add_vendor)
        }
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            if (validateInputs()) {
                saveVendor()
            }
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

        val phone = binding.etPhone.text.toString()
        if (phone.isBlank()) {
            binding.tilPhone.error = getString(R.string.field_required)
            isValid = false
        } else if (!Patterns.PHONE.matcher(phone).matches()) {
            binding.tilPhone.error = getString(R.string.invalid_phone_format)
            isValid = false
        } else {
            binding.tilPhone.error = null
        }

        if (binding.etAddress.text.isNullOrBlank()) {
            binding.tilAddress.error = getString(R.string.field_required)
            isValid = false
        } else {
            binding.tilAddress.error = null
        }

        return isValid
    }

    private fun saveVendor() {
        val request = VendorRequest(
            name = binding.etName.text.toString(),
            phone = binding.etPhone.text.toString(),
            address = binding.etAddress.text.toString(),
            active = vendor?.active ?: true
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                showLoading(true)
                val response = if (vendor == null) {
                    apiService.createVendor(request)
                } else {
                    apiService.updateVendor(vendor.id, request)
                }

                if (response.isSuccessful) {
                    val message = if (vendor == null) R.string.vendor_created else R.string.vendor_updated
                    Toast.makeText(requireContext(), getString(message), Toast.LENGTH_SHORT).show()
                    onDismiss()
                    dismiss()
                } else {
                    Toast.makeText(requireContext(), getString(R.string.failed_to_save_vendor), Toast.LENGTH_SHORT).show()
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
        binding.layoutButtons.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
        isCancelable = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "VendorBottomSheet"
    }
}
