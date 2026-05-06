package com.example.bazaartrackermobile.data.remote

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Payment(
    val id: String,
    val vendorId: String,
    val vendorName: String? = null,
    val amount: Double,
    val paymentMethod: String,
    val date: String,
    val description: String? = null
) : Parcelable

data class PaymentRequest(
    val vendorId: String,
    val amount: Double,
    val paymentMethod: String,
    val date: String,
    val description: String? = null,
    val saleId: String? = null
)

data class PaymentResponse(
    val payment: Payment,
    val remainingBalance: Double
)
