package tn.rnu.isetr.miniprojet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tn.rnu.isetr.miniprojet.data.*

class PharmacyAuthViewModel : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun pharmacyLogin(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.pharmacyLogin(LoginRequest(email, password))
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

    fun pharmacyRegister(name: String, email: String, password: String, phone: String, address: String, latitude: Double, longitude: Double) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.pharmacyRegister(
                    RegisterRequest(name, email, password, phone, address, latitude, longitude)
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

    fun getPharmacyProfile() {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getPharmacyProfile()
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

    fun updatePharmacyProfile(name: String? = null, phone: String? = null, address: String? = null, latitude: Double? = null, longitude: Double? = null) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val request = UpdateProfileRequest(name, phone, address, latitude, longitude)
                val response = RetrofitClient.apiService.updatePharmacyProfile(request)
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

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}