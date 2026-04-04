package com.example.bazaartracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PaymentViewModel(application: Application) : AndroidViewModel(application) {
    private val paymentApiService = ApiClient.createService(application, PaymentApiService::class.java)

    private val _payments = MutableStateFlow<List<Payment>>(emptyList())
    val payments: StateFlow<List<Payment>> = _payments

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        fetchPayments()
    }

    fun fetchPayments() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _payments.value = paymentApiService.getPayments()
            } catch (e: Exception) {
                _error.value = "Failed to fetch payments: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addPayment(vendorId: String, amount: Double, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                paymentApiService.createPayment(CreatePaymentRequest(vendorId, amount))
                fetchPayments()
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Failed to record payment: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetState() {
        _payments.value = emptyList()
        _error.value = null
        _isLoading.value = false
    }
}
