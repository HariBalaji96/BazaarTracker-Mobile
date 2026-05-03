package com.example.bazaartrackermobile.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<AuthResponse>

    @GET("api/users/me")
    suspend fun getProfile(): Response<UserProfile>

    @PUT("api/users/me")
    suspend fun updateProfile(@Body request: UpdateUserRequest): Response<UserProfile>

    @PUT("api/users/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<MessageResponse>

    @GET("api/dashboard")
    suspend fun getDashboardData(): Response<DashboardResponse>

    // Products
    @GET("api/products")
    suspend fun getProducts(): Response<List<Product>>

    @POST("api/products")
    suspend fun createProduct(@Body product: ProductRequest): Response<Product>

    @GET("api/products/{id}")
    suspend fun getProduct(@Path("id") id: String): Response<Product>

    @PUT("api/products/{id}")
    suspend fun updateProduct(@Path("id") id: String, @Body product: ProductRequest): Response<Product>

    @DELETE("api/products/{id}")
    suspend fun deleteProduct(@Path("id") id: String): Response<Unit>

    @GET("api/products/{id}/stock-logs")
    suspend fun getProductStockLogs(@Path("id") id: String): Response<List<StockLog>>

    // Vendors
    @GET("api/vendors")
    suspend fun getVendors(): Response<List<Vendor>>

    @POST("api/vendors")
    suspend fun createVendor(@Body vendor: VendorRequest): Response<Vendor>

    @GET("api/vendors/{id}")
    suspend fun getVendor(@Path("id") id: String): Response<Vendor>

    @PUT("api/vendors/{id}")
    suspend fun updateVendor(@Path("id") id: String, @Body vendor: VendorRequest): Response<Vendor>

    @DELETE("api/vendors/{id}")
    suspend fun deleteVendor(@Path("id") id: String): Response<Unit>

    @GET("api/vendors/{id}/recent-sales")
    suspend fun getVendorRecentSales(@Path("id") id: String): Response<List<RecentSale>>

    @GET("api/vendors/{id}/payments")
    suspend fun getVendorPayments(@Path("id") id: String): Response<List<PaymentRecord>>

    // Sales
    @GET("api/sales")
    suspend fun getSales(
        @Query("type") type: String? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): Response<List<Sale>>

    @POST("api/sales")
    suspend fun createSale(@Body request: CreateSaleRequest): Response<Sale>

    @GET("api/sales/{id}")
    suspend fun getSale(@Path("id") id: String): Response<Sale>

    @DELETE("api/sales/{id}")
    suspend fun deleteSale(@Path("id") id: String): Response<Unit>

    // Expenses
    @GET("api/expenses")
    suspend fun getExpenses(
        @Query("category") category: String? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): Response<List<Expense>>

    @POST("api/expenses")
    suspend fun createExpense(@Body request: ExpenseRequest): Response<Expense>

    @PUT("api/expenses/{id}")
    suspend fun updateExpense(@Path("id") id: String, @Body request: ExpenseRequest): Response<Expense>

    @DELETE("api/expenses/{id}")
    suspend fun deleteExpense(@Path("id") id: String): Response<Unit>

    // Payments
    @GET("api/payments")
    suspend fun getPayments(): Response<List<Payment>>

    @POST("api/payments")
    suspend fun recordPayment(@Body request: PaymentRequest): Response<PaymentResponse>

    @GET("api/payments/{id}")
    suspend fun getPayment(@Path("id") id: String): Response<Payment>

    @DELETE("api/payments/{id}")
    suspend fun deletePayment(@Path("id") id: String): Response<Unit>
}

data class ProductRequest(
    val name: String,
    val price: Double,
    val stock: Int,
    val unit: String,
    val active: Boolean = true
)
