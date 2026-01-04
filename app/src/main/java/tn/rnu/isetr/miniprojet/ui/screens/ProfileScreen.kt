package tn.rnu.isetr.miniprojet.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import tn.rnu.isetr.miniprojet.data.User
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 27.dp)
        ) {
            // Compact Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF10B981),
                                Color(0xFF059669)
                            )
                        )
                    )
            ) {
                // Back Button
                IconButton(
                    onClick = { /* Handle back navigation */ },
                    modifier = Modifier
                        .padding(12.dp)
                        .size(40.dp)
                        .background(
                            Color.White.copy(alpha = 0.25f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Title
                Text(
                    text = "My Profile",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                )

                // Logout Button
                IconButton(
                    onClick = {
                        // Clear preferences immediately
                        preferencesManager.clearToken()
                        preferencesManager.clearUser()
                        // Then trigger logout callback
                        onLogout()
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(40.dp)
                        .background(
                            Color.White.copy(alpha = 0.25f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.ExitToApp,
                        contentDescription = "Logout",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (authState) {
                    is AuthState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF10B981),
                                strokeWidth = 3.dp
                            )
                        }
                    }
                    is AuthState.Success -> {
                        val user = (authState as AuthState.Success).data.user

                        if (user != null) {
                            AnimatedContent(
                                targetState = Triple(isEditingProfile, isChangingPassword, showMapPicker),
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(300)) togetherWith
                                    fadeOut(animationSpec = tween(300))
                                },
                                label = "profile animation"
                            ) { (editing, changingPassword, mapPicker) ->
                                when {
                                    !editing && !changingPassword -> {
                                        // View Profile Mode
                                        ProfileInfoCard(
                                            user = user,
                                            onEditClick = { isEditingProfile = true },
                                            onChangePasswordClick = { isChangingPassword = true }
                                        )
                                    }
                                    editing -> {
                                        // Edit Profile Mode
                                        EditProfileCard(
                                            name = name,
                                            onNameChange = { name = it },
                                            phone = phone,
                                            onPhoneChange = { phone = it },
                                            address = address,
                                            onAddressChange = { address = it },
                                            showMapPicker = mapPicker,
                                            onToggleMap = {
                                                coroutineScope.launch {
                                                    if (!locationPermissionsState.permissions.all { it.status.isGranted }) {
                                                        locationPermissionsState.launchMultiplePermissionRequest()
                                                    }
                                                    if (locationPermissionsState.permissions.all { it.status.isGranted }) {
                                                        currentLocation = getCurrentLocation(context)
                                                    }
                                                    showMapPicker = !showMapPicker
                                                }
                                            },
                                            selectedLocation = selectedLocation,
                                            currentLocation = currentLocation,
                                            onLocationSelected = { selectedLocation = it },
                                            onSave = {
                                                viewModel.updateProfile(
                                                    name = name.takeIf { it.isNotBlank() },
                                                    phone = phone.takeIf { it.isNotBlank() },
                                                    address = address.takeIf { it.isNotBlank() }
                                                )
                                                isEditingProfile = false
                                            },
                                            onCancel = { isEditingProfile = false },
                                            onConfirmLocation = {
                                                selectedLocation?.let { location ->
                                                    coroutineScope.launch {
                                                        val addressText = reverseGeocode(context, location)
                                                        address = addressText
                                                    }
                                                }
                                                showMapPicker = false
                                            },
                                            context = context
                                        )
                                    }
                                    changingPassword -> {
                                        // Change Password Mode
                                        ChangePasswordCard(
                                            currentPassword = currentPassword,
                                            onCurrentPasswordChange = { currentPassword = it },
                                            newPassword = newPassword,
                                            onNewPasswordChange = { newPassword = it },
                                            confirmPassword = confirmPassword,
                                            onConfirmPasswordChange = { confirmPassword = it },
                                            onChangePassword = {
                                                if (newPassword == confirmPassword && currentPassword.isNotBlank() &&
                                                    newPassword.isNotBlank() && newPassword.length >= 6) {
                                                    viewModel.changePassword(currentPassword, newPassword)
                                                    isChangingPassword = false
                                                    currentPassword = ""
                                                    newPassword = ""
                                                    confirmPassword = ""
                                                }
                                            },
                                            onCancel = {
                                                isChangingPassword = false
                                                currentPassword = ""
                                                newPassword = ""
                                                confirmPassword = ""
                                            }
                                        )
                                    }
                                }
                            }
                        } else {
                            // User data is null, show error
                            EmptyStateCard(
                                icon = Icons.Default.Close,
                                title = "Failed to load user data",
                                message = "Please try again later",
                                buttonText = "Retry",
                                onButtonClick = { viewModel.getProfile() }
                            )
                        }
                    }
                    is AuthState.Error -> {
                        val errorMessage = (authState as AuthState.Error).message
                        EmptyStateCard(
                            icon = Icons.Default.Close,
                            title = "Error",
                            message = errorMessage,
                            buttonText = "Retry",
                            onButtonClick = { viewModel.getProfile() }
                        )
                    }
                    else -> {
                        // Idle state
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF10B981),
                                strokeWidth = 3.dp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ProfileInfoCard(
    user: User,
    onEditClick: () -> Unit,
    onChangePasswordClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with Avatar and Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF10B981),
                                    Color(0xFF059669)
                                )
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onChangePasswordClick,
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                Color(0xFFF3F4F6),
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Outlined.Lock,
                            contentDescription = "Change Password",
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                Color(0xFF10B981),
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = "Edit Profile",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // User Info
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = user.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user.email,
                    fontSize = 13.sp,
                    color = Color(0xFF9CA3AF)
                )
            }

            Divider(
                color = Color(0xFFF3F4F6),
                thickness = 1.dp
            )

            // Info Items Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileInfoItem(
                    icon = Icons.Outlined.Phone,
                    label = "Phone",
                    value = user.phone ?: "Not provided"
                )
                ProfileInfoItem(
                    icon = Icons.Outlined.LocationOn,
                    label = "Address",
                    value = user.address ?: "Not provided"
                )
                if (user.lastLogin != null) {
                    ProfileInfoItem(
                        icon = Icons.Outlined.Phone,
                        label = "Last Login",
                        value = if (user.lastLogin.length >= 10) user.lastLogin.substring(0, 10) else user.lastLogin
                    )
                }
            }

            // Account Status Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (user.isActive) Color(0xFFF0FDF4) else Color(0xFFFEF2F2),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (user.isActive) Icons.Default.CheckCircle else Icons.Default.Close,
                        contentDescription = null,
                        tint = if (user.isActive) Color(0xFF166534) else Color(0xFFDC2626),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Account Status: ${if (user.isActive) "Active" else "Inactive"}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (user.isActive) Color(0xFF166534) else Color(0xFFDC2626)
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileInfoItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    Color(0xFFF9FAFB),
                    RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color(0xFF9CA3AF),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 14.sp,
                color = Color(0xFF1F2937),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ModernButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector,
    isPrimary: Boolean
) {
    if (isPrimary) {
        Button(
            onClick = onClick,
            modifier = modifier
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF10B981)
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF10B981)
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                Color(0xFF10B981)
            )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun EditProfileCard(
    name: String,
    onNameChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit,
    showMapPicker: Boolean,
    onToggleMap: () -> Unit,
    selectedLocation: GeoPoint?,
    currentLocation: GeoPoint?,
    onLocationSelected: (GeoPoint) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onConfirmLocation: () -> Unit,
    context: android.content.Context
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                Color(0xFFECFDF5),
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Edit Profile",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                }

                IconButton(
                    onClick = onCancel,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Form Fields
            ModernTextField(
                value = name,
                onValueChange = onNameChange,
                label = "Full Name",
                icon = Icons.Outlined.Person
            )

            ModernTextField(
                value = phone,
                onValueChange = onPhoneChange,
                label = "Phone Number",
                icon = Icons.Outlined.Phone
            )

            ModernTextField(
                value = address,
                onValueChange = onAddressChange,
                label = "Address",
                icon = Icons.Outlined.LocationOn,
                isReadOnly = true
            )

            // Pick Location Button
            OutlinedButton(
                onClick = onToggleMap,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF10B981)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    Color(0xFF10B981)
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (showMapPicker) "Hide Map" else "Pick Location on Map",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Map Picker
            AnimatedVisibility(
                visible = showMapPicker,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(modifier = Modifier.height(220.dp)) {
                            AndroidView(
                                factory = { ctx ->
                                    Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", 0))
                                    MapView(ctx).apply {
                                        setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
                                        setMultiTouchControls(true)
                                        controller.setZoom(15.0)
                                        val startPoint = selectedLocation ?: currentLocation ?: GeoPoint(36.8065, 10.1815)
                                        controller.setCenter(startPoint)

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

                                        val mapEventsReceiver = object : MapEventsReceiver {
                                            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                                                onLocationSelected(p)
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

                                            override fun longPressHelper(p: GeoPoint): Boolean = false
                                        }
                                        val eventsOverlay = MapEventsOverlay(mapEventsReceiver)
                                        overlays.add(eventsOverlay)
                                    }
                                },
                                update = { mapView ->
                                    selectedLocation?.let { location ->
                                        mapView.overlays.clear()
                                        val marker = Marker(mapView).apply {
                                            position = location
                                            title = "Selected Location"
                                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                        }
                                        val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                                            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                                                onLocationSelected(p)
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onCancel,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD1D5DB))
                            ) {
                                Text("Cancel", color = Color(0xFF6B7280), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                            Button(
                                onClick = onConfirmLocation,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                            ) {
                                Text("Confirm", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    enabled = name.isNotBlank(),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFD1D5DB)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6B7280))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cancel", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun ChangePasswordCard(
    currentPassword: String,
    onCurrentPasswordChange: (String) -> Unit,
    newPassword: String,
    onNewPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    onChangePassword: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                Color(0xFFECFDF5),
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Change Password",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                }

                IconButton(
                    onClick = onCancel,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Form Fields
            ModernTextField(
                value = currentPassword,
                onValueChange = onCurrentPasswordChange,
                label = "Current Password",
                icon = Icons.Outlined.Lock,
                isPassword = true
            )

            ModernTextField(
                value = newPassword,
                onValueChange = onNewPasswordChange,
                label = "New Password",
                icon = Icons.Outlined.Lock,
                isPassword = true
            )

            ModernTextField(
                value = confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = "Confirm New Password",
                icon = Icons.Outlined.Lock,
                isPassword = true
            )

            // Password Requirements
            if (newPassword.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFFF9FAFB),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Password Requirements:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF6B7280)
                    )
                    PasswordRequirement(
                        text = "At least 6 characters",
                        isMet = newPassword.length >= 6
                    )
                    PasswordRequirement(
                        text = "Passwords match",
                        isMet = newPassword == confirmPassword && confirmPassword.isNotEmpty()
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onChangePassword,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    enabled = currentPassword.isNotBlank() && newPassword.isNotBlank() &&
                            confirmPassword.isNotBlank() && newPassword == confirmPassword && newPassword.length >= 6,
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Change", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFD1D5DB)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6B7280))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cancel", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    isPassword: Boolean = false,
    isReadOnly: Boolean = false
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 13.sp) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        readOnly = isReadOnly,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF10B981),
            unfocusedBorderColor = Color(0xFFE5E7EB),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color(0xFFF9FAFB),
            cursorColor = Color(0xFF10B981)
        ),
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(18.dp)
            )
        },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else null,
        visualTransformation = if (isPassword && !isPasswordVisible) {
            androidx.compose.ui.text.input.PasswordVisualTransformation()
        } else {
            androidx.compose.ui.text.input.VisualTransformation.None
        },
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
    )
}

@Composable
fun PasswordRequirement(
    text: String,
    isMet: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isMet) Icons.Default.CheckCircle else Icons.Default.Close,
            contentDescription = null,
            tint = if (isMet) Color(0xFF10B981) else Color(0xFFD1D5DB),
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = if (isMet) Color(0xFF10B981) else Color(0xFF9CA3AF)
        )
    }
}

@Composable
fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    message: String,
    buttonText: String,
    onButtonClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(
                        Color(0xFFFEE2E2),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(35.dp)
                )
            }

            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )

            Text(
                text = message,
                fontSize = 13.sp,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onButtonClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(buttonText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
        }
    }
}
