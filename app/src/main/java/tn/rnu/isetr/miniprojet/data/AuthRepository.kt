package tn.rnu.isetr.miniprojet.data

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthData>
    suspend fun register(name: String, email: String, password: String, phone: String? = null, address: String? = null): Result<AuthData>
    suspend fun getProfile(): Result<AuthData>
    suspend fun updateProfile(name: String? = null, phone: String? = null, address: String? = null, latitude: Double? = null, longitude: Double? = null): Result<AuthData>
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<AuthData>
}