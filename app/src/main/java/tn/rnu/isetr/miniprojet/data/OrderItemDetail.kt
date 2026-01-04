package tn.rnu.isetr.miniprojet.data

data class OrderItemDetail(
    val medicine: Medicine,
    val quantity: Int,
    val price: Double,
    val pharmacy: String
)