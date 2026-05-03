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
        binding.tvSaleDate.text = sale.saleDate
        binding.tvTotalAmount.text = String.format(Locale.getDefault(), "₹%.2f", sale.totalAmount)
        
        binding.tvSaleType.text = sale.saleType
        val typeColor = if (sale.saleType == "CASH") {
            android.R.color.holo_green_light
        } else {
            android.R.color.holo_orange_light
        }
        binding.tvSaleType.setBackgroundColor(ContextCompat.getColor(requireContext(), typeColor))

        adapter.submitList(sale.items)

        if (sale.saleType == "CREDIT") {
            binding.cardPayment.visibility = View.VISIBLE
            // Here we would ideally fetch the actual pending amount for this specific sale
            // For now, displaying as a placeholder
            binding.tvPaymentStatus.text = "Outstanding balance: ${String.format(Locale.getDefault(), "₹%.2f", sale.totalAmount)}"
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
            // TODO: Implementation for recording payment
            Toast.makeText(requireContext(), "Record Payment coming soon", Toast.LENGTH_SHORT).show()
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
