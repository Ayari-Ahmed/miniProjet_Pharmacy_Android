package tn.rnu.isetr.miniprojet.data

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val data: AuthData? = null,
    val errors: List<String>? = null
)

data class AuthData(
    val user: User?,
    val token: String?
)