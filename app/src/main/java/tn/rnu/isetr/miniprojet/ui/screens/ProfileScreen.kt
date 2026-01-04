package tn.rnu.isetr.miniprojet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import tn.rnu.isetr.miniprojet.data.User
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import tn.rnu.isetr.miniprojet.MainActivity
import tn.rnu.isetr.miniprojet.data.PreferencesManager
import tn.rnu.isetr.miniprojet.viewmodel.AuthState
import tn.rnu.isetr.miniprojet.viewmodel.AuthViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.isGranted
import android.location.Geocoder
import android.Manifest
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tn.rnu.isetr.miniprojet.utils.getCurrentLocation
import tn.rnu.isetr.miniprojet.utils.reverseGeocode

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel(),
    preferencesManager: PreferencesManager,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val authState by viewModel.authState.collectAsState()
    var isEditingProfile by remember { mutableStateOf(false) }
    var isChangingPassword by remember { mutableStateOf(false) }
    var showMapPicker by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }

    // Location permissions
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Form fields
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Load profile on first launch
    LaunchedEffect(Unit) {
        viewModel.getProfile()
    }

    // Update form fields when profile loads
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            val user = (authState as AuthState.Success).data.user
            if (user != null) {
                name = user.name
                phone = user.phone ?: ""
                address = user.address ?: ""
            }
        }
    }

    Column(
        modifier = modifier
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
                onClick = { /* Handle back navigation */ },
                modifier = Modifier
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            ) {
                Icon(
                    androidx.compose.material.icons.Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "Profile",
                fontSize = 20.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = {
                    preferencesManager.clearToken()
                    preferencesManager.clearUser()
                    onLogout()
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFEF4444)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444))
            ) {
                Text("Logout", color = Color(0xFFEF4444))
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (authState) {
                is AuthState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is AuthState.Success -> {
                    val user = (authState as AuthState.Success).data.user

                    if (user != null) {
                        if (!isEditingProfile && !isChangingPassword) {
                            // View Profile Mode
                            ProfileInfoCard(user = user)

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { isEditingProfile = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                                ) {
                                    Text("Edit Profile", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                                }
                                OutlinedButton(
                                    onClick = { isChangingPassword = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
                                ) {
                                    Text("Change Password", color = Color(0xFF10B981), fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                                }
                            }
                        } else if (isEditingProfile) {
                            // Edit Profile Mode
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "Edit Profile",
                                        fontSize = 18.sp,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )

                                    OutlinedTextField(
                                        value = name,
                                        onValueChange = { name = it },
                                        label = { Text("Name") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF10B981),
                                            unfocusedBorderColor = Color(0xFFE2E8F0),
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color(0xFFF8FAFC)
                                        )
                                    )

                                    OutlinedTextField(
                                        value = phone,
                                        onValueChange = { phone = it },
                                        label = { Text("Phone") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF10B981),
                                            unfocusedBorderColor = Color(0xFFE2E8F0),
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color(0xFFF8FAFC)
                                        )
                                    )

                                    OutlinedTextField(
                                        value = address,
                                        onValueChange = { address = it },
                                        label = { Text("Address") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF10B981),
                                            unfocusedBorderColor = Color(0xFFE2E8F0),
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color(0xFFF8FAFC)
                                        )
                                    )

                                    OutlinedButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                // Request permissions if not granted
                                                if (!locationPermissionsState.permissions.all { it.status.isGranted }) {
                                                    locationPermissionsState.launchMultiplePermissionRequest()
                                                }

                                                // Try to get current location
                                                if (locationPermissionsState.permissions.all { it.status.isGranted }) {
                                                    currentLocation = getCurrentLocation(context)
                                                }

                                                showMapPicker = !showMapPicker
                                            }
                                        },
                                        modifier = Modifier.align(Alignment.End),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = Color(0xFF10B981)
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(if (showMapPicker) "Hide Map" else "Pick Location", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                                    }

                                    if (showMapPicker) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                                        ) {
                                            Box(modifier = Modifier.height(250.dp)) {
                                                AndroidView(
                                                    factory = { ctx ->
                                                        Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", 0))
                                                        MapView(ctx).apply {
                                                            setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
                                                            setMultiTouchControls(true)
                                                            controller.setZoom(15.0)
                                                            val startPoint = selectedLocation ?: currentLocation ?: GeoPoint(36.8065, 10.1815) // Tunis
                                                            controller.setCenter(startPoint)

                                                            // Add current location marker if available
                                                            currentLocation?.let { currentLoc ->
                                                                if (selectedLocation == null) {
                                                                    val currentMarker = Marker(this).apply {
                                                                        position = currentLoc
                                                                        title = "Current Location"
                                                                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                                                    }
                                                                    overlays.add(currentMarker)
                                                                }
                                                            }

                                                            // Add map events overlay for tap detection
                                                            val mapEventsReceiver = object : MapEventsReceiver {
                                                                override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                                                                    selectedLocation = p
                                                                    // Update marker
                                                                    overlays.clear()
                                                                    val marker = Marker(this@apply).apply {
                                                                        position = p
                                                                        title = "Selected Location"
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
                                                                title = "Selected Location"
                                                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                                            }
                                                            val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                                                                override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                                                                    selectedLocation = p
                                                                    mapView.overlays.clear()
                                                                    val newMarker = Marker(mapView).apply {
                                                                        position = p
                                                                        title = "Selected Location"
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
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                                    selectedLocation?.let { location ->
                                                        coroutineScope.launch {
                                                            val addressText = reverseGeocode(context, location)
                                                            address = addressText
                                                        }
                                                    }
                                                    showMapPicker = false
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                                            ) {
                                                Text("Confirm", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                viewModel.updateProfile(
                                                    name = name.takeIf { it.isNotBlank() },
                                                    phone = phone.takeIf { it.isNotBlank() },
                                                    address = address.takeIf { it.isNotBlank() }
                                                )
                                                isEditingProfile = false
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                            enabled = name.isNotBlank()
                                        ) {
                                            Text("Save", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                                        }
                                        OutlinedButton(
                                            onClick = { isEditingProfile = false },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
                                        ) {
                                            Text("Cancel", color = Color(0xFF10B981), fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                                        }
                                    }
                            
                                    Spacer(modifier = Modifier.height(20.dp))
                                }
                            }
                        } else if (isChangingPassword) {
                            // Change Password Mode
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "Change Password",
                                        fontSize = 18.sp,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )

                                    OutlinedTextField(
                                        value = currentPassword,
                                        onValueChange = { currentPassword = it },
                                        label = { Text("Current Password") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF10B981),
                                            unfocusedBorderColor = Color(0xFFE2E8F0),
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color(0xFFF8FAFC)
                                        )
                                    )

                                    OutlinedTextField(
                                        value = newPassword,
                                        onValueChange = { newPassword = it },
                                        label = { Text("New Password") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF10B981),
                                            unfocusedBorderColor = Color(0xFFE2E8F0),
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color(0xFFF8FAFC)
                                        )
                                    )

                                    OutlinedTextField(
                                        value = confirmPassword,
                                        onValueChange = { confirmPassword = it },
                                        label = { Text("Confirm New Password") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF10B981),
                                            unfocusedBorderColor = Color(0xFFE2E8F0),
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color(0xFFF8FAFC)
                                        )
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                if (newPassword == confirmPassword && currentPassword.isNotBlank() && newPassword.isNotBlank() && newPassword.length >= 6) {
                                                    viewModel.changePassword(currentPassword, newPassword)
                                                    isChangingPassword = false
                                                    currentPassword = ""
                                                    newPassword = ""
                                                    confirmPassword = ""
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                            enabled = currentPassword.isNotBlank() && newPassword.isNotBlank() &&
                                                    confirmPassword.isNotBlank() && newPassword == confirmPassword && newPassword.length >= 6
                                        ) {
                                            Text("Change Password", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                isChangingPassword = false
                                                currentPassword = ""
                                                newPassword = ""
                                                confirmPassword = ""
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
                                        ) {
                                            Text("Cancel", color = Color(0xFF10B981), fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        
                        }
                    } else {
                        // User data is null, show error
                        Text(
                            text = "Failed to load user data",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(
                            onClick = { viewModel.getProfile() },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
                is AuthState.Error -> {
                    val errorMessage = (authState as AuthState.Error).message
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = { viewModel.getProfile() },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
                else -> {
                    // Idle state
                    Text("Loading profile...")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun ProfileInfoCard(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Personal Information",
                fontSize = 18.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            ProfileInfoRow(label = "Name", value = user.name)
            ProfileInfoRow(label = "Email", value = user.email)
            ProfileInfoRow(label = "Phone", value = user.phone ?: "Not provided")
            ProfileInfoRow(label = "Address", value = user.address ?: "Not provided")

            if (user.lastLogin != null && user.lastLogin.length >= 10) {
                ProfileInfoRow(label = "Last Login", value = user.lastLogin.substring(0, 10))
            } else if (user.lastLogin != null) {
                ProfileInfoRow(label = "Last Login", value = user.lastLogin)
            }

            Box(
                modifier = Modifier
                    .background(
                        if (user.isActive) Color(0xFFF0FDF4) else Color(0xFFFEF2F2),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Account Status: ${if (user.isActive) "Active" else "Inactive"}",
                    fontSize = 13.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    color = if (user.isActive) Color(0xFF166534) else Color(0xFFDC2626)
                )
            }
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}