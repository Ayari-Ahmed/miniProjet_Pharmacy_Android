package tn.rnu.isetr.miniprojet.data

class PharmacyRepositoryImpl(private val apiService: ApiService = RetrofitClient.apiService) : PharmacyRepository {

    override suspend fun getPharmacies(limit: Int, page: Int): Result<List<Pharmacy>> {
        return try {
            val response = apiService.getPharmacies(limit, page)
            if (response.isSuccessful) {
                val pharmacyResponse = response.body()
                if (pharmacyResponse?.success == true) {
                    Result.success(pharmacyResponse.data ?: emptyList())
                } else {
                    Result.failure(Exception(pharmacyResponse?.message ?: "Failed to load pharmacies"))
                }
            } else {
                Result.failure(Exception("Failed to load pharmacies: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        }
    }

    override suspend fun updateStock(pharmacyId: String, medicineId: String, stock: Int, price: Double?): Result<List<PharmacyStock>> {
        return try {
            val request = UpdateStockRequest(medicineId, stock, price)
            val response = apiService.updatePharmacyStock(pharmacyId, request)
            if (response.isSuccessful) {
                val stockResponse = response.body()
                if (stockResponse?.success == true) {
                    Result.success(stockResponse.data ?: emptyList())
                } else {
                    Result.failure(Exception(stockResponse?.message ?: "Failed to update stock"))
                }
            } else {
                Result.failure(Exception("Failed to update stock: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        }
    }
}