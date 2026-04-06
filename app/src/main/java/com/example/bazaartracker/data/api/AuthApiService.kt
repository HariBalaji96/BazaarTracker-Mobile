package com.example.bazaartracker.data.api

import com.example.bazaartracker.data.models.LoginRequest
import com.example.bazaartracker.data.models.LoginResponse
import com.example.bazaartracker.data.models.SignupRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/auth/signup")
    suspend fun signup(@Body request: SignupRequest): Unit
}
