package com.example.bazaartrackermobile.data.remote

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val stock: Int,
    val unit: String,
    val active: Boolean
) : Parcelable

data class StockLog(
    val id: String,
    val productId: String,
    val quantity: Int,
    val type: String, // "IN" or "OUT"
    val reason: String,
    val createdAt: String
)
