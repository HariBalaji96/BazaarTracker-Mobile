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
import com.example.bazaartrackermobile.data.remote.RetrofitClient
import com.example.bazaartrackermobile.data.remote.Sale
import com.example.bazaartrackermobile.databinding.FragmentSaleDetailBinding
import com.example.bazaartrackermobile.util.DateUtils
import kotlinx.coroutines.launch
import java.util.Locale

class SaleDetailFragment : Fragment() {

    private var _binding: FragmentSaleDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var apiService: ApiService
    private lateinit var adapter: SaleItemAdapter
    private lateinit var currentSale: Sale

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSaleDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiService = RetrofitClient.getClient(requireContext()).create(ApiService::class.java)

        val saleArg = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("sale", Sale::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("sale")
        }

        if (saleArg == null) {
            findNavController().navigateUp()
            return
        }
        currentSale = saleArg

        setupToolbar()
        setupRecyclerView()
        setupUI(currentSale)
        setupListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.title = getString(R.string.sale_details)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        adapter = SaleItemAdapter()
        binding.rvSaleItems.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSaleItems.adapter = adapter
    }

    private fun setupUI(sale: Sale) {
        binding.tvSaleId.text = "#${sale.id}"
        binding.tvVendorName.text = sale.vendorName ?: "Walking Customer"
        binding.tvSaleDate.text = DateUtils.formatDateTime(sale.saleDate)
        binding.tvTotalAmount.text = String.format(Locale.getDefault(), "₹%.2f", sale.totalAmount)
        
        val type = sale.saleType.uppercase().trim()
        val pending = sale.pendingAmount ?: if (type == "CREDIT") sale.totalAmount else 0.0

        val (statusText, gradientRes) = when {
            type == "CASH" -> {
                getString(R.string.cash).uppercase() to R.drawable.bg_gradient_success
            }
            type == "CREDIT" && pending <= 0 -> {
                getString(R.string.paid).uppercase() to R.drawable.bg_gradient_success
            }
            type == "CREDIT" -> {
                getString(R.string.credit).uppercase() to R.drawable.bg_gradient_warning
            }
            else -> {
                type to R.drawable.bg_status_tag
            }
        }

        binding.tvSaleType.text = statusText
        binding.tvSaleType.setBackgroundResource(gradientRes)
        binding.tvSaleType.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

        adapter.submitList(sale.items)

        if (type == "CREDIT" || pending > 0) {
            binding.cardPayment.visibility = View.VISIBLE
            val status = if (pending <= 0) {
                getString(R.string.paid)
            } else {
                "${getString(R.string.pending)}: ${String.format(Locale.getDefault(), "₹%.2f", pending)}"
            }
            binding.tvPaymentStatus.text = status
            
            // Hide record payment button if already fully paid
            binding.btnRecordPayment.visibility = if (pending > 0) View.VISIBLE else View.GONE
        } else {
            binding.cardPayment.visibility = View.GONE
        }
    }

    private fun setupListeners() {
        binding.btnEdit.setOnClickListener {
            // TODO: Implementation for editing sale
            Toast.makeText(requireContext(), "Edit Sale coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }

        binding.btnRecordPayment.setOnClickListener {
            showPaymentBottomSheet()
        }
    }

    private fun showPaymentBottomSheet() {
        val type = currentSale.saleType.uppercase().trim()
        val pending = currentSale.pendingAmount ?: if (type == "CREDIT") currentSale.totalAmount else 0.0

        if (pending <= 0) {
            Toast.makeText(requireContext(), "Sale is already fully paid", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = apiService.getVendor(currentSale.vendorId)
                if (response.isSuccessful && response.body() != null) {
                    val vendor = response.body()!!
                    val bottomSheet = PaymentBottomSheet(vendor, currentSale) {
                        refreshSaleData()
                    }
                    bottomSheet.show(parentFragmentManager, PaymentBottomSheet.TAG)
                } else {
                    Toast.makeText(requireContext(), "Cannot record payment: Vendor information missing", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun refreshSaleData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = apiService.getSale(currentSale.id)
                if (response.isSuccessful && response.body() != null) {
                    currentSale = response.body()!!
                    setupUI(currentSale)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_sale)
            .setMessage(R.string.delete_sale_confirmation)
            .setPositiveButton(R.string.delete) { _, _ ->
                deleteSale()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun deleteSale() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = apiService.deleteSale(currentSale.id)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), getString(R.string.sale_deleted), Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(requireContext(), "Failed to delete sale", Toast.LENGTH_SHORT).show()
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
