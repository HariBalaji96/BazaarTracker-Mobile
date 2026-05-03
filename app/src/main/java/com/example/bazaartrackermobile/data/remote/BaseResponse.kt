package com.example.bazaartrackermobile.data.remote

data class BaseResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val errors: Map<String, List<String>>? = null
)
