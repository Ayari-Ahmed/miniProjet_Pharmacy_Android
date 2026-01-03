package tn.rnu.isetr.miniprojet.data

interface PharmacyRepository {
    suspend fun getPharmacies(limit: Int = 20, page: Int = 1): Result<List<Pharmacy>>
    suspend fun updateStock(pharmacyId: String, medicineId: String, stock: Int, price: Double? = null): Result<List<PharmacyStock>>
}