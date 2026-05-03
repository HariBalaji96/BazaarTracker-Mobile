package com.example.bazaartrackermobile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bazaartrackermobile.data.local.AuthTokenManager
import com.example.bazaartrackermobile.data.remote.ApiService
import com.example.bazaartrackermobile.data.remote.ChangePasswordRequest
import com.example.bazaartrackermobile.data.remote.RetrofitClient
import com.example.bazaartrackermobile.data.remote.UpdateUserRequest
import com.example.bazaartrackermobile.databinding.ActivityProfileBinding
import com.example.bazaartrackermobile.databinding.DialogChangePasswordBinding
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var tokenManager: AuthTokenManager
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = AuthTokenManager(this)
        apiService = RetrofitClient.getClient(this).create(ApiService::class.java)

        fetchProfile()

        binding.btnEdit.setOnClickListener {
            toggleEditMode(true)
        }

        binding.btnSave.setOnClickListener {
            updateProfile()
        }

        binding.btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun fetchProfile() {
        lifecycleScope.launch {
            try {
                showLoading(true)
                val response = apiService.getProfile()
                if (response.isSuccessful) {
                    val profile = response.body()
                    profile?.let {
                        binding.etName.setText(it.name)
                        binding.etEmail.setText(it.email)
                        binding.etCompany.setText(it.companyName)
                        binding.etRole.setText(it.role)
                        binding.tvStatus.text = "Status: ${if (it.isActive) "Active" else "Inactive"}"
                    }
                } else {
                    Toast.makeText(this@ProfileActivity, "Failed to fetch profile", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun updateProfile() {
        val name = binding.etName.text.toString().trim()
        val company = binding.etCompany.text.toString().trim()

        if (name.isEmpty() || company.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                showLoading(true)
                val response = apiService.updateProfile(UpdateUserRequest(name, company))
                if (response.isSuccessful) {
                    Toast.makeText(this@ProfileActivity, "Profile updated", Toast.LENGTH_SHORT).show()
                    toggleEditMode(false)
                } else {
                    Toast.makeText(this@ProfileActivity, "Update failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showChangePasswordDialog() {
        val dialogBinding = DialogChangePasswordBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setPositiveButton("Change", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val oldPassword = dialogBinding.etOldPassword.text.toString()
            val newPassword = dialogBinding.etNewPassword.text.toString()
            val confirmPassword = dialogBinding.etConfirmPassword.text.toString()

            if (oldPassword.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val response = apiService.changePassword(ChangePasswordRequest(oldPassword, newPassword))
                    if (response.isSuccessful) {
                        Toast.makeText(this@ProfileActivity, "Password changed successfully", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(this@ProfileActivity, "Failed to change password", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@ProfileActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun toggleEditMode(isEditing: Boolean) {
        binding.etName.isEnabled = isEditing
        binding.etCompany.isEnabled = isEditing
        binding.btnEdit.visibility = if (isEditing) View.GONE else View.VISIBLE
        binding.btnSave.visibility = if (isEditing) View.VISIBLE else View.GONE
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun logout() {
        tokenManager.clearToken()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}