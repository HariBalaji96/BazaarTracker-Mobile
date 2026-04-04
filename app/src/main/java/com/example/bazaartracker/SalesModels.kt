package com.example.bazaartracker

enum class SaleType {
    CASH, CREDIT
}

data class SaleItem(
    val productId: String,
    val quantity: Int,
    val productName: String? = null,
    val price: Double? = null
)

data class Sale(
    val id: String,
    val vendorId: String?,
    val saleType: SaleType,
    val items: List<SaleItem>,
    val totalAmount: Double,
    val date: String?,
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
