package com.example.bazaartrackermobile.data.remote

import android.content.Context
import com.example.bazaartrackermobile.data.local.AuthTokenManager
import com.example.bazaartrackermobile.util.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private var retrofit: Retrofit? = null

    fun getClient(context: Context): Retrofit {
        return retrofit ?: synchronized(this) {
            val currentRetrofit = retrofit
            if (currentRetrofit != null) {
                currentRetrofit
            } else {
                val tokenManager = AuthTokenManager(context)
                
                val loggingInterceptor = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }

                val authInterceptor = AuthInterceptor(context, tokenManager)

                val okHttpClient = OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(authInterceptor)
                    .build()

                val newRetrofit = Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                
                retrofit = newRetrofit
                newRetrofit
            }
        }
    }

    fun resetClient() {
        retrofit = null
    }
}