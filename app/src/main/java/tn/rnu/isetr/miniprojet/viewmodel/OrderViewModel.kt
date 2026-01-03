package tn.rnu.isetr.miniprojet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tn.rnu.isetr.miniprojet.data.*

class OrderViewModel : ViewModel() {

    private val _orderState = MutableStateFlow<OrderState>(OrderState.Idle)
    val orderState: StateFlow<OrderState> = _orderState

    fun createOrder(
        pharmacyId: String,
        items: List<OrderItem>,
        deliveryAddress: String,
        deliveryLatitude: Double? = null,
        deliveryLongitude: Double? = null,
        specialInstructions: String? = null,
        prescriptionUrl: String? = null
    ) {
        _orderState.value = OrderState.Loading
        viewModelScope.launch {
            try {
                val orderRequest = OrderRequest(
                    pharmacy = pharmacyId,
                    items = items,
                    deliveryAddress = deliveryAddress,
                    deliveryLatitude = deliveryLatitude,
                    deliveryLongitude = deliveryLongitude,
                    specialInstructions = specialInstructions,
                    prescriptionUrl = prescriptionUrl
                )

                val response = RetrofitClient.apiService.createOrder(orderRequest)
                if (response.isSuccessful) {
                    val orderResponse = response.body()
                    if (orderResponse?.success == true) {
                        _orderState.value = OrderState.Success(orderResponse.data!!)
                    } else {
                        _orderState.value = OrderState.Error(orderResponse?.message ?: "Failed to create order")
                    }
                } else {
                    _orderState.value = OrderState.Error("Failed to create order: ${response.message()}")
                }
            } catch (e: Exception) {
                _orderState.value = OrderState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    fun getUserOrders() {
        _orderState.value = OrderState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getUserOrders()
                if (response.isSuccessful) {
                    val orderListResponse = response.body()
                    if (orderListResponse?.success == true) {
                        _orderState.value = OrderState.OrdersLoaded(orderListResponse.data)
                    } else {
                        _orderState.value = OrderState.Error(orderListResponse?.message ?: "Failed to load orders")
                    }
                } else {
                    _orderState.value = OrderState.Error("Failed to load orders: ${response.message()}")
                }
            } catch (e: Exception) {
                _orderState.value = OrderState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    fun getPharmacyOrders(status: String? = null) {
        _orderState.value = OrderState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getPharmacyOrders(status)
                if (response.isSuccessful) {
                    val orderListResponse = response.body()
                    if (orderListResponse?.success == true) {
                        _orderState.value = OrderState.OrdersLoaded(orderListResponse.data)
                    } else {
                        _orderState.value = OrderState.Error(orderListResponse?.message ?: "Failed to load orders")
                    }
                } else {
                    _orderState.value = OrderState.Error("Failed to load orders: ${response.message()}")
                }
            } catch (e: Exception) {
                _orderState.value = OrderState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    fun updateOrderStatus(orderId: String, status: String) {
        _orderState.value = OrderState.Loading
        viewModelScope.launch {
            try {
                val request = UpdateOrderStatusRequest(status)
                val response = RetrofitClient.apiService.updateOrderStatus(orderId, request)
                if (response.isSuccessful) {
                    val orderResponse = response.body()
                    if (orderResponse?.success == true) {
                        // After successful update, refresh the pharmacy orders list
                        getPharmacyOrders()
                    } else {
                        _orderState.value = OrderState.Error(orderResponse?.message ?: "Failed to update order status")
                    }
                } else {
                    _orderState.value = OrderState.Error("Failed to update order status: ${response.message()}")
                }
            } catch (e: Exception) {
                _orderState.value = OrderState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    fun resetState() {
        _orderState.value = OrderState.Idle
    }
}

sealed class OrderState {
    object Idle : OrderState()
    object Loading : OrderState()
    data class Success(val order: Order) : OrderState()
    data class OrdersLoaded(val orders: List<Order>) : OrderState()
    data class Error(val message: String) : OrderState()
}