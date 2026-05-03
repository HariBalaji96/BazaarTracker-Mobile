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
import com.example.bazaartrackermobile.data.remote.Vendor
import com.example.bazaartrackermobile.databinding.FragmentVendorDetailBinding
import kotlinx.coroutines.launch
import java.util.Locale

class VendorDetailFragment : Fragment() {

    private var _binding: FragmentVendorDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var apiService: ApiService
    private lateinit var currentVendor: Vendor
    private lateinit var salesAdapter: RecentSaleAdapter
    private lateinit var paymentsAdapter: PaymentRecordAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVendorDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiService = RetrofitClient.getClient(requireContext()).create(ApiService::class.java)

        val vendorArg = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("vendor", Vendor::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("vendor")
        }

        if (vendorArg == null) {
            findNavController().navigateUp()
            return
        }
        currentVendor = vendorArg

        setupToolbar()
        setupRecyclerViews()
        setupUI(currentVendor)
        setupListeners()
        fetchData()
    }

    private fun setupToolbar() {
        binding.toolbar.title = getString(R.string.vendors)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerViews() {
        salesAdapter = RecentSaleAdapter()
        binding.rvRecentSales.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecentSales.adapter = salesAdapter

        paymentsAdapter = PaymentRecordAdapter()
        binding.rvPayments.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPayments.adapter = paymentsAdapter
    }

    private fun setupUI(vendor: Vendor) {
        binding.tvVendorName.text = vendor.name
        binding.tvPhone.text = vendor.phone
        binding.tvAddress.text = vendor.address

        binding.tvTotalCredit.text = String.format(Locale.getDefault(), "₹%.2f", vendor.totalCreditGiven)
        binding.tvTotalPaid.text = String.format(Locale.getDefault(), "₹%.2f", vendor.totalPaidAmount)
        binding.tvPending.text = String.format(Locale.getDefault(), "₹%.2f", vendor.pendingAmount)

        // Financial Summary: Color formatting for Pending/Outstanding amount
        if (vendor.pendingAmount > 0) {
            binding.tvPending.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_light))
        } else {
            binding.tvPending.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
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
        val bottomSheet = VendorBottomSheet(currentVendor) {
            refreshVendorData()
        }
        bottomSheet.show(parentFragmentManager, VendorBottomSheet.TAG)
    }

    private fun refreshVendorData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = apiService.getVendor(currentVendor.id)
                if (response.isSuccessful && response.body() != null) {
                    currentVendor = response.body()!!
                    setupUI(currentVendor)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun fetchData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val salesResponse = apiService.getVendorRecentSales(currentVendor.id)
                if (salesResponse.isSuccessful && salesResponse.body() != null) {
                    val sales = salesResponse.body()!!
                    salesAdapter.submitList(sales)
                    binding.tvNoSales.visibility = if (sales.isEmpty()) View.VISIBLE else View.GONE
                }

                val paymentsResponse = apiService.getVendorPayments(currentVendor.id)
                if (paymentsResponse.isSuccessful && paymentsResponse.body() != null) {
                    val payments = paymentsResponse.body()!!
                    paymentsAdapter.submitList(payments)
                    binding.tvNoPayments.visibility = if (payments.isEmpty()) View.VISIBLE else View.GONE
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_vendor)
            .setMessage(getString(R.string.delete_vendor_confirmation, currentVendor.name))
            .setPositiveButton(R.string.delete) { _, _ ->
                deleteVendor()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun deleteVendor() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = apiService.deleteVendor(currentVendor.id)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), getString(R.string.vendor_deleted), Toast.LENGTH_SHORT).show()
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
