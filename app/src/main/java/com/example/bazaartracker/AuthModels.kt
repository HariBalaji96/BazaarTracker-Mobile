package com.example.bazaartracker

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String
)

data class SignupRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String
)
