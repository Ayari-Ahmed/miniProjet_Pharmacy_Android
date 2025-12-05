package tn.rnu.isetr.miniprojet.data

data class User(
    val _id: String,
    val name: String,
    val email: String,
    val phone: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isActive: Boolean = true,
    val lastLogin: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)