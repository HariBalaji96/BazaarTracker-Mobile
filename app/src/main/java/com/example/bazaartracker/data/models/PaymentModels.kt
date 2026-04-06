package com.example.bazaartracker.data.models

data class Payment(
    val id: String,
    val vendorId: String,
    val vendorName: String? = null,
    val amount: Double,
    val date: String? = null
)

data class CreatePaymentRequest(
    val vendorId: String,
    val amount: Double
)

data class PaymentResponse(
    val message: String?,
    val payment: Payment?
)
