package tn.rnu.isetr.miniprojet.utils

import android.content.Context
import android.location.Geocoder
import android.location.LocationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint

// Function to get current location
suspend fun getCurrentLocation(context: Context): GeoPoint? {
    return withContext(Dispatchers.IO) {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val providers = locationManager?.getProviders(true)

            providers?.forEach { provider ->
                try {
                    val location = locationManager.getLastKnownLocation(provider)
                    if (location != null) {
                        return@withContext GeoPoint(location.latitude, location.longitude)
                    }
                } catch (e: SecurityException) {
                    // Permission not granted
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }
}

// Function to reverse geocode coordinates to address
suspend fun reverseGeocode(context: Context, geoPoint: GeoPoint): String {
    return withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context)
            val addresses = geocoder.getFromLocation(geoPoint.latitude, geoPoint.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val addressParts = mutableListOf<String>()
                address.getAddressLine(0)?.let { addressParts.add(it) }
                return@withContext addressParts.joinToString(", ")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        "${geoPoint.latitude}, ${geoPoint.longitude}"
    }
}