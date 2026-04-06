package com.example.bazaartracker.data.api

import com.example.bazaartracker.data.models.ChangePasswordRequest
import com.example.bazaartracker.data.models.GenericResponse
import com.example.bazaartracker.data.models.UpdateProfileRequest
import com.example.bazaartracker.data.models.UserProfileResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface UserApiService {
    @GET("api/users/me")
    suspend fun getUserProfile(): UserProfileResponse

    @PUT("api/users/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): UserProfileResponse

    @PUT("api/users/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): GenericResponse
}
