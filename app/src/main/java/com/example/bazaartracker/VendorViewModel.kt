package com.example.bazaartracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VendorViewModel(application: Application) : AndroidViewModel(application) {
    private val vendorApiService = ApiClient.createService(application, VendorApiService::class.java)

    private val _vendors = MutableStateFlow<List<Vendor>>(emptyList())
    val vendors: StateFlow<List<Vendor>> = _vendors

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        fetchVendors()
    }

    fun fetchVendors() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _vendors.value = vendorApiService.getVendors()
            } catch (e: Exception) {
                _error.value = "Failed to fetch vendors: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addVendor(name: String, phone: String, address: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                vendorApiService.createVendor(CreateVendorRequest(name, phone, address))
                fetchVendors()
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Failed to add vendor: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateVendor(id: String, name: String?, phone: String?, address: String?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                vendorApiService.updateVendor(id, PartialVendorRequest(name, phone, address))
                fetchVendors()
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Failed to update vendor: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteVendor(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                vendorApiService.deleteVendor(id)
                fetchVendors()
            } catch (e: Exception) {
                _error.value = "Failed to delete vendor: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetState() {
        _vendors.value = emptyList()
        _error.value = null
        _isLoading.value = false
    }
}
