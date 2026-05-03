package com.example.bazaartrackermobile.util

import com.example.bazaartrackermobile.data.local.*
import com.example.bazaartrackermobile.data.remote.*

fun Product.toEntity() = ProductEntity(id, name, price, stock, unit, active)
fun ProductEntity.toDomain() = Product(id, name, price, stock, unit, active)

fun Vendor.toEntity() = VendorEntity(id, name, phone, address, totalCreditGiven, totalPaidAmount, pendingAmount, active)
fun VendorEntity.toDomain() = Vendor(id, name, phone, address, totalCreditGiven, totalPaidAmount, pendingAmount, active)

fun Sale.toEntity() = SaleEntity(id, vendorId, vendorName, saleType, totalAmount, saleDate)
fun SaleEntity.toDomain() = Sale(id, vendorId, vendorName, saleType, totalAmount, saleDate, emptyList()) // Items not cached in main table for simplicity

fun Expense.toEntity() = ExpenseEntity(id, category, amount, description, date)
fun ExpenseEntity.toDomain() = Expense(id, category, amount, description, date)

fun Payment.toEntity() = PaymentEntity(id, vendorId, vendorName, amount, paymentMethod, date, description)
fun PaymentEntity.toDomain() = Payment(id, vendorId, vendorName, amount, paymentMethod, date, description)

fun DashboardResponse.toEntity() = DashboardEntity(
    totalSales = totalSales,
    totalCredit = totalCredit,
    totalPayments = totalPayments,
    totalExpenses = totalExpenses,
    profit = profit
)
fun DashboardEntity.toDomain() = DashboardResponse(
    totalSales = totalSales,
    totalCredit = totalCredit,
    totalPayments = totalPayments,
    totalExpenses = totalExpenses,
    profit = profit
)
