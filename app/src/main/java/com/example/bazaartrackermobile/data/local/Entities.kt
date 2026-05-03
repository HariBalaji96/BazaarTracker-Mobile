package com.example.bazaartrackermobile.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val price: Double,
    val stock: Int,
    val unit: String,
    val active: Boolean,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "vendors")
data class VendorEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val address: String,
    val totalCreditGiven: Double,
    val totalPaidAmount: Double,
    val pendingAmount: Double,
    val active: Boolean,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey val id: String,
    val vendorId: String,
    val vendorName: String? = null,
    val saleType: String,
    val totalAmount: Double,
    val saleDate: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val category: String,
    val amount: Double,
    val description: String,
    val date: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey val id: String,
    val vendorId: String,
    val vendorName: String? = null,
    val amount: Double,
    val paymentMethod: String,
    val date: String,
    val description: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "dashboard_metrics")
data class DashboardEntity(
    @PrimaryKey val id: Int = 1,
    val totalSales: Double,
    val totalCredit: Double,
    val totalPayments: Double,
    val totalExpenses: Double,
    val profit: Double,
    val lastUpdated: Long = System.currentTimeMillis()
)
