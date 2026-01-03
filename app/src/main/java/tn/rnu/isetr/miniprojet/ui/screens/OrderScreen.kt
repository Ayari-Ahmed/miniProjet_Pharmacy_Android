package tn.rnu.isetr.miniprojet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import tn.rnu.isetr.miniprojet.data.OrderItem
import tn.rnu.isetr.miniprojet.data.Pharmacy
import tn.rnu.isetr.miniprojet.data.PharmacyStock
import tn.rnu.isetr.miniprojet.viewmodel.OrderState
import tn.rnu.isetr.miniprojet.viewmodel.OrderViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.core.content.FileProvider
import java.io.File
import android.net.Uri
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.Manifest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun OrderScreen(
    pharmacy: Pharmacy,
    onOrderSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: OrderViewModel = viewModel()
) {
    var deliveryAddress by remember { mutableStateOf("") }
    var specialInstructions by remember { mutableStateOf("") }
    var selectedItems by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showMapPicker by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var prescriptionUri by remember { mutableStateOf<Uri?>(null) }
    var currentPhotoFile by remember { mutableStateOf<File?>(null) }
    var showPrescriptionDialog by remember { mutableStateOf(false) }

    val orderState by viewModel.orderState.collectAsState()
    val context = LocalContext.current

    // Camera permission
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                // Check if the file was actually created and has content
                currentPhotoFile?.let { file ->
                    if (file.exists() && file.length() > 0) {
                        // prescriptionUri is already set
                    } else {
                        prescriptionUri = null
                        currentPhotoFile = null
                    }
                } ?: run {
                    prescriptionUri = null
                }
            } else {
                prescriptionUri = null
                currentPhotoFile = null
            }
        }
    )

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            prescriptionUri = uri
        }
    )

    // Handle order success
    LaunchedEffect(orderState) {
        when (orderState) {
            is OrderState.Success -> {
                showSuccessDialog = true
            }
            else -> {}
        }
    }

    val totalItems = selectedItems.values.sum()
    val totalPrice = selectedItems.entries.sumOf { (medicineId, quantity) ->
        val stockItem = pharmacy.stock.find { it.medicine._id == medicineId }
        (stockItem?.price ?: 0.0) * quantity
    }

    // Check if any selected medicine requires prescription
    val requiresPrescription = selectedItems.keys.any { medicineId ->
        val stockItem = pharmacy.stock.find { it.medicine._id == medicineId }
        stockItem?.medicine?.requiresPrescription == true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Modern Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Place Order",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = pharmacy.name,
                    fontSize = 14.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
            }

            if (totalItems > 0) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF10B981), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$totalItems",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Pharmacy Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFFDBEAFE), RoundedCornerShape(12.dp))
                                .border(2.dp, Color(0xFFBFDBFE), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Ph",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E40AF)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pharmacy.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = pharmacy.address,
                                fontSize = 13.sp,
                                color = Color(0xFF64748B),
                                maxLines = 1
                            )
                            Text(
                                text = pharmacy.phone,
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Medicine Selection Section
            item {
                Text(
                    text = "Select Medicines",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(pharmacy.stock.filter { it.stock > 0 }) { stockItem ->
                MedicineOrderCard(
                    stockItem = stockItem,
                    quantity = selectedItems[stockItem.medicine._id] ?: 0,
                    onQuantityChange = { quantity ->
                        val newItems = selectedItems.toMutableMap()
                        if (quantity > 0) {
                            newItems[stockItem.medicine._id] = quantity
                        } else {
                            newItems.remove(stockItem.medicine._id)
                        }
                        selectedItems = newItems
                    }
                )
            }

            // Delivery Details Section
            item {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Delivery Details",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = deliveryAddress,
                            onValueChange = { deliveryAddress = it },
                            label = { Text("Delivery Address") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            minLines = 2
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = specialInstructions,
                            onValueChange = { specialInstructions = it },
                            label = { Text("Special Instructions (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            minLines = 2
                        )
                    }
                }
            }

            // Prescription Section
            if (requiresPrescription) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Prescription Required",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (prescriptionUri != null) {
                                AsyncImage(
                                    model = prescriptionUri,
                                    contentDescription = "Prescription",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            Button(
                                onClick = { showPrescriptionDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (prescriptionUri != null) Color(0xFF10B981) else Color(0xFFEF4444)
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        if (prescriptionUri != null) Icons.Default.Check else Icons.Default.Place,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        if (prescriptionUri != null) "Change Prescription" else "Upload Prescription",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Order Summary
            if (totalItems > 0) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Order Summary",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "$totalItems items",
                                    fontSize = 14.sp,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = "$${String.format("%.2f", totalPrice)}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }
                    
                        // Prescription Upload Dialog
                        if (showPrescriptionDialog) {
                            androidx.compose.ui.window.Dialog(
                                onDismissRequest = { showPrescriptionDialog = false }
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Text(
                                            text = "Upload Prescription",
                                            fontSize = 20.sp,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                            color = Color(0xFF0F172A),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                    
                                        Text(
                                            text = "Please upload a clear photo of your prescription for verification.",
                                            fontSize = 14.sp,
                                            color = Color(0xFF64748B),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                    
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    if (cameraPermissionState.status.isGranted) {
                                                        try {
                                                            val photoFile = File(context.cacheDir, "prescription_${System.currentTimeMillis()}.jpg")
                                                            photoFile.createNewFile()
                                                            val photoUri = FileProvider.getUriForFile(
                                                                context,
                                                                "${context.packageName}.fileprovider",
                                                                photoFile
                                                            )
                                                            prescriptionUri = photoUri
                                                            currentPhotoFile = photoFile
                                                            cameraLauncher.launch(photoUri)
                                                            showPrescriptionDialog = false
                                                        } catch (e: Exception) {
                                                            // Handle error - could show a toast or log
                                                            prescriptionUri = null
                                                            currentPhotoFile = null
                                                        }
                                                    } else {
                                                        cameraPermissionState.launchPermissionRequest()
                                                    }
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(12.dp),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Add,
                                                        contentDescription = "Camera",
                                                        tint = Color(0xFF10B981)
                                                    )
                                                    Text("Camera", color = Color(0xFF10B981), fontSize = 12.sp)
                                                }
                                            }
                    
                                            OutlinedButton(
                                                onClick = {
                                                    galleryLauncher.launch(
                                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                    )
                                                    showPrescriptionDialog = false
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(12.dp),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.ShoppingCart,
                                                        contentDescription = "Gallery",
                                                        tint = Color(0xFF10B981)
                                                    )
                                                    Text("Gallery", color = Color(0xFF10B981), fontSize = 12.sp)
                                                }
                                            }
                                        }
                    
                                        OutlinedButton(
                                            onClick = { showPrescriptionDialog = false },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF94A3B8))
                                        ) {
                                            Text("Cancel", color = Color(0xFF94A3B8), fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Place Order Button
            item {
                Spacer(modifier = Modifier.height(8.dp))

                val isValidOrder = totalItems > 0 && deliveryAddress.isNotBlank() &&
                    (!requiresPrescription || prescriptionUri != null)

                Button(
                    onClick = {
                        if (deliveryAddress.isBlank()) {
                            // Open map picker if no address
                            showMapPicker = true
                        } else if (requiresPrescription && prescriptionUri == null) {
                            // Show prescription dialog if prescription is required but not uploaded
                            showPrescriptionDialog = true
                        } else {
                            // Place order if all requirements are met
                            val orderItems = selectedItems.map { (medicineId, quantity) ->
                                OrderItem(medicineId, quantity)
                            }
                            viewModel.createOrder(
                                pharmacyId = pharmacy._id,
                                items = orderItems,
                                deliveryAddress = deliveryAddress,
                                specialInstructions = specialInstructions.takeIf { it.isNotBlank() },
                                prescriptionUrl = prescriptionUri?.toString()
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    enabled = totalItems > 0 && orderState !is OrderState.Loading
                ) {
                    if (orderState is OrderState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                if (deliveryAddress.isBlank()) Icons.Default.LocationOn else Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (deliveryAddress.isBlank()) "Select Delivery Location" else "Place Order",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (orderState is OrderState.Error) {
                    Text(
                        text = (orderState as OrderState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Success dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                viewModel.resetState()
                onOrderSuccess()
            },
            title = { Text("Order Placed Successfully!") },
            text = { Text("Your order has been placed and will be processed soon.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        viewModel.resetState()
                        onOrderSuccess()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }

    // Map Picker Dialog
    if (showMapPicker) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showMapPicker = false }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Select Delivery Location",
                        fontSize = 20.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", 0))
                                MapView(ctx).apply {
                                    setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
                                    setMultiTouchControls(true)
                                    controller.setZoom(12.0)
                                    val startPoint = selectedLocation ?: GeoPoint(36.8065, 10.1815) // Tunis
                                    controller.setCenter(startPoint)

                                    // Add map events overlay for tap detection
                                    val mapEventsReceiver = object : MapEventsReceiver {
                                        override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                                            selectedLocation = p
                                            // Update marker
                                            overlays.clear()
                                            val marker = Marker(this@apply).apply {
                                                position = p
                                                title = "Delivery Location"
                                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                            }
                                            overlays.add(marker)
                                            invalidate()
                                            return true
                                        }

                                        override fun longPressHelper(p: GeoPoint): Boolean {
                                            return false
                                        }
                                    }
                                    val eventsOverlay = MapEventsOverlay(mapEventsReceiver)
                                    overlays.add(eventsOverlay)
                                }
                            },
                            update = { mapView ->
                                // Update marker if location selected
                                selectedLocation?.let { location ->
                                    mapView.overlays.clear()
                                    val marker = Marker(mapView).apply {
                                        position = location
                                        title = "Delivery Location"
                                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                    }
                                    val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                                        override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                                            selectedLocation = p
                                            mapView.overlays.clear()
                                            val newMarker = Marker(mapView).apply {
                                                position = p
                                                title = "Delivery Location"
                                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                            }
                                            mapView.overlays.add(newMarker)
                                            mapView.invalidate()
                                            return true
                                        }

                                        override fun longPressHelper(p: GeoPoint): Boolean = false
                                    })
                                    mapView.overlays.add(eventsOverlay)
                                    mapView.overlays.add(marker)
                                    mapView.invalidate()
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showMapPicker = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
                        ) {
                            Text("Cancel", color = Color(0xFF10B981), fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                        }
                        Button(
                            onClick = {
                                selectedLocation?.let {
                                    deliveryAddress = "${it.latitude}, ${it.longitude}"
                                }
                                showMapPicker = false
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Text("Confirm Location", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MedicineOrderCard(
    stockItem: PharmacyStock,
    quantity: Int,
    onQuantityChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Medicine info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stockItem.medicine.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                if (stockItem.medicine.genericName != null) {
                    Text(
                        text = stockItem.medicine.genericName!!,
                        fontSize = 13.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${stockItem.stock} in stock",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF10B981)
                        )
                    }

                    Text(
                        text = "$${String.format("%.2f", stockItem.price)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }
            }

            // Quantity selector
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { if (quantity > 0) onQuantityChange(quantity - 1) },
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            if (quantity > 0) Color(0xFF10B981) else Color(0xFFF1F5F9),
                            CircleShape
                        ),
                    enabled = quantity > 0
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Decrease",
                        tint = if (quantity > 0) Color.White else Color(0xFFCBD5E1),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = quantity.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.width(32.dp),
                    textAlign = TextAlign.Center
                )

                IconButton(
                    onClick = { if (quantity < stockItem.stock) onQuantityChange(quantity + 1) },
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            if (quantity < stockItem.stock) Color(0xFF10B981) else Color(0xFFF1F5F9),
                            CircleShape
                        ),
                    enabled = quantity < stockItem.stock
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        contentDescription = "Increase",
                        tint = if (quantity < stockItem.stock) Color.White else Color(0xFFCBD5E1),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}