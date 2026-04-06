package com.example.bazaartracker.data.models

data class UserProfileResponse(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val isActive: Boolean
)

data class UpdateProfileRequest(
    val name: String
)

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)

data class GenericResponse(
    val message: String
)
