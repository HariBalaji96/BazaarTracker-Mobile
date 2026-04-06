package com.example.bazaartracker.data.api

import com.example.bazaartracker.data.models.CreatePaymentRequest
import com.example.bazaartracker.data.models.Payment
import com.example.bazaartracker.data.models.PaymentResponse
import retrofit2.http.*

interface PaymentApiService {
    @GET("api/payments")
    suspend fun getPayments(): List<Payment>

    @POST("api/payments")
    suspend fun createPayment(@Body request: CreatePaymentRequest): PaymentResponse
}
