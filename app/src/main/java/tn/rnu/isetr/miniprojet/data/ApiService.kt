package tn.rnu.isetr.miniprojet.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @GET("auth/me")
    suspend fun getProfile(): Response<AuthResponse>

    @PUT("auth/update-profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<AuthResponse>

    @POST("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<AuthResponse>

    @GET("pharmacies")
    suspend fun getPharmacies(
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 1
    ): Response<PharmacyResponse>

    @POST("orders")
    suspend fun createOrder(@Body request: OrderRequest): Response<OrderResponse>

    @GET("orders")
    suspend fun getUserOrders(): Response<OrderListResponse>
}