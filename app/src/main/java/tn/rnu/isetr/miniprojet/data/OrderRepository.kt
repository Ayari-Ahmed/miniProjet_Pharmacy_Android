package tn.rnu.isetr.miniprojet.data

interface OrderRepository {
    suspend fun createOrder(
        pharmacyId: String,
        items: List<OrderItem>,
        deliveryAddress: String,
        deliveryLatitude: Double? = null,
        deliveryLongitude: Double? = null,
        specialInstructions: String? = null
    ): Result<Order>

    suspend fun getUserOrders(): Result<List<Order>>
    suspend fun getPharmacyOrders(status: String? = null, limit: Int = 20, page: Int = 1): Result<List<Order>>
    suspend fun updateOrderStatus(orderId: String, status: String): Result<Order>
}