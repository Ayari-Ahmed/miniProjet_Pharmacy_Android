package tn.rnu.isetr.miniprojet.data

class OrderRepositoryImpl(private val apiService: ApiService = RetrofitClient.apiService) : OrderRepository {

    override suspend fun createOrder(
        pharmacyId: String,
        items: List<OrderItem>,
        deliveryAddress: String,
        deliveryLatitude: Double?,
        deliveryLongitude: Double?,
        specialInstructions: String?
    ): Result<Order> {
        return try {
            val orderRequest = OrderRequest(
                pharmacy = pharmacyId,
                items = items,
                deliveryAddress = deliveryAddress,
                deliveryLatitude = deliveryLatitude,
                deliveryLongitude = deliveryLongitude,
                specialInstructions = specialInstructions
            )
            val response = apiService.createOrder(orderRequest)
            if (response.isSuccessful) {
                val orderResponse = response.body()
                if (orderResponse?.success == true && orderResponse.data != null) {
                    Result.success(orderResponse.data)
                } else {
                    Result.failure(Exception(orderResponse?.message ?: "Failed to create order"))
                }
            } else {
                Result.failure(Exception("Failed to create order: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        }
    }

    override suspend fun getUserOrders(): Result<List<Order>> {
        return try {
            val response = apiService.getUserOrders()
            if (response.isSuccessful) {
                val orderListResponse = response.body()
                if (orderListResponse?.success == true) {
                    Result.success(orderListResponse.data)
                } else {
                    Result.failure(Exception(orderListResponse?.message ?: "Failed to load orders"))
                }
            } else {
                Result.failure(Exception("Failed to load orders: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        }
    }

    override suspend fun getPharmacyOrders(status: String?, limit: Int, page: Int): Result<List<Order>> {
        return try {
            val response = apiService.getPharmacyOrders(status, limit, page)
            if (response.isSuccessful) {
                val orderListResponse = response.body()
                if (orderListResponse?.success == true) {
                    Result.success(orderListResponse.data)
                } else {
                    Result.failure(Exception(orderListResponse?.message ?: "Failed to load orders"))
                }
            } else {
                Result.failure(Exception("Failed to load orders: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        }
    }

    override suspend fun updateOrderStatus(orderId: String, status: String): Result<Order> {
        return try {
            val request = UpdateOrderStatusRequest(status)
            val response = apiService.updateOrderStatus(orderId, request)
            if (response.isSuccessful) {
                val orderResponse = response.body()
                if (orderResponse?.success == true && orderResponse.data != null) {
                    Result.success(orderResponse.data)
                } else {
                    Result.failure(Exception(orderResponse?.message ?: "Failed to update order status"))
                }
            } else {
                Result.failure(Exception("Failed to update order status: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        }
    }
}