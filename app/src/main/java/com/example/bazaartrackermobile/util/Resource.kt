package com.example.bazaartrackermobile.util

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null,
    val errorCode: Int? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null, errorCode: Int? = null) : Resource<T>(data, message, errorCode)
    class Loading<T> : Resource<T>()
}
