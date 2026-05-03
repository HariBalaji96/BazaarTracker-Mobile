package com.example.bazaartrackermobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bazaartrackermobile.data.local.AppDatabase
import com.example.bazaartrackermobile.data.remote.ApiService
import com.example.bazaartrackermobile.data.remote.Payment
import com.example.bazaartrackermobile.data.remote.RetrofitClient
import com.example.bazaartrackermobile.databinding.FragmentPaymentsBinding
import com.example.bazaartrackermobile.util.toDomain
import com.example.bazaartrackermobile.util.toEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PaymentsFragment : Fragment() {

    private var _binding: FragmentPaymentsBinding? = null
    private val binding get() = _binding!!
    private lateinit var apiService: ApiService
    private lateinit var database: AppDatabase
    private lateinit var adapter: PaymentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiService = RetrofitClient.getClient(requireContext()).create(ApiService::class.java)
        database = AppDatabase.getDatabase(requireContext())

        setupRecyclerView()
        setupListeners()
        loadCachedData()
        fetchPayments()
    }

    private fun setupRecyclerView() {
        adapter = PaymentAdapter(
            onItemClick = { payment ->
                Toast.makeText(requireContext(), "Payment: ${payment.id}", Toast.LENGTH_SHORT).show()
            },
            onDeleteClick = { payment ->
                showDeleteConfirmation(payment)
            }
        )
        binding.rvPayments.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPayments.adapter = adapter
    }

    private fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            fetchPayments()
        }

        binding.fabRecordPayment.setOnClickListener {
            showPaymentBottomSheet()
        }
    }

    private fun showPaymentBottomSheet() {
        val bottomSheet = PaymentBottomSheet {
            fetchPayments()
        }
        bottomSheet.show(parentFragmentManager, PaymentBottomSheet.TAG)
    }

    private fun loadCachedData() {
        viewLifecycleOwner.lifecycleScope.launch {
            val cachedEntities = database.paymentDao().getAllPayments().first()
            if (cachedEntities.isNotEmpty()) {
                adapter.submitList(cachedEntities.map { it.toDomain() })
            }
        }
    }

    private fun fetchPayments() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                showLoading(true)
                val response = apiService.getPayments()
                if (response.isSuccessful && response.body() != null) {
                    val payments = response.body()!!
                    
                    // Update cache
                    database.paymentDao().deleteAllPayments()
                    database.paymentDao().insertPayments(payments.map { it.toEntity() })
                    
                    adapter.submitList(payments)
                    binding.tvEmptyState.visibility = if (payments.isEmpty()) View.VISIBLE else View.GONE
                } else {
                    handleFetchError()
                }
            } catch (e: Exception) {
                handleFetchError()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun handleFetchError() {
        if (adapter.currentList.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.failed_to_fetch_payments), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Showing offline data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteConfirmation(payment: Payment) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_payment)
            .setMessage(R.string.delete_payment_confirmation)
            .setPositiveButton(R.string.delete) { _, _ ->
                deletePayment(payment)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun deletePayment(payment: Payment) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = apiService.deletePayment(payment.id)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), getString(R.string.payment_deleted), Toast.LENGTH_SHORT).show()
                    fetchPayments()
                } else {
                    Toast.makeText(requireContext(), "Failed to delete payment", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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
