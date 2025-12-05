package tn.rnu.isetr.miniprojet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import tn.rnu.isetr.miniprojet.data.Pharmacy
import tn.rnu.isetr.miniprojet.viewmodel.PharmacyState
import tn.rnu.isetr.miniprojet.viewmodel.PharmacyViewModel

@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    viewModel: PharmacyViewModel = viewModel()
) {
    val pharmacyState by viewModel.pharmacyState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
        viewModel.getPharmacies()
    }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Pharmacy Map",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        when (pharmacyState) {
            is PharmacyState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is PharmacyState.Success -> {
                val pharmacies = (pharmacyState as PharmacyState.Success).pharmacies
                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(15.0)
                            // Set initial position to first pharmacy or default
                            val startPoint = if (pharmacies.isNotEmpty()) {
                                GeoPoint(pharmacies[0].latitude, pharmacies[0].longitude)
                            } else {
                                GeoPoint(36.8065, 10.1815) // Tunis as default
                            }
                            controller.setCenter(startPoint)
                        }
                    },
                    update = { mapView ->
                        mapView.overlays.clear()
                        pharmacies.forEach { pharmacy ->
                            val marker = Marker(mapView).apply {
                                position = GeoPoint(pharmacy.latitude, pharmacy.longitude)
                                title = pharmacy.name
                                snippet = pharmacy.address
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            }
                            mapView.overlays.add(marker)
                        }
                        mapView.invalidate()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            is PharmacyState.Error -> {
                val errorMessage = (pharmacyState as PharmacyState.Error).message
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.getPharmacies() }) {
                            Text("Retry")
                        }
                    }
                }
            }
            PharmacyState.Idle -> {
                // Do nothing
            }
        }
    }
}