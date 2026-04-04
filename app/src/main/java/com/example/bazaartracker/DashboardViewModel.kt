package com.example.bazaartracker

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

sealed class DashboardState {
    object Loading : DashboardState()
    data class Success(val data: DashboardResponse) : DashboardState()
    data class Error(val message: String) : DashboardState()
}

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val dashboardService = ApiClient.createService(application, DashboardApiService::class.java)

    var dashboardState by mutableStateOf<DashboardState>(DashboardState.Loading)
        private set

    init {
        fetchDashboardData()
    }

    fun fetchDashboardData() {
        viewModelScope.launch {
            dashboardState = DashboardState.Loading
            try {
                val response = dashboardService.getDashboardData()
                dashboardState = DashboardState.Success(response)
            } catch (e: Exception) {
                dashboardState = DashboardState.Error(e.message ?: "Failed to load dashboard")
            }
        }
    }

    fun resetState() {
        dashboardState = DashboardState.Loading
    }
}
