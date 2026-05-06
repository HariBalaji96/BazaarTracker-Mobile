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
import com.example.bazaartrackermobile.data.remote.RetrofitClient
import com.example.bazaartrackermobile.data.remote.Vendor
import com.example.bazaartrackermobile.databinding.FragmentVendorsBinding
import com.example.bazaartrackermobile.util.ErrorHandler
import kotlinx.coroutines.launch

class VendorsFragment : Fragment() {

    private var _binding: FragmentVendorsBinding? = null
    private val binding get() = _binding!!
    private lateinit var apiService: ApiService
    private lateinit var adapter: VendorAdapter
    private var allVendors: List<Vendor> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVendorsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiService = RetrofitClient.getClient(requireContext()).create(ApiService::class.java)

        setupRecyclerView()
        setupListeners()
        fetchVendors()
    }

    private fun setupRecyclerView() {
        adapter = VendorAdapter(
            onItemClick = { vendor ->
                navigateToDetail(vendor)
            },
            onEditClick = { vendor ->
                showVendorBottomSheet(vendor)
            },
            onDeleteClick = { vendor ->
                showDeleteConfirmation(vendor)
            }
        )
        binding.rvVendors.layoutManager = LinearLayoutManager(requireContext())
        binding.rvVendors.adapter = adapter
    }

    private fun navigateToDetail(vendor: Vendor) {
        val bundle = Bundle().apply {
            putParcelable("vendor", vendor)
        }
        findNavController().navigate(R.id.action_navigation_vendors_to_vendorDetailFragment, bundle)
    }

    private fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            fetchVendors()
        }

        binding.fabAddVendor.setOnClickListener {
            showVendorBottomSheet(null)
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.chipOutstanding.setOnCheckedChangeListener { _, _ -> applyFilters() }
    }

    private fun applyFilters() {
        val query = binding.etSearch.text?.toString() ?: ""
        val outstandingOnly = binding.chipOutstanding.isChecked

        val filteredList = allVendors.filter { vendor ->
            val matchesQuery = vendor.name.contains(query, ignoreCase = true)
            val matchesOutstanding = !outstandingOnly || vendor.pendingAmount > 0
            
            matchesQuery && matchesOutstanding
        }
        adapter.submitList(filteredList)
        binding.tvEmptyState.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showVendorBottomSheet(vendor: Vendor?) {
        val bottomSheet = VendorBottomSheet(vendor) {
            fetchVendors()
        }
        bottomSheet.show(parentFragmentManager, VendorBottomSheet.TAG)
    }

    private fun fetchVendors() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                showLoading(true)
                val response = apiService.getVendors()
                if (response.isSuccessful && response.body() != null) {
                    allVendors = response.body()!!
                    applyFilters()
                } else {
                    val message = ErrorHandler.parseError(response.code(), response.errorBody()?.string())
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), ErrorHandler.getErrorMessage(e), Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showDeleteConfirmation(vendor: Vendor) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_vendor)
            .setMessage(getString(R.string.delete_vendor_confirmation, vendor.name))
            .setPositiveButton(R.string.delete) { _, _ ->
                deleteVendor(vendor)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun deleteVendor(vendor: Vendor) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = apiService.deleteVendor(vendor.id)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), getString(R.string.vendor_deleted), Toast.LENGTH_SHORT).show()
                    fetchVendors()
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
