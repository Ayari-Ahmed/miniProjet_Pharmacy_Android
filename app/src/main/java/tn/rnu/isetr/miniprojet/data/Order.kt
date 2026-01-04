package tn.rnu.isetr.miniprojet.data

import tn.rnu.isetr.miniprojet.data.Pagination

data class OrderRequest(
    val pharmacy: String, // pharmacy ID
    val items: List<OrderItem>,
    val deliveryAddress: String,
    val deliveryLatitude: Double? = null,
    val deliveryLongitude: Double? = null,
    val specialInstructions: String? = null,
    val prescriptionUrl: String? = null
)

data class OrderItem(
    val medicine: String, // medicine ID
    val quantity: Int
)

data class OrderResponse(
    val success: Boolean,
    val message: String,
    val data: Order? = null,
    val errors: List<String>? = null
)

data class Order(
    val _id: String,
    val customer: Any?,  // Changed to Any? to handle string or Customer object
    val pharmacy: Pharmacy,
    val items: List<OrderItemDetail>,
    val totalAmount: Double,
    val status: String,
    val deliveryAddress: String,
    val deliveryLatitude: Double? = null,
    val deliveryLongitude: Double? = null,
    val specialInstructions: String? = null,
    val prescriptionUrl: String? = null,
    val createdAt: String,
    val updatedAt: String
)

data class OrderListResponse(
    val success: Boolean,
    val data: List<Order>,
    val pagination: Pagination? = null,
    val message: String? = null
)

data class UploadResponse(
    val success: Boolean,
    val message: String,
    val data: UploadData
)

data class UploadData(
    val url: String,
    val filename: String
)
