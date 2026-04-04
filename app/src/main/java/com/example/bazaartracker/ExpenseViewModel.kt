package com.example.bazaartracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val expenseApiService = ApiClient.createService(application, ExpenseApiService::class.java)

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        fetchExpenses()
    }

    fun fetchExpenses() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _expenses.value = expenseApiService.getExpenses()
            } catch (e: Exception) {
                _error.value = "Failed to fetch expenses: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addExpense(category: String, amount: Double, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                expenseApiService.createExpense(CreateExpenseRequest(category, amount))
                fetchExpenses()
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Failed to add expense: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateExpense(id: String, category: String, amount: Double, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                expenseApiService.updateExpense(id, CreateExpenseRequest(category, amount))
                fetchExpenses()
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Failed to update expense: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteExpense(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                expenseApiService.deleteExpense(id)
                fetchExpenses()
            } catch (e: Exception) {
                _error.value = "Failed to delete expense: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetState() {
        _expenses.value = emptyList()
        _error.value = null
        _isLoading.value = false
    }
}
