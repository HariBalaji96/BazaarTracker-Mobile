package com.example.bazaartracker.data.api

import com.example.bazaartracker.data.models.CreateExpenseRequest
import com.example.bazaartracker.data.models.Expense
import com.example.bazaartracker.data.models.ExpenseResponse
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
