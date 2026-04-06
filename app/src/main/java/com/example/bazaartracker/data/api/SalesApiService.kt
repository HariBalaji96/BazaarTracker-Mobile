package com.example.bazaartracker.data.api

import com.example.bazaartracker.data.models.CreateSaleRequest
import com.example.bazaartracker.data.models.Sale
import com.example.bazaartracker.data.models.SaleResponse
import retrofit2.http.*

interface SalesApiService {
    @GET("api/sales")
    suspend fun getSales(): List<Sale>

    @GET("api/sales/{id}")
    suspend fun getSaleDetails(@Path("id") id: String): Sale

    @POST("api/sales")
    suspend fun createSale(@Body request: CreateSaleRequest): SaleResponse

    @PUT("api/sales/{id}")
    suspend fun updateSale(@Path("id") id: String, @Body request: CreateSaleRequest): SaleResponse

    @DELETE("api/sales/{id}")
    suspend fun deleteSale(@Path("id") id: String): SaleResponse
}
