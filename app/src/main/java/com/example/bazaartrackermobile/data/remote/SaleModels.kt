package com.example.bazaartrackermobile.data.remote

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Sale(
    val id: String,
    val vendorId: String,
    val vendorName: String? = null,
    val saleType: String, // CASH or CREDIT
    val totalAmount: Double,
    val saleDate: String,
    val items: List<SaleItem>
) : Parcelable

@Parcelize
data class SaleItem(
    val productId: String,
    val productName: String?, // Added for convenience in UI
    val quantity: Int,
    val price: Double
) : Parcelable

data class CreateSaleRequest(
    val vendorId: String,
    val saleType: String,
    val items: List<CreateSaleItemRequest>
)

data class CreateSaleItemRequest(
    val productId: String,
    val quantity: Int,
    val price: Double
)
