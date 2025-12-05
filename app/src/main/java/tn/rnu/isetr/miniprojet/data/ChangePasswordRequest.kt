package tn.rnu.isetr.miniprojet.data

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)