package com.example.bazaartrackermobile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bazaartrackermobile.data.local.AuthTokenManager
import com.example.bazaartrackermobile.data.remote.ApiService
import com.example.bazaartrackermobile.data.remote.RetrofitClient
import com.example.bazaartrackermobile.data.remote.SignupRequest
import com.example.bazaartrackermobile.databinding.ActivitySignupBinding
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var tokenManager: AuthTokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = AuthTokenManager(this)

        binding.btnSignup.setOnClickListener {
            signup()
        }

        binding.tvLoginLink.setOnClickListener {
            finish() // Go back to login
        }
    }

    private fun signup() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val company = binding.etCompany.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || company.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                showLoading(true)
                val apiService = RetrofitClient.getClient(this@SignupActivity).create(ApiService::class.java)
                val response = apiService.signup(SignupRequest(name, email, company, password))

                if (response.isSuccessful && response.body()?.token != null) {
                    tokenManager.saveToken(response.body()!!.token!!)
                    val intent = Intent(this@SignupActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    val errorMsg = response.body()?.message ?: response.body()?.error ?: "Signup failed"
                    Toast.makeText(this@SignupActivity, errorMsg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SignupActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSignup.isEnabled = !isLoading
    }
}