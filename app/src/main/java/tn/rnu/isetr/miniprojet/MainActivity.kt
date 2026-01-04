package tn.rnu.isetr.miniprojet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.rnu.isetr.miniprojet.data.Order
import tn.rnu.isetr.miniprojet.data.Pharmacy
import tn.rnu.isetr.miniprojet.ui.screens.HomeScreen
import tn.rnu.isetr.miniprojet.data.PreferencesManager
import tn.rnu.isetr.miniprojet.data.RetrofitClient
import tn.rnu.isetr.miniprojet.ui.screens.LoginScreen
import tn.rnu.isetr.miniprojet.ui.screens.MapScreen
import tn.rnu.isetr.miniprojet.ui.screens.OrderDetailsScreen
import tn.rnu.isetr.miniprojet.ui.screens.OrderScreen
import tn.rnu.isetr.miniprojet.ui.screens.OrdersScreen
import tn.rnu.isetr.miniprojet.ui.screens.PharmaciesScreen
import tn.rnu.isetr.miniprojet.ui.screens.PharmacyDashboardScreen
import tn.rnu.isetr.miniprojet.ui.screens.PharmacyLoginScreen
import tn.rnu.isetr.miniprojet.ui.screens.PharmacyOrdersScreen
import tn.rnu.isetr.miniprojet.ui.screens.PharmacyStockScreen
import tn.rnu.isetr.miniprojet.ui.screens.ProfileScreen
import tn.rnu.isetr.miniprojet.ui.screens.RegisterScreen
import tn.rnu.isetr.miniprojet.ui.theme.MiniProjetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val preferencesManager = PreferencesManager(this)
        RetrofitClient.initialize(preferencesManager)
        setContent {
            MiniProjetTheme(dynamicColor = false) {
                MiniProjetApp(preferencesManager)
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun MiniProjetApp(preferencesManager: PreferencesManager) {
    var isLoggedIn by rememberSaveable { mutableStateOf(preferencesManager.isLoggedIn()) }
    var isPharmacyLoggedIn by rememberSaveable { mutableStateOf(preferencesManager.getPharmacy() != null) }
    var showLogin by rememberSaveable { mutableStateOf(!isLoggedIn && !isPharmacyLoggedIn) }
    var showRegister by rememberSaveable { mutableStateOf(false) }
    var showPharmacyLogin by rememberSaveable { mutableStateOf(false) }
    var selectedPharmacy by rememberSaveable { mutableStateOf<Pharmacy?>(null) }
    var selectedOrder by rememberSaveable { mutableStateOf<Order?>(null) }
    var currentPharmacy by rememberSaveable { mutableStateOf(preferencesManager.getPharmacy()) }
    var currentPharmacyScreen by rememberSaveable { mutableStateOf<PharmacyScreen?>(null) }

    val onLogout = {
        // Clear preferences first
        preferencesManager.logout()
        // Then update all states atomically
        isLoggedIn = false
        isPharmacyLoggedIn = false
        showLogin = true
        showRegister = false
        showPharmacyLogin = false
        selectedPharmacy = null
        selectedOrder = null
        currentPharmacy = null
        currentPharmacyScreen = null
    }

    // Handle order placement first (highest priority)
    if (selectedPharmacy != null) {
        val pharmacy = selectedPharmacy!!
        OrderScreen(
            pharmacy = pharmacy,
            onOrderSuccess = {
                selectedPharmacy = null
            },
            onBack = {
                selectedPharmacy = null
            }
        )
    } else if (selectedOrder != null) {
        val order = selectedOrder!!
        OrderDetailsScreen(
            order = order,
            onBack = {
                selectedOrder = null
            }
        )
    } else if (showLogin || showRegister || showPharmacyLogin) {
        when {
            showLogin -> LoginScreen(
                onLoginSuccess = {
                    isLoggedIn = true
                    showLogin = false
                },
                onNavigateToRegister = {
                    showLogin = false
                    showRegister = true
                },
                onNavigateToPharmacyLogin = {
                    showLogin = false
                    showPharmacyLogin = true
                },
                preferencesManager = preferencesManager
            )
            showRegister -> RegisterScreen(
                onRegisterSuccess = {
                    isLoggedIn = true
                    showRegister = false
                },
                onNavigateToLogin = {
                    showRegister = false
                    showLogin = true
                },
                preferencesManager = preferencesManager
            )
            showPharmacyLogin -> PharmacyLoginScreen(
                onLoginSuccess = {
                    isPharmacyLoggedIn = true
                    showPharmacyLogin = false
                    currentPharmacyScreen = PharmacyScreen.DASHBOARD
                },
                onNavigateToRegister = {
                    // For now, navigate back to login selection
                    showPharmacyLogin = false
                    showLogin = true
                },
                preferencesManager = preferencesManager
            )
        }
    } else if (isPharmacyLoggedIn) {
        currentPharmacy = currentPharmacy ?: preferencesManager.getPharmacy()
        if (currentPharmacy != null) {
            when (currentPharmacyScreen) {
                PharmacyScreen.DASHBOARD -> PharmacyDashboardScreen(
                    pharmacy = currentPharmacy!!,
                    preferencesManager = preferencesManager,
                    onNavigateToStock = { currentPharmacyScreen = PharmacyScreen.STOCK },
                    onNavigateToOrders = { currentPharmacyScreen = PharmacyScreen.ORDERS },
                    onLogout = onLogout,
                    onRefreshPharmacy = { updatedPharmacy ->
                        currentPharmacy = updatedPharmacy
                        preferencesManager.savePharmacy(updatedPharmacy)
                    }
                )
                PharmacyScreen.STOCK -> PharmacyStockScreen(
                    initialPharmacy = currentPharmacy!!,
                    preferencesManager = preferencesManager,
                    onNavigateBack = { currentPharmacyScreen = PharmacyScreen.DASHBOARD },
                    onPharmacyUpdated = { updatedPharmacy ->
                        currentPharmacy = updatedPharmacy
                    }
                )
                PharmacyScreen.ORDERS -> PharmacyOrdersScreen(
                    pharmacy = currentPharmacy!!,
                    preferencesManager = preferencesManager,
                    onNavigateBack = { currentPharmacyScreen = PharmacyScreen.DASHBOARD }
                )
                null -> {
                    // Default to dashboard if no screen is set
                    currentPharmacyScreen = PharmacyScreen.DASHBOARD
                    PharmacyDashboardScreen(
                        pharmacy = currentPharmacy!!,
                        preferencesManager = preferencesManager,
                        onNavigateToStock = { currentPharmacyScreen = PharmacyScreen.STOCK },
                        onNavigateToOrders = { currentPharmacyScreen = PharmacyScreen.ORDERS },
                        onLogout = onLogout,
                        onRefreshPharmacy = { updatedPharmacy ->
                            currentPharmacy = updatedPharmacy
                            preferencesManager.savePharmacy(updatedPharmacy)
                        }
                    )
                }
            }
        } else {
            // Pharmacy data is corrupted, logout
            onLogout()
        }
    } else if (isLoggedIn) {
        MainApp(
            onOrderClick = { pharmacy ->
                selectedPharmacy = pharmacy
            },
            onOrderDetailsClick = { order ->
                selectedOrder = order
            },
            onLogout = onLogout,
            preferencesManager = preferencesManager
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(onOrderClick: (Pharmacy) -> Unit, onOrderDetailsClick: (Order) -> Unit, onLogout: () -> Unit, preferencesManager: PreferencesManager) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    val user = preferencesManager.getUser()
    val context = LocalContext.current

    Scaffold(
        bottomBar = {
            ModernNavigationBar(
                currentDestination = currentDestination,
                onDestinationSelected = { currentDestination = it }
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentDestination) {
                AppDestinations.HOME -> HomeScreen(
                    preferencesManager = preferencesManager,
                    onLogout = onLogout
                )
                AppDestinations.PHARMACIES -> PharmaciesScreen(
                    onOrderClick = onOrderClick,
                    preferencesManager = preferencesManager
                )
                AppDestinations.MAP -> MapScreen()
                AppDestinations.ORDERS -> OrdersScreen(
                    onOrderClick = onOrderDetailsClick
                )
                AppDestinations.PROFILE -> ProfileScreen(
                    preferencesManager = preferencesManager,
                    onLogout = onLogout
                )
            }
        }
    }
}

@Composable
fun ModernNavigationBar(
    currentDestination: AppDestinations,
    onDestinationSelected: (AppDestinations) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 40.dp)
            .shadow(
                elevation = 12.dp,
                spotColor = Color(0xFF10B981).copy(alpha = 0.15f),
                ambientColor = Color(0xFF10B981).copy(alpha = 0.1f)
            ),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppDestinations.entries.forEach { destination ->
                val isSelected = destination == currentDestination
                val icon = if (isSelected) destination.selectedIcon else destination.icon

                ModernNavigationItem(
                    icon = icon,
                    label = destination.label,
                    isSelected = isSelected,
                    onClick = { onDestinationSelected(destination) }
                )
            }
        }
    }
}

@Composable
fun ModernNavigationItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF10B981),
                Color(0xFF059669)
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(Color.Transparent, Color.Transparent)
        )
    }

    val iconColor = if (isSelected) Color.White else Color(0xFF94A3B8)
    val textColor = if (isSelected) Color(0xFF10B981) else Color(0xFF94A3B8)

    Box(
        modifier = Modifier
            .width(65.dp)
            .height(54.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .then(
                if (isSelected) {
                    Modifier.shadow(
                        elevation = 8.dp,
                        spotColor = Color(0xFF10B981).copy(alpha = 0.4f),
                        ambientColor = Color(0xFF10B981).copy(alpha = 0.2f)
                    )
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor,
                letterSpacing = 0.2.sp
            )
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
) {
    HOME("Home", Icons.Outlined.Home, Icons.Default.Home),
    PHARMACIES("Pharmacies", Icons.Outlined.Favorite, Icons.Default.Favorite),
    MAP("Map", Icons.Outlined.LocationOn, Icons.Default.LocationOn),
    ORDERS("Orders", Icons.Outlined.AccountBox, Icons.Default.AccountBox),
    PROFILE("Profile", Icons.Outlined.AccountBox, Icons.Default.AccountBox),
}

enum class PharmacyScreen {
    DASHBOARD,
    STOCK,
    ORDERS
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MiniProjetTheme {
        Greeting("Android")
    }
}