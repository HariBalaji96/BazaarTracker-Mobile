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
import com.example.bazaartrackermobile.data.remote.ApiService
import com.example.bazaartrackermobile.data.remote.Payment
import com.example.bazaartrackermobile.data.remote.RetrofitClient
import com.example.bazaartrackermobile.databinding.FragmentPaymentsBinding
import com.example.bazaartrackermobile.util.ErrorHandler
import kotlinx.coroutines.launch

class PaymentsFragment : Fragment() {

    private var _binding: FragmentPaymentsBinding? = null
    private val binding get() = _binding!!
    private lateinit var apiService: ApiService
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

        setupRecyclerView()
        setupListeners()
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

    private fun fetchPayments() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                showLoading(true)
                val response = apiService.getPayments()
                if (response.isSuccessful && response.body() != null) {
                    val payments = response.body()!!
                    adapter.submitList(payments)
                    binding.tvEmptyState.visibility = if (payments.isEmpty()) View.VISIBLE else View.GONE
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
