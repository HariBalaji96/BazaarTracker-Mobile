package com.example.bazaartrackermobile.data.remote

data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val companyName: String,
    val role: String,
    val isActive: Boolean
)

data class UpdateUserRequest(
    val name: String,
    val companyName: String
)

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)

data class MessageResponse(
    val message: String?,
    val error: String?
)