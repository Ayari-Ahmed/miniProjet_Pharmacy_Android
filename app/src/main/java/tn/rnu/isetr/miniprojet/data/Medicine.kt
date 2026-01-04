package tn.rnu.isetr.miniprojet.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Medicine (
    val _id: String,
    val name: String,
    val genericName: String?,
    val description: String?,
    val category: String?,
    val requiresPrescription: Boolean,
    val price: Double,
    val imageUrl: String?
) : Parcelable
