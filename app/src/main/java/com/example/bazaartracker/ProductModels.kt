package com.example.bazaartracker

import com.google.gson.annotations.SerializedName

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
