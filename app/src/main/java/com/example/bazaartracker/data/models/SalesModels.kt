package com.example.bazaartracker.data.models

import com.google.gson.annotations.SerializedName

enum class SaleType {
    CASH, CREDIT
}

data class SaleItem(
    @SerializedName("productId")
    val productId: String,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("productName")
    val productName: String? = null,
    @SerializedName("price")
    val price: Double? = null
)

data class Sale(
    @SerializedName("id")
    val id: String,
    @SerializedName("vendorId")
    val vendorId: String?,
    @SerializedName("saleType")
    val saleType: SaleType,
    @SerializedName("items", alternate = ["products", "saleItems"])
    val items: List<SaleItem>? = null,
    @SerializedName("totalAmount")
    val totalAmount: Double,
    @SerializedName("date", alternate = ["createdAt", "timestamp"])
    val date: String?,
    @SerializedName("vendorName")
    val vendorName: String? = null
)

data class CreateSaleRequest(
    val vendorId: String?,
    val saleType: SaleType,
    val items: List<CreateSaleItemRequest>
)

data class CreateSaleItemRequest(
    val productId: String,
    val quantity: Int
)

data class SaleResponse(
    val message: String?,
    val sale: Sale?
)
