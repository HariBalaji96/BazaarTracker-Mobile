package com.example.bazaartrackermobile.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()
}

@Dao
interface VendorDao {
    @Query("SELECT * FROM vendors ORDER BY name ASC")
    fun getAllVendors(): Flow<List<VendorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVendors(vendors: List<VendorEntity>)

    @Query("DELETE FROM vendors")
    suspend fun deleteAllVendors()
}

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY saleDate DESC")
    fun getAllSales(): Flow<List<SaleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSales(sales: List<SaleEntity>)

    @Query("DELETE FROM sales")
    suspend fun deleteAllSales()
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)

    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY date DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<PaymentEntity>)

    @Query("DELETE FROM payments")
    suspend fun deleteAllPayments()
}

@Dao
interface DashboardDao {
    @Query("SELECT * FROM dashboard_metrics WHERE id = 1")
    fun getDashboardMetrics(): Flow<DashboardEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDashboardMetrics(dashboard: DashboardEntity)
}
