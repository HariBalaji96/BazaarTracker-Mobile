package com.example.bazaartrackermobile.data.remote

data class DashboardResponse(
    val totalSales: Double,
    val totalCredit: Double,
    val totalPayments: Double,
    val totalExpenses: Double,
    val profit: Double,
    val salesTrends: List<TrendItem> = emptyList(),
    val expenseTrends: List<TrendItem> = emptyList()
)

data class TrendItem(
    val date: String,
    val amount: Double
)
