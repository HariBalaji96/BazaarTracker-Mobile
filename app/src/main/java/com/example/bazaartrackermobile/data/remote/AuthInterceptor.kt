package com.example.bazaartrackermobile.data.remote

import android.content.Context
import android.content.Intent
import com.example.bazaartrackermobile.data.local.AuthTokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val context: Context,
    private val tokenManager: AuthTokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
        
        tokenManager.getToken()?.let { token ->
            request.addHeader("Authorization", "Bearer $token")
        }
        
        val response = chain.proceed(request.build())
        
        if (response.code == 401) {
            tokenManager.clearToken()
            
            // Redirect to Login (Assuming a LoginActivity exists or will be created)
            // Using NEW_TASK because this is called from the networking thread
            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            intent?.let { context.startActivity(it) }
        }
        
        return response
    }
}