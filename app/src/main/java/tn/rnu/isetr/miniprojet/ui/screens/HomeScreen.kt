package tn.rnu.isetr.miniprojet.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import tn.rnu.isetr.miniprojet.data.Order
import tn.rnu.isetr.miniprojet.data.Pharmacy
import tn.rnu.isetr.miniprojet.data.PreferencesManager
import tn.rnu.isetr.miniprojet.data.User
import tn.rnu.isetr.miniprojet.ui.theme.*
import tn.rnu.isetr.miniprojet.viewmodel.AuthViewModel
import tn.rnu.isetr.miniprojet.viewmodel.OrderViewModel
import tn.rnu.isetr.miniprojet.viewmodel.OrderState
import tn.rnu.isetr.miniprojet.viewmodel.PharmacyViewModel
import tn.rnu.isetr.miniprojet.viewmodel.PharmacyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    preferencesManager: PreferencesManager,
    onLogout: () -> Unit,
    onPharmacyClick: (Pharmacy) -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onOrderDetailsClick: (Order) -> Unit = {},
    pharmacyViewModel: PharmacyViewModel = viewModel(),
    orderViewModel: OrderViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val pharmacyState by pharmacyViewModel.pharmacyState.collectAsState()
    val orderState by orderViewModel.orderState.collectAsState()
    val user = remember { preferencesManager.getUser() }

    // Dynamic stats
    val pharmaciesCount = when (pharmacyState) {
        is PharmacyState.Success -> (pharmacyState as PharmacyState.Success).pharmacies.size
        else -> 0
    }

    val ordersCount = when (orderState) {
        is OrderState.OrdersLoaded -> (orderState as OrderState.OrdersLoaded).orders.size
        else -> 0
    }

    val recentOrders = when (orderState) {
        is OrderState.OrdersLoaded -> (orderState as OrderState.OrdersLoaded).orders.take(3)
        else -> emptyList()
    }

    val nearbyPharmacies = when (pharmacyState) {
        is PharmacyState.Success -> (pharmacyState as PharmacyState.Success).pharmacies.take(4)
        else -> emptyList()
    }

    LaunchedEffect(Unit) {
        pharmacyViewModel.getPharmacies()
        orderViewModel.getUserOrders()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(top = 20.dp)
    ) {
        // Header with gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.PrimaryGradient)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column {
                // Top bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user?.name?.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Column {
                            Text(
                                text = "Hello, ${user?.name?.split(" ")?.firstOrNull() ?: "User"}!",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Find your medicines",
                                fontSize = 13.sp,
                                color = AppColors.PrimaryPale,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { /* TODO: Notifications */ },
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                onLogout()
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.ExitToApp,
                                contentDescription = "Logout",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Search Bar
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.Large,
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = AppColors.TextTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Search medicines, pharmacies...",
                            fontSize = 14.sp,
                            color = AppColors.TextTertiary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Stats Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCardNew(
                        title = "Pharmacies",
                        value = pharmaciesCount.toString(),
                        icon = Icons.Outlined.Place,
                        color = AppColors.Secondary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCardNew(
                        title = "My Orders",
                        value = ordersCount.toString(),
                        icon = Icons.Default.ShoppingCart,
                        color = AppColors.Primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            // Quick Actions Section
            SectionHeader(
                title = "Quick Actions",
                icon = Icons.Outlined.Star
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionCard(
                    title = "Browse Pharmacies",
                    description = "Find nearby pharmacies",
                    icon = Icons.Outlined.Place,
                    color = AppColors.Primary,
                    modifier = Modifier.weight(1f),
                    onClick = { /* Navigate to pharmacies */ }
                )
                ActionCard(
                    title = "My Orders",
                    description = "Track your orders",
                    icon = Icons.Default.ShoppingCart,
                    color = AppColors.Secondary,
                    modifier = Modifier.weight(1f),
                    onClick = onOrdersClick
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Nearby Pharmacies Section
            SectionHeader(
                title = "Nearby Pharmacies",
                subtitle = "Closest to your location",
                icon = Icons.Outlined.Place,
                actionText = "See All",
                onActionClick = { /* Navigate to all pharmacies */ }
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (nearbyPharmacies.isEmpty()) {
                EmptyStateCard(
                    icon = Icons.Outlined.Place,
                    title = "No Pharmacies Found",
                    message = "Pharmacies will appear here once available"
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(nearbyPharmacies) { pharmacy ->
                        PharmacyCardCompact(
                            pharmacy = pharmacy,
                            onClick = { onPharmacyClick(pharmacy) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recent Orders Section
            SectionHeader(
                title = "Recent Orders",
                subtitle = "Your latest purchases",
                icon = Icons.Outlined.ShoppingCart,
                actionText = "View All",
                onActionClick = onOrdersClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (recentOrders.isEmpty()) {
                EmptyStateCard(
                    icon = Icons.Default.ShoppingCart,
                    title = "No Orders Yet",
                    message = "Start ordering medicines to see them here"
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    recentOrders.forEach { order ->
                        OrderCardCompact(
                            order = order,
                            onClick = { onOrderDetailsClick(order) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Health Tips Section
            InfoCard(
                icon = Icons.Outlined.Favorite,
                title = "Health Tip of the Day",
                description = "Stay hydrated! Drink at least 8 glasses of water daily to maintain good health.",
                backgroundColor = AppColors.PrimaryExtraPale,
                iconColor = AppColors.Primary
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun StatCardNew(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = AppShapes.Large,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = AppColors.TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = AppShapes.Large,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, AppColors.BorderLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = description,
                fontSize = 11.sp,
                color = AppColors.TextSecondary,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun PharmacyCardCompact(
    pharmacy: Pharmacy,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable(onClick = onClick),
        shape = AppShapes.Large,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, AppColors.BorderLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(AppColors.SecondaryPale, CircleShape)
                    .border(2.dp, AppColors.SecondaryLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Ph",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Secondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Name
            Text(
                text = pharmacy.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Address
            Text(
                text = pharmacy.address,
                fontSize = 11.sp,
                color = AppColors.TextSecondary,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(AppColors.Primary, CircleShape)
                )
                Text(
                    text = "Open",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.Primary
                )
                Text(
                    text = "• 2.3 km",
                    fontSize = 11.sp,
                    color = AppColors.TextTertiary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun OrderCardCompact(
    order: Order,
    onClick: () -> Unit
) {
    val statusColor = when (order.status.lowercase()) {
        "pending" -> AppColors.Info
        "confirmed" -> AppColors.Purple
        "processing", "preparing" -> AppColors.Warning
        "ready", "delivered" -> AppColors.Success
        "cancelled" -> AppColors.Error
        else -> AppColors.TextSecondary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = AppShapes.Large,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, AppColors.BorderLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(statusColor.copy(alpha = 0.15f), AppShapes.Medium),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (order.status.lowercase()) {
                        "pending" -> Icons.Outlined.Info
                        "confirmed" -> Icons.Outlined.CheckCircle
                        "processing", "preparing" -> Icons.Outlined.Edit
                        "ready", "delivered" -> Icons.Outlined.Check
                        "cancelled" -> Icons.Outlined.Close
                        else -> Icons.Outlined.ShoppingCart
                    },
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Order Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Order #${order._id.takeLast(6).uppercase()}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
                Text(
                    text = "${order.items.size} items • $${String.format("%.2f", order.totalAmount)}",
                    fontSize = 12.sp,
                    color = AppColors.TextSecondary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Status Badge
            StatusBadge(
                text = order.status.replaceFirstChar { it.uppercase() },
                status = when (order.status.lowercase()) {
                    "pending" -> StatusType.INFO
                    "confirmed" -> StatusType.PURPLE
                    "processing", "preparing" -> StatusType.WARNING
                    "ready", "delivered" -> StatusType.SUCCESS
                    "cancelled" -> StatusType.ERROR
                    else -> StatusType.INFO
                }
            )
        }
    }
}