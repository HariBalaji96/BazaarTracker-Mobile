package com.example.bazaartracker

import retrofit2.http.*

interface ExpenseApiService {
    @GET("api/expenses")
    suspend fun getExpenses(): List<Expense>

    @POST("api/expenses")
    suspend fun createExpense(@Body request: CreateExpenseRequest): ExpenseResponse

    @PUT("api/expenses/{id}")
    suspend fun updateExpense(@Path("id") id: String, @Body request: CreateExpenseRequest): ExpenseResponse

    @DELETE("api/expenses/{id}")
    suspend fun deleteExpense(@Path("id") id: String): ExpenseResponse
}
