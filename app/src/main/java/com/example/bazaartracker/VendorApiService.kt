package com.example.bazaartracker

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface VendorApiService {
    @GET("api/vendors")
    suspend fun getVendors(): List<Vendor>

    @GET("api/vendors/{id}")
    suspend fun getVendorById(@Path("id") id: String): Vendor

    @POST("api/vendors")
    suspend fun createVendor(@Body request: CreateVendorRequest): VendorResponse

    @PUT("api/vendors/{id}")
    suspend fun updateVendor(@Path("id") id: String, @Body request: PartialVendorRequest): VendorResponse

    @DELETE("api/vendors/{id}")
    suspend fun deleteVendor(@Path("id") id: String): VendorResponse
}

data class PartialVendorRequest(
    val name: String? = null,
    val phone: String? = null,
    val address: String? = null
)
