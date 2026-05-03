package com.example.bazaartrackermobile.data.remote

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Expense(
    val id: String,
    val category: String,
    val amount: Double,
    val description: String,
    val date: String
) : Parcelable

data class ExpenseRequest(
    val category: String,
    val amount: Double,
    val description: String,
    val date: String
)
