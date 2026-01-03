package tn.rnu.isetr.miniprojet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tn.rnu.isetr.miniprojet.data.*

class AuthViewModel(private val authRepository: AuthRepository = AuthRepositoryImpl()) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse?.success == true) {
                        _authState.value = AuthState.Success(authResponse.data!!)
                    } else {
                        _authState.value = AuthState.Error(authResponse?.message ?: "Login failed")
                    }
                } else {
                    _authState.value = AuthState.Error("Login failed: ${response.message()}")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    fun register(name: String, email: String, password: String, phone: String? = null, address: String? = null) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.register(
                    RegisterRequest(name, email, password, phone, address)
                )
                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse?.success == true) {
                        _authState.value = AuthState.Success(authResponse.data!!)
                    } else {
                        _authState.value = AuthState.Error(authResponse?.message ?: "Registration failed")
                    }
                } else {
                    _authState.value = AuthState.Error("Registration failed: ${response.message()}")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    fun getProfile() {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getProfile()
                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse?.success == true && authResponse.data != null) {
                        _authState.value = AuthState.Success(authResponse.data)
                    } else {
                        _authState.value = AuthState.Error(authResponse?.message ?: "Failed to get profile")
                    }
                } else {
                    _authState.value = AuthState.Error("Failed to get profile: ${response.message()}")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    fun updateProfile(name: String? = null, phone: String? = null, address: String? = null, latitude: Double? = null, longitude: Double? = null) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val request = UpdateProfileRequest(name, phone, address, latitude, longitude)
                val response = RetrofitClient.apiService.updateProfile(request)
                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse?.success == true) {
                        _authState.value = AuthState.Success(authResponse.data!!)
                    } else {
                        _authState.value = AuthState.Error(authResponse?.message ?: "Failed to update profile")
                    }
                } else {
                    _authState.value = AuthState.Error("Failed to update profile: ${response.message()}")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val request = ChangePasswordRequest(currentPassword, newPassword)
                val response = RetrofitClient.apiService.changePassword(request)
                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse?.success == true) {
                        _authState.value = AuthState.Success(authResponse.data!!)
                    } else {
                        _authState.value = AuthState.Error(authResponse?.message ?: "Failed to change password")
                    }
                } else {
                    _authState.value = AuthState.Error("Failed to change password: ${response.message()}")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val data: AuthData) : AuthState()
    data class Error(val message: String) : AuthState()
}