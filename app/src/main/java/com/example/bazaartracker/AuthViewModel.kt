package com.example.bazaartracker

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val authService = ApiClient.createService(application, AuthApiService::class.java)

    var authState by mutableStateOf<AuthState>(AuthState.Idle)
        private set

    fun login(request: LoginRequest) {
        viewModelScope.launch {
            authState = AuthState.Loading
            try {
                val response = authService.login(request)
                ApiClient.saveToken(getApplication(), response.token)
                authState = AuthState.Success("Login Successful")
            } catch (e: HttpException) {
                val errorMsg = parseError(e) ?: when (e.code()) {
                    401 -> "Invalid credentials"
                    404 -> "User not found"
                    else -> "Login Failed"
                }
                authState = AuthState.Error(errorMsg)
            } catch (e: Exception) {
                authState = AuthState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    fun signup(request: SignupRequest) {
        viewModelScope.launch {
            authState = AuthState.Loading
            try {
                authService.signup(request)
                authState = AuthState.Success("Signup Successful! Please login.")
            } catch (e: HttpException) {
                val errorMsg = parseError(e) ?: when (e.code()) {
                    400 -> "User already exists or invalid data"
                    else -> "Signup Failed"
                }
                authState = AuthState.Error(errorMsg)
            } catch (e: Exception) {
                authState = AuthState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    private fun parseError(e: HttpException): String? {
        return try {
            val response = e.response()
            val errorBody = response?.errorBody()?.string()
            if (errorBody != null) {
                val jsonObject = JSONObject(errorBody)
                // Try common keys for error messages
                jsonObject.optString("message", null) 
                    ?: jsonObject.optString("error", null)
                    ?: jsonObject.optString("msg", null)
            } else {
                null
            }
        } catch (ex: Exception) {
            null
        }
    }

    fun resetState() {
        authState = AuthState.Idle
    }
}
