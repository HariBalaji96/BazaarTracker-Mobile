package com.example.bazaartrackermobile.util

import com.example.bazaartrackermobile.data.remote.BaseResponse
import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

object ErrorHandler {

    fun getErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is IOException -> "No internet connection. Please check your network."
            is SocketTimeoutException -> "Connection timed out. Please try again."
            is HttpException -> {
                val errorBody = throwable.response()?.errorBody()?.string()
                parseErrorBody(errorBody) ?: "An unexpected error occurred (HTTP ${throwable.code()})"
            }
            else -> "Something went wrong. Please try again later."
        }
    }

    fun parseError(errorCode: Int, errorBody: String?): String {
        return when (errorCode) {
            400 -> parseErrorBody(errorBody) ?: "Invalid request."
            401 -> "Session expired. Please login again."
            403 -> "You don't have permission to perform this action."
            404 -> "Resource not found."
            in 500..599 -> "Server error. Please try again later."
            else -> parseErrorBody(errorBody) ?: "An unexpected error occurred."
        }
    }

    private fun parseErrorBody(errorBody: String?): String? {
        return try {
            val baseResponse = Gson().fromJson(errorBody, BaseResponse::class.java)
            baseResponse.message ?: baseResponse.errors?.values?.flatten()?.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }
}
