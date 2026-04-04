package com.example.bazaartracker

data class DashboardResponse(
    val totalSales: Double,
    val totalCredit: Double,
    val totalPayments: Double,
    val totalExpenses: Double,
    val profit: Double
)
