package com.example.bazaartracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val userApiService = ApiClient.createService(application, UserApiService::class.java)

    private val _userProfile = MutableStateFlow<UserProfileResponse?>(null)
    val userProfile: StateFlow<UserProfileResponse?> = _userProfile

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage

    init {
        fetchUserProfile()
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _userProfile.value = userApiService.getUserProfile()
            } catch (e: Exception) {
                _snackbarMessage.emit("Failed to load profile: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateName(newName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updatedProfile = userApiService.updateProfile(UpdateProfileRequest(newName))
                _userProfile.value = updatedProfile
                _snackbarMessage.emit("Profile updated successfully")
            } catch (e: Exception) {
                _snackbarMessage.emit("Update failed: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun changePassword(oldPass: String, newPass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                userApiService.changePassword(ChangePasswordRequest(oldPass, newPass))
                _snackbarMessage.emit("Password changed successfully")
            } catch (e: Exception) {
                _snackbarMessage.emit("Failed to change password: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetState() {
        _userProfile.value = null
        _isLoading.value = false
    }

    fun logout(onLogout: () -> Unit) {
        ApiClient.clearToken(getApplication())
        resetState()
        onLogout()
    }
}
