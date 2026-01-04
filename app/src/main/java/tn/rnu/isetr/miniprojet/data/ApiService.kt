package tn.rnu.isetr.miniprojet.data

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
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

    // Pharmacy Auth
    @POST("pharmacies/auth/login")
    suspend fun pharmacyLogin(@Body request: LoginRequest): Response<AuthResponse>

    @POST("pharmacies/auth/register")
    suspend fun pharmacyRegister(@Body request: RegisterRequest): Response<AuthResponse>

    @GET("pharmacies/auth/me")
    suspend fun getPharmacyProfile(): Response<AuthResponse>

    @PUT("pharmacies/auth/update-profile")
    suspend fun updatePharmacyProfile(@Body request: UpdateProfileRequest): Response<AuthResponse>

    @GET("pharmacies")
    suspend fun getPharmacies(
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 1
    ): Response<PharmacyResponse>

    @PUT("pharmacies/{id}/stock")
    suspend fun updatePharmacyStock(
        @Path("id") pharmacyId: String,
        @Body request: UpdateStockRequest
    ): Response<StockResponse>

    @GET("orders/pharmacy/my-orders")
    suspend fun getPharmacyOrders(
        @Query("status") status: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 1
    ): Response<OrderListResponse>

    @PUT("orders/{id}/pharmacy-status")
    suspend fun updateOrderStatus(
        @Path("id") orderId: String,
        @Body request: UpdateOrderStatusRequest
    ): Response<OrderResponse>

    @Multipart
    @POST("orders/upload-prescription")
    suspend fun uploadPrescription(@Part prescription: MultipartBody.Part): Response<UploadResponse>

    @POST("orders")
    suspend fun createOrder(@Body request: OrderRequest): Response<OrderResponse>

    @GET("orders")
    suspend fun getUserOrders(): Response<OrderListResponse>
}