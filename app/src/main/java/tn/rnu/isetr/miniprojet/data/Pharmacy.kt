package tn.rnu.isetr.miniprojet.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Pharmacy(
    val _id: String,
    val name: String,
    val address: String,
    val phone: String,
    val email: String? = null,
    val latitude: Double,
    val longitude: Double,
    val rating: Double = 0.0,
    val totalReviews: Int = 0,
    val stock: List<PharmacyStock> = emptyList(),
    val isActive: Boolean = true,
    val services: List<String> = emptyList()
) : Parcelable

@Parcelize
data class PharmacyStock(
    val medicine: Medicine,
    val stock: Int,
    val price: Double,
    val lastUpdated: String? = null
) : Parcelable

@Parcelize
data class Medicine(
    val _id: String,
    val name: String,
    val genericName: String? = null,
    val description: String? = null,
    val category: String? = null,
    val requiresPrescription: Boolean = false,
    val price: Double = 0.0,
    val imageUrl: String? = null
) : Parcelable

data class PharmacyResponse(
    val success: Boolean,
    val data: List<Pharmacy>,
    val pagination: Pagination? = null,
    val message: String? = null
)

data class Pagination(
    val page: Int,
    val limit: Int,
    val total: Int,
    val pages: Int
)

data class MedicineListResponse(
    val success: Boolean,
    val message: String,
    val data: List<Medicine>? = null,
    val errors: List<String>? = null
)

data class PharmacyStockResponse(
    val success: Boolean,
    val message: String,
    val data: List<PharmacyStock>? = null,
    val errors: List<String>? = null
)