package com.example.bazaartracker

import retrofit2.http.*

interface SalesApiService {
    @GET("api/sales")
    suspend fun getSales(): List<Sale>

    @POST("api/sales")
    suspend fun createSale(@Body request: CreateSaleRequest): SaleResponse

    @PUT("api/sales/{id}")
    suspend fun updateSale(@Path("id") id: String, @Body request: CreateSaleRequest): SaleResponse

    @DELETE("api/sales/{id}")
    suspend fun deleteSale(@Path("id") id: String): SaleResponse
}
