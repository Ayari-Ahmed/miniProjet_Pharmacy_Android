package tn.rnu.isetr.miniprojet.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import tn.rnu.isetr.miniprojet.data.PreferencesManager
import tn.rnu.isetr.miniprojet.ui.theme.*
import tn.rnu.isetr.miniprojet.viewmodel.AuthState
import tn.rnu.isetr.miniprojet.viewmodel.AuthViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.isGranted
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import android.Manifest
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tn.rnu.isetr.miniprojet.utils.getCurrentLocation
import tn.rnu.isetr.miniprojet.utils.reverseGeocode

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    preferencesManager: PreferencesManager,
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf<String?>(null) }
    var showMapPicker by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    // Location permissions
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    val authState by viewModel.authState.collectAsState()

    // Animation for logo
    val infiniteTransition = rememberInfiniteTransition(label = "logo animation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                val authData = (authState as AuthState.Success).data
                authData.token?.let { preferencesManager.saveToken(it) }
                authData.user?.let { preferencesManager.saveUser(it) }
                onRegisterSuccess()
            }
            is AuthState.Error -> {
                showError = (authState as AuthState.Error).message
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AppColors.PrimaryExtraPale,
                        AppColors.Background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Animated Logo Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 10.dp)
            ) {
                // Logo with glow effect
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(scale)
                        .background(
                            AppColors.PrimaryGradient,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Inner glow
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                CircleShape
                            )
                    )
                    Icon(
                        imageVector = Icons.Outlined.Home,
                        contentDescription = "MediCare",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Create Account",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AppColors.TextPrimary,
                    letterSpacing = (-0.5).sp
                )

                Text(
                    text = "Join MediCare today",
                    fontSize = 14.sp,
                    color = AppColors.TextSecondary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Register Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.ExtraLarge,
                colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                border = BorderStroke(1.dp, AppColors.BorderLight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Name Field
                    ModernTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = if (it.isBlank()) "Name is required" else null
                        },
                        label = "Full Name",
                        icon = Icons.Outlined.Person,
                        errorMessage = nameError
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Email Field
                    ModernTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = if (it.isBlank()) "Email is required" else null
                        },
                        label = "Email Address",
                        icon = Icons.Outlined.Email,
                        errorMessage = emailError
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password Field
                    ModernTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = if (it.isBlank()) "Password is required" else null
                        },
                        label = "Password",
                        icon = Icons.Outlined.Lock,
                        isPassword = true,
                        errorMessage = passwordError
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Confirm Password Field
                    ModernTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            confirmPasswordError = if (it != password) "Passwords do not match" else null
                        },
                        label = "Confirm Password",
                        icon = Icons.Outlined.Lock,
                        isPassword = true,
                        errorMessage = confirmPasswordError
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Phone Field
                    ModernTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "Phone Number (Optional)",
                        icon = Icons.Outlined.Phone
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Address Field with Location Picker
                    Column(modifier = Modifier.fillMaxWidth()) {
                        ModernTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = "Address (Optional)",
                            icon = Icons.Outlined.LocationOn,
                            isReadOnly = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

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

                                    showMapPicker = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = AppShapes.Medium,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = AppColors.Primary
                            ),
                            border = BorderStroke(1.dp, AppColors.Primary)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pick Location on Map", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Error Message
                    showError?.let {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = AppShapes.Medium,
                            colors = CardDefaults.cardColors(
                                containerColor = AppColors.Error.copy(alpha = 0.1f)
                            ),
                            border = BorderStroke(1.dp, AppColors.Error.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Warning,
                                    contentDescription = null,
                                    tint = AppColors.Error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = it,
                                    fontSize = 13.sp,
                                    color = AppColors.Error,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Register Button
                    PrimaryButton(
                        text = "Create Account",
                        onClick = {
                            showError = null
                            nameError = if (name.isBlank()) "Name is required" else null
                            emailError = if (email.isBlank()) "Email is required" else null
                            passwordError = if (password.isBlank()) "Password is required" else null
                            confirmPasswordError = if (password != confirmPassword) "Passwords do not match" else null

                            if (name.isNotBlank() && email.isNotBlank() && password.isNotBlank() && password == confirmPassword) {
                                viewModel.register(
                                    name,
                                    email,
                                    password,
                                    phone.takeIf { it.isNotBlank() },
                                    address.takeIf { it.isNotBlank() }
                                )
                            }
                        },
                        icon = Icons.Outlined.ArrowForward,
                        isLoading = authState is AuthState.Loading,
                        enabled = name.isNotBlank() && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank()
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Login Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account?",
                    fontSize = 14.sp,
                    color = AppColors.TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                TextButton(
                    onClick = onNavigateToLogin,
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Text(
                        text = "Sign In",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Map Picker Dialog
    if (showMapPicker) {
        AlertDialog(
            onDismissRequest = { showMapPicker = false },
            title = { Text("Select Location") },
            text = {
                Box(modifier = Modifier.height(300.dp)) {
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
            },
            confirmButton = {
                Button(onClick = {
                    selectedLocation?.let { location ->
                        coroutineScope.launch {
                            val addressText = reverseGeocode(context, location)
                            address = addressText
                        }
                    }
                    showMapPicker = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showMapPicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
