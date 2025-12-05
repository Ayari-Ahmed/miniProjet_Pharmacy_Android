package tn.rnu.isetr.miniprojet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tn.rnu.isetr.miniprojet.data.*

class PharmacyViewModel : ViewModel() {

    private val _pharmacyState = MutableStateFlow<PharmacyState>(PharmacyState.Idle)
    val pharmacyState: StateFlow<PharmacyState> = _pharmacyState

    fun getPharmacies() {
        _pharmacyState.value = PharmacyState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getPharmacies()
                if (response.isSuccessful) {
                    val pharmacyResponse = response.body()
                    if (pharmacyResponse?.success == true) {
                        _pharmacyState.value = PharmacyState.Success(pharmacyResponse.data ?: emptyList())
                    } else {
                        _pharmacyState.value = PharmacyState.Error(pharmacyResponse?.message ?: "Failed to load pharmacies")
                    }
                } else {
                    _pharmacyState.value = PharmacyState.Error("Failed to load pharmacies: ${response.message()}")
                }
            } catch (e: Exception) {
                _pharmacyState.value = PharmacyState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    fun resetState() {
        _pharmacyState.value = PharmacyState.Idle
    }
}

sealed class PharmacyState {
    object Idle : PharmacyState()
    object Loading : PharmacyState()
    data class Success(val pharmacies: List<Pharmacy>) : PharmacyState()
    data class Error(val message: String) : PharmacyState()
}