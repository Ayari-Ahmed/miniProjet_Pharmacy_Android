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

    private val _stockState = MutableStateFlow<StockState>(StockState.Idle)
    val stockState: StateFlow<StockState> = _stockState

    private val _medicinesState = MutableStateFlow<MedicinesState>(MedicinesState.Idle)
    val medicines: StateFlow<MedicinesState> = _medicinesState

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

    fun updateStock(pharmacyId: String, medicineId: String, stock: Int, price: Double? = null) {
        _stockState.value = StockState.Loading
        viewModelScope.launch {
            try {
                val request = UpdateStockRequest(medicineId, stock, price)
                val response = RetrofitClient.apiService.updatePharmacyStock(pharmacyId, request)
                if (response.isSuccessful) {
                    val stockResponse = response.body()
                    if (stockResponse?.success == true) {
                        _stockState.value = StockState.Success(stockResponse.data ?: emptyList())
                    } else {
                        _stockState.value = StockState.Error(stockResponse?.message ?: "Failed to update stock")
                    }
                } else {
                    _stockState.value = StockState.Error("Failed to update stock: ${response.message()}")
                }
            } catch (e: Exception) {
                _stockState.value = StockState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    fun resetState() {
        _pharmacyState.value = PharmacyState.Idle
    }

    fun getMedicines() {
        _medicinesState.value = MedicinesState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getMedicines()
                if (response.isSuccessful) {
                    val medicineResponse = response.body()
                    if (medicineResponse?.success == true) {
                        _medicinesState.value = MedicinesState.Success(medicineResponse.data ?: emptyList())
                    } else {
                        _medicinesState.value = MedicinesState.Error(medicineResponse?.message ?: "Failed to load medicines")
                    }
                } else {
                    _medicinesState.value = MedicinesState.Error("Failed to load medicines: ${response.message()}")
                }
            } catch (e: Exception) {
                _medicinesState.value = MedicinesState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    fun getPharmacy(id: String, onSuccess: (Pharmacy?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getPharmacy(id)
                if (response.isSuccessful) {
                    val pharmacyResponse = response.body()
                    if (pharmacyResponse?.success == true) {
                        onSuccess(pharmacyResponse.data?.firstOrNull())
                    }
                }
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }
}

sealed class PharmacyState {
    object Idle : PharmacyState()
    object Loading : PharmacyState()
    data class Success(val pharmacies: List<Pharmacy>) : PharmacyState()
    data class Error(val message: String) : PharmacyState()
}

sealed class StockState {
    object Idle : StockState()
    object Loading : StockState()
    data class Success(val stock: List<PharmacyStock>) : StockState()
    data class Error(val message: String) : StockState()
}

sealed class MedicinesState {
    object Idle : MedicinesState()
    object Loading : MedicinesState()
    data class Success(val medicines: List<Medicine>) : MedicinesState()
    data class Error(val message: String) : MedicinesState()
}