package com.example.bazaartracker.data.models

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val stock: Int
)

data class CreateProductRequest(
    val name: String,
    val price: Double,
    val stock: Int
)

data class ProductResponse(
    val message: String?,
    val product: Product?
)
