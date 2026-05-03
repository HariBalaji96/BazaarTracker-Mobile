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
import com.example.bazaartrackermobile.data.remote.PaymentRequest
import com.example.bazaartrackermobile.data.remote.RetrofitClient
import com.example.bazaartrackermobile.data.remote.Vendor
import com.example.bazaartrackermobile.databinding.BottomSheetPaymentBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class PaymentBottomSheet(
    private val preSelectedVendor: Vendor? = null,
    private val onDismiss: () -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetPaymentBinding? = null
    private val binding get() = _binding!!
    private lateinit var apiService: ApiService
    private var vendors: List<Vendor> = emptyList()
    private var selectedVendor: Vendor? = null
    private var selectedDate: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiService = RetrofitClient.getClient(requireContext()).create(ApiService::class.java)

        selectedVendor = preSelectedVendor
        setupDropdowns()
        setupListeners()
        fetchVendors()
        
        val calendar = Calendar.getInstance()
        updateDateLabel(calendar)
        
        if (selectedVendor != null) {
            binding.actvVendor.setText(selectedVendor!!.name, false)
            binding.tilVendor.isEnabled = false
        }
    }

    private fun setupDropdowns() {
        val methods = arrayOf("Cash", "Check", "Bank Transfer", "Online", "Other")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, methods)
        binding.actvMethod.setAdapter(adapter)
    }

    private fun setupListeners() {
        binding.actvVendor.setOnItemClickListener { _, _, position, _ ->
            selectedVendor = vendors[position]
        }

        binding.etDate.setOnClickListener {
            showDatePicker()
        }

        binding.btnRecord.setOnClickListener {
            if (validateInputs()) {
                recordPayment()
            }
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun fetchVendors() {
        if (preSelectedVendor != null) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = apiService.getVendors()
                if (response.isSuccessful) {
                    vendors = response.body() ?: emptyList()
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, vendors.map { it.name })
                    binding.actvVendor.setAdapter(adapter)
                }
            } catch (e: Exception) {
                // Error
            }
        }
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
        selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
        binding.etDate.setText(selectedDate)
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (selectedVendor == null) {
            binding.tilVendor.error = getString(R.string.field_required)
            isValid = false
        } else {
            binding.tilVendor.error = null
        }

        val amountStr = binding.etAmount.text.toString()
        if (amountStr.isBlank() || amountStr.toDouble() <= 0) {
            binding.tilAmount.error = getString(R.string.invalid_amount)
            isValid = false
        } else {
            binding.tilAmount.error = null
        }

        if (binding.actvMethod.text.isNullOrBlank()) {
            binding.tilMethod.error = getString(R.string.field_required)
            isValid = false
        } else {
            binding.tilMethod.error = null
        }

        return isValid
    }

    private fun recordPayment() {
        val request = PaymentRequest(
            vendorId = selectedVendor!!.id,
            amount = binding.etAmount.text.toString().toDouble(),
            paymentMethod = binding.actvMethod.text.toString(),
            date = selectedDate,
            description = binding.etNotes.text.toString()
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                showLoading(true)
                val response = apiService.recordPayment(request)
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    val balance = String.format(Locale.getDefault(), "₹%.2f", data.remainingBalance)
                    Toast.makeText(requireContext(), getString(R.string.payment_recorded, balance), Toast.LENGTH_LONG).show()
                    onDismiss()
                    dismiss()
                } else {
                    Toast.makeText(requireContext(), getString(R.string.failed_to_record_payment), Toast.LENGTH_SHORT).show()
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
        const val TAG = "PaymentBottomSheet"
    }
}
