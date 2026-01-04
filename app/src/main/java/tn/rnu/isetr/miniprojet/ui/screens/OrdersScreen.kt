package tn.rnu.isetr.miniprojet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import tn.rnu.isetr.miniprojet.data.Order
import tn.rnu.isetr.miniprojet.viewmodel.OrderState
import tn.rnu.isetr.miniprojet.viewmodel.OrderViewModel

// --- Custom Color Palette ---
private object AppColors {
    val Background = Color(0xFFF8FAFC)
    val CardSurface = Color(0xFFFFFFFF)
    val PrimaryText = Color(0xFF0F172A)
    val SecondaryText = Color(0xFF64748B)
    val Border = Color(0xFFE2E8F0)
    val Success = Color(0xFF10B981)
    val Warning = Color(0xFFF59E0B)
    val Error = Color(0xFFEF4444)
    val Info = Color(0xFF3B82F6)
    val Purple = Color(0xFF8B5CF6)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    modifier: Modifier = Modifier,
    onOrderClick: (Order) -> Unit = {},
    viewModel: OrderViewModel = viewModel()
) {
    val orderState by viewModel.orderState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getUserOrders()
    }

    val orders = when (orderState) {
        is OrderState.OrdersLoaded -> (orderState as OrderState.OrdersLoaded).orders
        else -> emptyList()
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 20.dp)
            .background(AppColors.Background),
        topBar = {
            OrdersHeader(count = orders.size, onRefresh = { viewModel.getUserOrders() })
        },
        containerColor = AppColors.Background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(top = 32.dp)
        ) {
            when (orderState) {
                is OrderState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = AppColors.Info)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Loading your orders...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.SecondaryText
                        )
                    }
                }
                is OrderState.OrdersLoaded -> {
                    if (orders.isEmpty()) {
                        EmptyState()
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(orders, key = { it._id }) { order ->
                                OrderCard(order = order, onClick = { onOrderClick(order) })
                            }
                        }
                    }
                }
                is OrderState.Error -> {
                    val errorMessage = (orderState as OrderState.Error).message
                    ErrorState(message = errorMessage, onRetry = { viewModel.getUserOrders() })
                }
                else -> {}
            }
        }
    }
}

@Composable
fun OrdersHeader(count: Int, onRefresh: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.CardSurface)
            .padding(horizontal = 36.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "My Orders",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AppColors.PrimaryText
            )
            Text(
                text = if (count > 0) "$count orders placed" else "No orders yet",
                fontSize = 13.sp,
                color = AppColors.SecondaryText,
                fontWeight = FontWeight.Medium
            )
        }

        IconButton(
            onClick = onRefresh,
            modifier = Modifier
                .background(AppColors.Background, CircleShape)
                .size(40.dp)
        ) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = "Refresh",
                tint = AppColors.PrimaryText
            )
        }
    }
}

@Composable
fun OrderCard(order: Order, onClick: () -> Unit) {
    val statusInfo = getStatusStyle(order.status)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Icon/Avatar
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(statusInfo.containerColor.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getStatusIcon(order.status),
                    contentDescription = null,
                    tint = statusInfo.contentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Middle: Details - All info in one continuous column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Row 1: Order ID and Status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Order #${order._id.takeLast(6).uppercase()}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryText
                    )
                    // Status Chip
                    Surface(
                        color = statusInfo.containerColor,
                        shape = RoundedCornerShape(100.dp)
                    ) {
                        Text(
                            text = order.status.replaceFirstChar { it.uppercase() },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = statusInfo.contentColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                // Row 2: Items, Date, and Price - all in one continuous line
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Items
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = AppColors.SecondaryText,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${order.items.size} Items",
                            fontSize = 13.sp,
                            color = AppColors.SecondaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Date
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            tint = AppColors.SecondaryText,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = formatDate(order.createdAt),
                            fontSize = 13.sp,
                            color = AppColors.SecondaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Price - integrated into the same row
                    Text(
                        text = "• $${String.format("%.2f", order.totalAmount)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.PrimaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Right: Arrow indicator
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = "View",
                tint = AppColors.SecondaryText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(AppColors.Background, CircleShape)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.ShoppingCart,
                contentDescription = null,
                tint = AppColors.SecondaryText,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No Orders Yet",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.PrimaryText,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Looks like you haven't placed any orders.\nStart shopping to fill this list!",
            fontSize = 14.sp,
            color = AppColors.SecondaryText,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Warning,
            contentDescription = null,
            tint = AppColors.Error,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Oops!",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.PrimaryText
        )

        Text(
            text = message,
            fontSize = 14.sp,
            color = AppColors.SecondaryText,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.PrimaryText)
        ) {
            Text("Try Again")
        }
    }
}

// --- Helpers ---

data class StatusStyle(
    val containerColor: Color,
    val contentColor: Color
)

fun getStatusStyle(status: String): StatusStyle {
    return when (status.lowercase()) {
        "pending" -> StatusStyle(AppColors.Info.copy(alpha = 0.1f), AppColors.Info)
        "confirmed" -> StatusStyle(AppColors.Purple.copy(alpha = 0.1f), AppColors.Purple)
        "preparing" -> StatusStyle(AppColors.Warning.copy(alpha = 0.1f), AppColors.Warning)
        "ready", "delivered" -> StatusStyle(AppColors.Success.copy(alpha = 0.1f), AppColors.Success)
        "cancelled" -> StatusStyle(AppColors.Error.copy(alpha = 0.1f), AppColors.Error)
        else -> StatusStyle(AppColors.Border, AppColors.SecondaryText)
    }
}

fun getStatusIcon(status: String): ImageVector {
    return when (status.lowercase()) {
        "pending" -> Icons.Default.Info
        "confirmed" -> Icons.Default.Done
        "preparing" -> Icons.Default.Edit // Using Edit as a generic "work in progress" icon
        "ready" -> Icons.Default.Done
        "delivered" -> Icons.Default.Done
        "cancelled" -> Icons.Default.Close
        else -> Icons.Default.Info
    }
}

fun formatDate(dateString: String?): String {
    if (dateString == null) return "Unknown"
    return try {
        val date = dateString.substring(0, 10)
        val parts = date.split("-")
        if (parts.size == 3) {
            "${parts[1]}/${parts[2]}/${parts[0].takeLast(2)}"
        } else {
            date
        }
    } catch (e: Exception) {
        "Invalid Date"
    }
}