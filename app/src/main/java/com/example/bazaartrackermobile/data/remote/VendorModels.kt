package com.example.bazaartrackermobile.data.remote

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Vendor(
    val id: String,
    val name: String,
    val phone: String,
    val address: String,
    val totalCreditGiven: Double,
    val totalPaidAmount: Double,
    val pendingAmount: Double,
    val active: Boolean
) : Parcelable

data class VendorRequest(
    val name: String,
    val phone: String,
    val address: String,
    val active: Boolean = true
)

data class RecentSale(
    val id: String,
    val date: String,
    val totalAmount: Double,
    val status: String
)

data class PaymentRecord(
    val id: String,
    val date: String,
    val amount: Double,
    val method: String
)
