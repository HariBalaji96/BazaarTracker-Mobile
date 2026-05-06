package com.example.bazaartrackermobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.bazaartrackermobile.data.remote.ApiService
import com.example.bazaartrackermobile.data.remote.DashboardResponse
import com.example.bazaartrackermobile.data.remote.RetrofitClient
import com.example.bazaartrackermobile.databinding.FragmentDashboardBinding
import com.example.bazaartrackermobile.util.DateUtils
import com.example.bazaartrackermobile.util.ErrorHandler
import kotlinx.coroutines.launch
import java.util.*

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var apiService: ApiService
    private var currentData: DashboardResponse? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiService = RetrofitClient.getClient(requireContext()).create(ApiService::class.java)

        setupListeners()
        fetchDashboardData()
    }

    private fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            fetchDashboardData()
        }
    }

    private fun fetchDashboardData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                binding.swipeRefresh.isRefreshing = true
                val response = apiService.getDashboardData()
                
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    currentData = data
                    
                    updateSummaryUI(data, System.currentTimeMillis())
                } else {
                    val message = ErrorHandler.parseError(response.code(), response.errorBody()?.string())
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), ErrorHandler.getErrorMessage(e), Toast.LENGTH_SHORT).show()
            } finally {
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun updateSummaryUI(data: DashboardResponse, timestamp: Long) {
        binding.tvTotalSales.text = String.format(Locale.getDefault(), "₹%.2f", data.totalSales)
        binding.tvTotalExpenses.text = String.format(Locale.getDefault(), "₹%.2f", data.totalExpenses)
        binding.tvProfit.text = String.format(Locale.getDefault(), "₹%.2f", data.profit)
        binding.tvTotalPayments.text = String.format(Locale.getDefault(), "₹%.2f", data.totalPayments)
        binding.tvTotalCredit.text = String.format(Locale.getDefault(), "₹%.2f", data.totalCredit)

        val relativeTime = DateUtils.getRelativeTimeSpan(timestamp)
        binding.tvLastUpdated.text = getString(R.string.last_updated, relativeTime)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
