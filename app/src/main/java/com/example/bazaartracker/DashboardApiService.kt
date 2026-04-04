package com.example.bazaartracker

import retrofit2.http.GET

interface DashboardApiService {
    @GET("api/dashboard")
    suspend fun getDashboardData(): DashboardResponse
}
