package tn.rnu.isetr.miniprojet.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tn.rnu.isetr.miniprojet.data.Order
import tn.rnu.isetr.miniprojet.data.Pharmacy
import tn.rnu.isetr.miniprojet.data.PreferencesManager
import tn.rnu.isetr.miniprojet.ui.theme.*
import tn.rnu.isetr.miniprojet.viewmodel.OrderState
import tn.rnu.isetr.miniprojet.viewmodel.OrderViewModel
import tn.rnu.isetr.miniprojet.viewmodel.PharmacyViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacyDashboardScreen(
    pharmacy: Pharmacy,
    preferencesManager: PreferencesManager,
    onNavigateToStock: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onLogout: () -> Unit,
    onRefreshPharmacy: (Pharmacy) -> Unit = {},
    orderViewModel: OrderViewModel = viewModel(),
    pharmacyViewModel: PharmacyViewModel = viewModel()
) {
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()
    val orderState by orderViewModel.orderState.collectAsState()

    // Animation states
    val infiniteTransition = rememberInfiniteTransition(label = "infinite")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )

    // Greeting based on time
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 0..11 -> "Good Morning"
            in 12..17 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    LaunchedEffect(Unit) {
        orderViewModel.getPharmacyOrders()
    }

    fun refreshData() {
        coroutineScope.launch {
            isRefreshing = true
            pharmacyViewModel.getPharmacy(pharmacy._id) { updatedPharmacy ->
                updatedPharmacy?.let { onRefreshPharmacy(it) }
            }
            orderViewModel.getPharmacyOrders()
            delay(1000)
            isRefreshing = false
        }
    }

    PullToRefreshBox(
        state = pullRefreshState,
        isRefreshing = isRefreshing,
        onRefresh = { refreshData() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Epic Header with Gradient
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        AppColors.Primary,
                                        AppColors.PrimaryDark
                                    )
                                )
                            )
                            .padding(horizontal = 20.dp, vertical = 24.dp)
                    ) {
                        Column {
                            // Top Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Greeting
                                Column {
                                    Text(
                                        text = greeting,
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = pharmacy.name,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        letterSpacing = (-0.5).sp
                                    )
                                }

                                // Action Buttons
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Logout Button - More Prominent
                                    IconButton(
                                        onClick = onLogout,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(
                                                Color(0xFFEF4444).copy(alpha = 0.9f),
                                                CircleShape
                                            )
                                    ) {
                                        Icon(
                                            Icons.Default.ExitToApp,
                                            contentDescription = "Logout",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { refreshData() },
                                        enabled = !isRefreshing,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(
                                                Color.White.copy(alpha = 0.2f),
                                                CircleShape
                                            )
                                            .padding( top = 8.dp)
                                    ) {
                                        if (isRefreshing) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(22.dp),
                                                color = Color.White,
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Icon(
                                                Icons.Default.Refresh,
                                                contentDescription = "Refresh",
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Stats Cards Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Total Orders Card
                                StatCard(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Outlined.ShoppingCart,
                                    value = when (orderState) {
                                        is OrderState.OrdersLoaded -> (orderState as OrderState.OrdersLoaded).orders.size
                                        else -> 0
                                    }.toString(),
                                    label = "Total Orders",
                                    color = Color(0xFF10B981),
                                    scale = pulseScale
                                )

                                // Medicines Card
                                StatCard(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Outlined.Favorite,
                                    value = pharmacy.stock.size.toString(),
                                    label = "Medicines",
                                    color = Color(0xFF3B82F6),
                                    scale = pulseScale
                                )
                            }
                        }
                    }
                }

                // Quick Actions Section
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(top = 24.dp)
                    ) {
                        Text(
                            text = "Quick Actions",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Manage Stock Action
                            QuickActionCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.ShoppingCart,
                                title = "Manage Stock",
                                subtitle = "${pharmacy.stock.size} items",
                                gradient = AppColors.PrimaryGradient,
                                onClick = onNavigateToStock
                            )

                            // View Orders Action
                            QuickActionCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.List,
                                title = "View Orders",
                                subtitle = "Track & manage",
                                gradient = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF8B5CF6), Color(0xFF6366F1))
                                ),
                                onClick = onNavigateToOrders
                            )
                        }
                    }
                }

                // Pharmacy Info Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(top = 24.dp),
                        shape = AppShapes.ExtraLarge,
                        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        border = BorderStroke(1.dp, AppColors.BorderLight)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Animated Pharmacy Icon
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .scale(pulseScale)
                                        .background(
                                            AppColors.PrimaryGradient,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Outlined.Home,
                                        contentDescription = "Pharmacy",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = pharmacy.name,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.TextPrimary
                                    )
                                    Text(
                                        text = pharmacy.address,
                                        fontSize = 13.sp,
                                        color = AppColors.TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = pharmacy.phone,
                                        fontSize = 13.sp,
                                        color = AppColors.TextSecondary
                                    )
                                }

                                Icon(
                                    Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = AppColors.TextTertiary
                                )
                            }
                        }
                    }
                }

                // Stock Overview - Featured Section
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(top = 24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(Color(0xFF10B981), Color(0xFF059669))
                                            ),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Outlined.Favorite,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = "Stock Management",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AppColors.TextPrimary
                                )
                            }
                            Surface(
                                onClick = onNavigateToStock,
                                color = AppColors.Primary.copy(alpha = 0.1f),
                                shape = AppShapes.Medium
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Manage",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppColors.Primary
                                    )
                                    Icon(
                                        Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        tint = AppColors.Primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Featured Stock Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = AppShapes.ExtraLarge,
                            colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            border = BorderStroke(1.dp, AppColors.BorderLight)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                // Stock Stats Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // In Stock
                                    FeaturedStockStat(
                                        modifier = Modifier.weight(1f),
                                        title = "In Stock",
                                        count = pharmacy.stock.count { it.stock > 0 },
                                        total = pharmacy.stock.size,
                                        color = Color(0xFF10B981),
                                        icon = Icons.Outlined.CheckCircle
                                    )

                                    // Low Stock
                                    FeaturedStockStat(
                                        modifier = Modifier.weight(1f),
                                        title = "Low Stock",
                                        count = pharmacy.stock.count { it.stock > 0 && it.stock <= 10 },
                                        total = pharmacy.stock.size,
                                        color = Color(0xFFF59E0B),
                                        icon = Icons.Outlined.Warning
                                    )

                                    // Out of Stock
                                    FeaturedStockStat(
                                        modifier = Modifier.weight(1f),
                                        title = "Out of Stock",
                                        count = pharmacy.stock.count { it.stock == 0 },
                                        total = pharmacy.stock.size,
                                        color = Color(0xFFEF4444),
                                        icon = Icons.Outlined.Close
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Progress Bar
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Stock Health",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = AppColors.TextSecondary
                                        )
                                        Text(
                                            text = "${pharmacy.stock.count { it.stock > 0 }} / ${pharmacy.stock.size} items",
                                            fontSize = 12.sp,
                                            color = AppColors.TextTertiary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .background(
                                                AppColors.BorderLight.copy(alpha = 0.5f),
                                                AppShapes.Small
                                            )
                                    ) {
                                        val stockPercentage = if (pharmacy.stock.isNotEmpty()) {
                                            (pharmacy.stock.count { it.stock > 0 }.toFloat() / pharmacy.stock.size * 100).toInt()
                                        } else 0
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(stockPercentage / 100f)
                                                .fillMaxHeight()
                                                .background(
                                                    Brush.horizontalGradient(
                                                        colors = listOf(
                                                            Color(0xFF10B981),
                                                            Color(0xFF059669)
                                                        )
                                                    ),
                                                    AppShapes.Small
                                                )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stock Items List - Similar to Orders
                        if (pharmacy.stock.isEmpty()) {
                            EmptyStockCard()
                        } else {
                            // Show low stock items first, then regular items
                            val lowStockItems = pharmacy.stock.filter { it.stock > 0 && it.stock <= 10 }.take(3)
                            val regularItems = pharmacy.stock.filter { it.stock > 10 }.take(3)
                            val outOfStockItems = pharmacy.stock.filter { it.stock == 0 }.take(2)

                            // Low Stock Alert Section
                            if (lowStockItems.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Outlined.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Low Stock Alert",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF59E0B)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                lowStockItems.forEach { item ->
                                    EnhancedStockItemCard(
                                        item = item,
                                        onClick = onNavigateToStock
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }

                            // Out of Stock Section
                            if (outOfStockItems.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Outlined.Close,
                                        contentDescription = null,
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Out of Stock",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFEF4444)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                outOfStockItems.forEach { item ->
                                    EnhancedStockItemCard(
                                        item = item,
                                        onClick = onNavigateToStock
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }

                            // Regular Stock Section
                            if (regularItems.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Outlined.ShoppingCart,
                                        contentDescription = null,
                                        tint = AppColors.Primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "In Stock",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.Primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                regularItems.forEach { item ->
                                    EnhancedStockItemCard(
                                        item = item,
                                        onClick = onNavigateToStock
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }

                // My Orders - Featured Section
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(top = 24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(Color(0xFF8B5CF6), Color(0xFF6366F1))
                                            ),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Outlined.List,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = "My Orders",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AppColors.TextPrimary
                                )
                            }
                            Surface(
                                onClick = onNavigateToOrders,
                                color = AppColors.Primary.copy(alpha = 0.1f),
                                shape = AppShapes.Medium
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "View All",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppColors.Primary
                                    )
                                    Icon(
                                        Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        tint = AppColors.Primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        when (orderState) {
                            is OrderState.OrdersLoaded -> {
                                val orders = (orderState as OrderState.OrdersLoaded).orders.take(5)
                                if (orders.isEmpty()) {
                                    EmptyOrdersCard()
                                } else {
                                    orders.forEach { order ->
                                        EnhancedDashboardOrderCard(
                                            order = order,
                                            onClick = onNavigateToOrders
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                    }
                                }
                            }

                            is OrderState.Loading -> {
                                LoadingOrdersCard()
                            }

                            is OrderState.Error -> {
                                ErrorOrdersCard()
                            }

                            else -> {}
                        }
                    }
                }

                // Bottom Spacer
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    scale: Float
) {
    Card(
        modifier = modifier,
        shape = AppShapes.Large,
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun QuickActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    gradient: Brush,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable(onClick = onClick),
        shape = AppShapes.ExtraLarge,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient, AppShapes.ExtraLarge)
                .padding(20.dp)
        ) {
            Column {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun FeaturedStockStat(
    modifier: Modifier = Modifier,
    title: String,
    count: Int,
    total: Int,
    color: Color,
    icon: ImageVector
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = 0.2f),
                            color.copy(alpha = 0.05f)
                        )
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = count.toString(),
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Text(
            text = title,
            fontSize = 11.sp,
            color = AppColors.TextSecondary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun EnhancedDashboardOrderCard(
    order: Order,
    onClick: () -> Unit
) {
    val statusColor = when (order.status) {
        "pending" -> Color(0xFFF59E0B)
        "confirmed" -> Color(0xFF3B82F6)
        "preparing" -> Color(0xFF8B5CF6)
        "ready" -> Color(0xFF10B981)
        "delivered" -> Color(0xFF059669)
        "cancelled" -> Color(0xFFEF4444)
        else -> Color(0xFF6B7280)
    }

    val statusIcon = when (order.status) {
        "pending" -> Icons.Outlined.Info
        "confirmed" -> Icons.Outlined.CheckCircle
        "preparing" -> Icons.Outlined.Build
        "ready" -> Icons.Outlined.CheckCircle
        "delivered" -> Icons.Outlined.Check
        "cancelled" -> Icons.Outlined.Close
        else -> Icons.Outlined.Info
    }

    val statusGradient = when (order.status) {
        "pending" -> Brush.horizontalGradient(colors = listOf(Color(0xFFF59E0B), Color(0xFFD97706)))
        "confirmed" -> Brush.horizontalGradient(colors = listOf(Color(0xFF3B82F6), Color(0xFF2563EB)))
        "preparing" -> Brush.horizontalGradient(colors = listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED)))
        "ready" -> Brush.horizontalGradient(colors = listOf(Color(0xFF10B981), Color(0xFF059669)))
        "delivered" -> Brush.horizontalGradient(colors = listOf(Color(0xFF059669), Color(0xFF047857)))
        "cancelled" -> Brush.horizontalGradient(colors = listOf(Color(0xFFEF4444), Color(0xFFDC2626)))
        else -> Brush.horizontalGradient(colors = listOf(Color(0xFF6B7280), Color(0xFF4B5563)))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = AppShapes.ExtraLarge,
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, AppColors.BorderLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Icon with Gradient
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(statusGradient, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    statusIcon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Order Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Order #${order._id.takeLast(6).uppercase()}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                    Text(
                        text = "${String.format("%.2f", order.totalAmount)} DT",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppColors.Primary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.ShoppingCart,
                        contentDescription = null,
                        tint = AppColors.TextTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${order.items.size} item${if (order.items.size > 1) "s" else ""}",
                        fontSize = 13.sp,
                        color = AppColors.TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = AppShapes.Medium,
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(statusColor, CircleShape)
                        )
                        Text(
                            text = order.status.replaceFirstChar { it.uppercase() },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                Icons.Default.ArrowForward,
                contentDescription = null,
                tint = AppColors.TextTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun EmptyOrdersCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.Large,
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        border = BorderStroke(1.dp, AppColors.BorderLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(AppColors.PrimaryExtraPale, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.ShoppingCart,
                    contentDescription = null,
                    tint = AppColors.Primary,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Orders Yet",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
            Text(
                text = "Orders will appear here when customers place them",
                fontSize = 14.sp,
                color = AppColors.TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LoadingOrdersCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.Large,
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        border = BorderStroke(1.dp, AppColors.BorderLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = AppColors.Primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Loading orders...",
                fontSize = 14.sp,
                color = AppColors.TextSecondary
            )
        }
    }
}

@Composable
fun EnhancedStockItemCard(
    item: tn.rnu.isetr.miniprojet.data.PharmacyStock,
    onClick: () -> Unit
) {
    val stockStatus = when {
        item.stock == 0 -> "out_of_stock"
        item.stock <= 10 -> "low_stock"
        else -> "in_stock"
    }

    val statusColor = when (stockStatus) {
        "in_stock" -> Color(0xFF10B981)
        "low_stock" -> Color(0xFFF59E0B)
        "out_of_stock" -> Color(0xFFEF4444)
        else -> Color(0xFF6B7280)
    }

    val statusIcon = when (stockStatus) {
        "in_stock" -> Icons.Outlined.CheckCircle
        "low_stock" -> Icons.Outlined.Warning
        "out_of_stock" -> Icons.Outlined.Close
        else -> Icons.Outlined.Info
    }

    val statusGradient = when (stockStatus) {
        "in_stock" -> Brush.horizontalGradient(colors = listOf(Color(0xFF10B981), Color(0xFF059669)))
        "low_stock" -> Brush.horizontalGradient(colors = listOf(Color(0xFFF59E0B), Color(0xFFD97706)))
        "out_of_stock" -> Brush.horizontalGradient(colors = listOf(Color(0xFFEF4444), Color(0xFFDC2626)))
        else -> Brush.horizontalGradient(colors = listOf(Color(0xFF6B7280), Color(0xFF4B5563)))
    }

    val statusText = when (stockStatus) {
        "in_stock" -> "In Stock"
        "low_stock" -> "Low Stock"
        "out_of_stock" -> "Out of Stock"
        else -> "Unknown"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = AppShapes.ExtraLarge,
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, AppColors.BorderLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Icon with Gradient
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(statusGradient, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    statusIcon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Item Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.medicine.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${String.format("%.2f", item.price)} DT",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppColors.Primary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.ShoppingCart,
                        contentDescription = null,
                        tint = AppColors.TextTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Quantity: ${item.stock}",
                        fontSize = 13.sp,
                        color = AppColors.TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = AppShapes.Medium,
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(statusColor, CircleShape)
                        )
                        Text(
                            text = statusText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                Icons.Default.ArrowForward,
                contentDescription = null,
                tint = AppColors.TextTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun EmptyStockCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.Large,
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        border = BorderStroke(1.dp, AppColors.BorderLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFFD1FAE5), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.ShoppingCart,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Stock Items",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
            Text(
                text = "Add medicines to your stock to get started",
                fontSize = 14.sp,
                color = AppColors.TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ErrorOrdersCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.Large,
        colors = CardDefaults.cardColors(
            containerColor = AppColors.Error.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, AppColors.Error.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Warning,
                contentDescription = null,
                tint = AppColors.Error,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Failed to load orders",
                fontSize = 14.sp,
                color = AppColors.Error,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
