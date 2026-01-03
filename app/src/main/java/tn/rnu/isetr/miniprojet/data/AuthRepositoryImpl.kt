package tn.rnu.isetr.miniprojet.data

class AuthRepositoryImpl(private val apiService: ApiService = RetrofitClient.apiService) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthData> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse?.success == true && authResponse.data != null) {
                    Result.success(authResponse.data)
                } else {
                    Result.failure(Exception(authResponse?.message ?: "Login failed"))
                }
            } else {
                Result.failure(Exception("Login failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        }
    }

    override suspend fun register(name: String, email: String, password: String, phone: String?, address: String?): Result<AuthData> {
        return try {
            val response = apiService.register(RegisterRequest(name, email, password, phone, address))
            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse?.success == true && authResponse.data != null) {
                    Result.success(authResponse.data)
                } else {
                    Result.failure(Exception(authResponse?.message ?: "Registration failed"))
                }
            } else {
                Result.failure(Exception("Registration failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        }
    }

    override suspend fun getProfile(): Result<AuthData> {
        return try {
            val response = apiService.getProfile()
            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse?.success == true && authResponse.data != null) {
                    Result.success(authResponse.data)
                } else {
                    Result.failure(Exception(authResponse?.message ?: "Failed to get profile"))
                }
            } else {
                Result.failure(Exception("Failed to get profile: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        }
    }

    override suspend fun updateProfile(name: String?, phone: String?, address: String?, latitude: Double?, longitude: Double?): Result<AuthData> {
        return try {
            val request = UpdateProfileRequest(name, phone, address, latitude, longitude)
            val response = apiService.updateProfile(request)
            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse?.success == true && authResponse.data != null) {
                    Result.success(authResponse.data)
                } else {
                    Result.failure(Exception(authResponse?.message ?: "Failed to update profile"))
                }
            } else {
                Result.failure(Exception("Failed to update profile: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        }
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<AuthData> {
        return try {
            val request = ChangePasswordRequest(currentPassword, newPassword)
            val response = apiService.changePassword(request)
            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse?.success == true && authResponse.data != null) {
                    Result.success(authResponse.data)
                } else {
                    Result.failure(Exception(authResponse?.message ?: "Failed to change password"))
                }
            } else {
                Result.failure(Exception("Failed to change password: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        }
    }
}