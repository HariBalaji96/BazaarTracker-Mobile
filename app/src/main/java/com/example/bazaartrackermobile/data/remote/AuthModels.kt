package com.example.bazaartrackermobile.data.remote

data class LoginRequest(
    val email: String,
    val password: String
)

data class SignupRequest(
    val name: String,
    val email: String,
    val companyName: String,
    val password: String
)

data class AuthResponse(
    val token: String?,
    val message: String?,
    val error: String?
)