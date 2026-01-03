package tn.rnu.isetr.miniprojet.data

data class UpdateStockRequest(
    val medicine: String, // medicine ID
    val stock: Int,
    val price: Double? = null
)

data class StockResponse(
    val success: Boolean,
    val message: String,
    val data: List<PharmacyStock>? = null,
    val errors: List<String>? = null
)

data class UpdateOrderStatusRequest(
    val status: String,
    val note: String? = null
)