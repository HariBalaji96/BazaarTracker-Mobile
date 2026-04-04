package com.example.bazaartracker

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ProductApiService {
    @GET("api/products")
    suspend fun getProducts(): List<Product>

    @POST("api/products")
    suspend fun createProduct(@Body request: CreateProductRequest): ProductResponse

    @PUT("api/products/{id}")
    suspend fun updateProduct(@Path("id") id: String, @Body request: CreateProductRequest): ProductResponse

    @DELETE("api/products/{id}")
    suspend fun deleteProduct(@Path("id") id: String): ProductResponse
}
