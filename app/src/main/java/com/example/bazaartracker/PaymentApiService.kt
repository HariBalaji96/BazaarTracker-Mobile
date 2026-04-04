package com.example.bazaartracker

import retrofit2.http.*

interface PaymentApiService {
    @GET("api/payments")
    suspend fun getPayments(): List<Payment>

    @POST("api/payments")
    suspend fun createPayment(@Body request: CreatePaymentRequest): PaymentResponse
}
