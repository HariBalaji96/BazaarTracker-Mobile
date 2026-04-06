package com.example.bazaartracker.data.api

import com.example.bazaartracker.data.models.DashboardResponse
import retrofit2.http.GET

interface DashboardApiService {
    @GET("api/dashboard")
    suspend fun getDashboardData(): DashboardResponse
}
