package com.example.bazaartracker

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
