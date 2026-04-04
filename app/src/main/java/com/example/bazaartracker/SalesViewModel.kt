package com.example.bazaartracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SalesViewModel(application: Application) : AndroidViewModel(application) {
    private val salesApiService = ApiClient.createService(application, SalesApiService::class.java)

    private val _sales = MutableStateFlow<List<Sale>>(emptyList())
    val sales: StateFlow<List<Sale>> = _sales

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        fetchSales()
    }

    fun fetchSales() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _sales.value = salesApiService.getSales()
            } catch (e: Exception) {
                _error.value = "Failed to fetch sales: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addSale(request: CreateSaleRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                salesApiService.createSale(request)
                fetchSales()
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Failed to add sale: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteSale(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                salesApiService.deleteSale(id)
                fetchSales()
            } catch (e: Exception) {
                _error.value = "Failed to delete sale: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetState() {
        _sales.value = emptyList()
        _error.value = null
        _isLoading.value = false
    }
}
