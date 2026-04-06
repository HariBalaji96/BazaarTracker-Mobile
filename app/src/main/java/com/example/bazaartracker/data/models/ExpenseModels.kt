package com.example.bazaartracker.data.models

import com.google.gson.annotations.SerializedName

data class Expense(
    @SerializedName("id")
    val id: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("date")
    val date: String? = null
)

data class CreateExpenseRequest(
    val category: String,
    val amount: Double
)

data class ExpenseResponse(
    val message: String?,
    val expense: Expense?
)
