package com.example.bazaartracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bazaartracker.ui.theme.BazaarTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val authViewModel: AuthViewModel = viewModel()
            var currentScreen by remember { mutableStateOf("login") }

            BazaarTrackerTheme {
                when (currentScreen) {
                    "login" -> {
                        LoginScreen(
                            viewModel = authViewModel,
                            onNavigateToSignup = {
                                authViewModel.resetState()
                                currentScreen = "signup"
                            },
                            onLoginSuccess = {
                                currentScreen = "main"
                            }
                        )
                    }
                    "signup" -> {
                        SignupScreen(
                            viewModel = authViewModel,
                            onNavigateToLogin = {
                                authViewModel.resetState()
                                currentScreen = "login"
                            },
                            onSignupSuccess = {
                                currentScreen = "login"
                            }
                        )
                    }
                    "main" -> {
                        MainScreen(
                            onLogout = {
                                authViewModel.resetState()
                                currentScreen = "login"
                            }
                        )
                    }
                }
            }
        }
    }
}
