package tn.rnu.isetr.miniprojet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
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
    var currentPharmacyScreen by rememberSaveable { mutableStateOf<PharmacyScreen?>(null) }

    val onLogout = {
        preferencesManager.logout()
        isLoggedIn = false
        isPharmacyLoggedIn = false
        showLogin = true
        showRegister = false
        showPharmacyLogin = false
        selectedPharmacy = null
        selectedOrder = null
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
        val pharmacy = preferencesManager.getPharmacy()
        if (pharmacy != null) {
            when (currentPharmacyScreen) {
                PharmacyScreen.DASHBOARD -> PharmacyDashboardScreen(
                    pharmacy = pharmacy,
                    preferencesManager = preferencesManager,
                    onNavigateToStock = { currentPharmacyScreen = PharmacyScreen.STOCK },
                    onNavigateToOrders = { currentPharmacyScreen = PharmacyScreen.ORDERS },
                    onLogout = onLogout
                )
                PharmacyScreen.STOCK -> PharmacyStockScreen(
                    pharmacy = pharmacy,
                    preferencesManager = preferencesManager,
                    onNavigateBack = { currentPharmacyScreen = PharmacyScreen.DASHBOARD }
                )
                PharmacyScreen.ORDERS -> PharmacyOrdersScreen(
                    pharmacy = pharmacy,
                    preferencesManager = preferencesManager,
                    onNavigateBack = { currentPharmacyScreen = PharmacyScreen.DASHBOARD }
                )
                null -> {
                    // Default to dashboard if no screen is set
                    currentPharmacyScreen = PharmacyScreen.DASHBOARD
                    PharmacyDashboardScreen(
                        pharmacy = pharmacy,
                        preferencesManager = preferencesManager,
                        onNavigateToStock = { currentPharmacyScreen = PharmacyScreen.STOCK },
                        onNavigateToOrders = { currentPharmacyScreen = PharmacyScreen.ORDERS },
                        onLogout = onLogout
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

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Surface(modifier = Modifier.fillMaxSize(), color = Color.White, tonalElevation = 0.dp) {
                NavigationSuiteScaffold(
                    navigationSuiteColors = NavigationSuiteDefaults.colors(
                        navigationBarContainerColor = Color.White,
                        navigationBarContentColor = Color(0xFF0DFF9D),
                        navigationDrawerContainerColor = Color.White
                    ),
                    navigationSuiteItems = {
                        AppDestinations.entries.forEach {
                            item(
                                icon = {
                                    Icon(
                                        it.icon,
                                        contentDescription = it.label
                                    )
                                },
                                label = { Text(it.label) },
                                selected = it == currentDestination,
                                onClick = { currentDestination = it }
                            )
                        }
                    }
                ) {
                    when (currentDestination) {
                        AppDestinations.HOME -> HomeScreen(
                            modifier = Modifier.padding(innerPadding),
                            preferencesManager = preferencesManager,
                            onLogout = onLogout
                        )
                        AppDestinations.PHARMACIES -> PharmaciesScreen(
                            modifier = Modifier.padding(innerPadding),
                            onOrderClick = onOrderClick,
                            preferencesManager = preferencesManager
                        )
                        AppDestinations.MAP -> MapScreen(modifier = Modifier.padding(innerPadding))
                        AppDestinations.ORDERS -> OrdersScreen(
                            modifier = Modifier.padding(innerPadding),
                            onOrderClick = onOrderDetailsClick
                        )
                        AppDestinations.PROFILE -> ProfileScreen(
                            modifier = Modifier.padding(innerPadding),
                            preferencesManager = preferencesManager,
                            onLogout = onLogout
                        )
                    }
                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    PHARMACIES("Pharmacies", Icons.Default.Favorite),
    MAP("Map", Icons.Default.LocationOn),
    ORDERS("Orders", Icons.Default.AccountBox),
    PROFILE("Profile", Icons.Default.AccountBox),
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