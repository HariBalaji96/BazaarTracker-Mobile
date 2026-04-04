package com.example.bazaartracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductViewModel(application: Application) : AndroidViewModel(application) {
    private val productApiService = ApiClient.createService(application, ProductApiService::class.java)

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        fetchProducts()
    }

    fun fetchProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = productApiService.getProducts()
                _products.value = response
            } catch (e: Exception) {
                _error.value = "Failed to fetch products: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addProduct(name: String, price: Double, stock: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val request = CreateProductRequest(name, price, stock)
                val response = productApiService.createProduct(request)
                fetchProducts()
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Failed to add product: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProduct(id: String, name: String, price: Double, stock: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val request = CreateProductRequest(name, price, stock)
                val response = productApiService.updateProduct(id, request)
                fetchProducts()
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Failed to update product: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteProduct(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                productApiService.deleteProduct(id)
                fetchProducts()
            } catch (e: Exception) {
                _error.value = "Failed to delete product: ${e.message}"
                fetchProducts()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetState() {
        _products.value = emptyList()
        _error.value = null
        _isLoading.value = false
    }
}
