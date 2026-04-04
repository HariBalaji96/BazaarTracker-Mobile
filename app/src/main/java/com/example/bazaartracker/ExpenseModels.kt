package com.example.bazaartracker

data class Expense(
    val id: String,
    val category: String,
    val amount: Double,
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
