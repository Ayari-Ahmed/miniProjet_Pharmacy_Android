package tn.rnu.isetr.miniprojet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import tn.rnu.isetr.miniprojet.data.Customer
import tn.rnu.isetr.miniprojet.data.Order
import tn.rnu.isetr.miniprojet.data.Pharmacy
import tn.rnu.isetr.miniprojet.data.PreferencesManager
import tn.rnu.isetr.miniprojet.viewmodel.OrderViewModel
import tn.rnu.isetr.miniprojet.viewmodel.OrderState

@Composable
fun PharmacyOrdersScreen(
    pharmacy: Pharmacy,
    preferencesManager: PreferencesManager,
    onNavigateBack: () -> Unit,
    viewModel: OrderViewModel = viewModel()
) {
    val orderState by viewModel.orderState.collectAsState()
    var selectedFilter by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getPharmacyOrders(null)
    }

    // Filter and search orders
    val filteredOrders = when (orderState) {
        is OrderState.OrdersLoaded -> {
            val orders = (orderState as OrderState.OrdersLoaded).orders
            orders.filter { order ->
                val matchesFilter = when (selectedFilter) {
                    "pending" -> order.status == "pending"
                    "confirmed" -> order.status == "confirmed"
                    "processing" -> order.status == "processing"
                    "ready" -> order.status == "ready"
                    "delivering" -> order.status == "delivering"
                    "delivered" -> order.status == "delivered"
                    "cancelled" -> order.status == "cancelled"
                    else -> true
                }
                val matchesSearch = searchQuery.isBlank() ||
                    order._id.contains(searchQuery, ignoreCase = true) ||
                    (order.customer is Customer && (order.customer as Customer).name?.contains(searchQuery, ignoreCase = true) == true)
                matchesFilter && matchesSearch
            }
        }
        else -> emptyList()
    }

    // Calculate order statistics
    val orderStats = when (orderState) {
        is OrderState.OrdersLoaded -> {
            val orders = (orderState as OrderState.OrdersLoaded).orders
            mapOf(
                "all" to orders.size,
                "pending" to orders.count { it.status == "pending" },
                "confirmed" to orders.count { it.status == "confirmed" },
                "processing" to orders.count { it.status == "processing" },
                "ready" to orders.count { it.status == "ready" },
                "delivering" to orders.count { it.status == "delivering" },
                "delivered" to orders.count { it.status == "delivered" },
                "cancelled" to orders.count { it.status == "cancelled" }
            )
        }
        else -> mapOf("all" to 0, "pending" to 0, "confirmed" to 0, "processing" to 0, "ready" to 0, "delivering" to 0, "delivered" to 0, "cancelled" to 0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Compact Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF64748B)
                    )
                }
                Text(
                    text = "My Orders",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A),
                    letterSpacing = (-0.5).sp
                )
                Box(
                    modifier = Modifier
                        .background(Color(0xFF10B981), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = orderStats["all"].toString(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = {
                        isRefreshing = true
                        viewModel.getPharmacyOrders(null)
                        isRefreshing = false
                    },
                    modifier = Modifier
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFF64748B),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color(0xFF64748B)
                        )
                    }
                }
            }
        }

        // Search Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search orders by ID or customer name...") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF64748B)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = Color(0xFF64748B)
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF10B981),
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                )
            )
        }

        // Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .heightIn(max = 25.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == "all",
                onClick = { selectedFilter = "all" },
                label = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("All", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        if (orderStats["all"]!! > 0) {
                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.3f), CircleShape)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(orderStats["all"].toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF10B981),
                    selectedLabelColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            )
            FilterChip(
                selected = selectedFilter == "pending",
                onClick = { selectedFilter = "pending" },
                label = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Pending", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        if (orderStats["pending"]!! > 0) {
                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.3f), CircleShape)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(orderStats["pending"].toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF3B82F6),
                    selectedLabelColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            )
            FilterChip(
                selected = selectedFilter == "processing",
                onClick = { selectedFilter = "processing" },
                label = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Processing", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        if (orderStats["processing"]!! > 0) {
                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.3f), CircleShape)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(orderStats["processing"].toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFF59E0B),
                    selectedLabelColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            )
            FilterChip(
                selected = selectedFilter == "ready",
                onClick = { selectedFilter = "ready" },
                label = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Ready", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        if (orderStats["ready"]!! > 0) {
                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.3f), CircleShape)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(orderStats["ready"].toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF8B5CF6),
                    selectedLabelColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            )
            FilterChip(
                selected = selectedFilter == "delivering",
                onClick = { selectedFilter = "delivering" },
                label = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Delivering", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        if (orderStats["delivering"]!! > 0) {
                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.3f), CircleShape)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(orderStats["delivering"].toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFEC4899),
                    selectedLabelColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            )
        }

        // Orders List
        when (orderState) {
            is OrderState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF10B981))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading orders...",
                            fontSize = 14.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
            is OrderState.OrdersLoaded -> {
                if (filteredOrders.isEmpty()) {
                    if (searchQuery.isNotEmpty() || selectedFilter != "all") {
                        PharmacyEmptyState(
                            icon = Icons.Default.Search,
                            title = "No orders found",
                            subtitle = "Try adjusting your search or filter"
                        )
                    } else {
                        PharmacyEmptyState(
                            icon = Icons.Default.ShoppingCart,
                            title = "No orders found",
                            subtitle = "Orders will appear here when customers place them"
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredOrders) { order ->
                            EnhancedPharmacyOrderCard(order = order, onStatusUpdate = { newStatus ->
                                viewModel.updateOrderStatus(order._id, newStatus)
                            })
                        }
                    }
                }
            }
            is OrderState.Error -> {
                val errorMessage = (orderState as OrderState.Error).message
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(Color(0xFFFEE2E2), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Error Loading Orders",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = errorMessage,
                            fontSize = 14.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.getPharmacyOrders(null) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Retry")
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
fun PharmacyEmptyState(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color(0xFFF1F5F9), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color(0xFFCBD5E1),
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF334155),
            textAlign = TextAlign.Center,
            letterSpacing = 0.3.sp
        )

        Text(
            text = subtitle,
            fontSize = 14.sp,
            color = Color(0xFF94A3B8),
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun EnhancedPharmacyOrderCard(order: Order, onStatusUpdate: (String) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var showDetails by remember { mutableStateOf(false) }
    var showPrescriptionDialog by remember { mutableStateOf(false) }

    val statusColor = when (order.status) {
        "pending" -> Color(0xFF3B82F6)
        "confirmed" -> Color(0xFF10B981)
        "processing" -> Color(0xFFF59E0B)
        "ready" -> Color(0xFF8B5CF6)
        "delivering" -> Color(0xFFEC4899)
        "delivered" -> Color(0xFF10B981)
        "cancelled" -> Color(0xFFEF4444)
        else -> Color(0xFF6B7280)
    }

    val statusGradient = when (order.status) {
        "pending" -> Brush.linearGradient(colors = listOf(Color(0xFF3B82F6), Color(0xFF2563EB)))
        "confirmed" -> Brush.linearGradient(colors = listOf(Color(0xFF10B981), Color(0xFF059669)))
        "processing" -> Brush.linearGradient(colors = listOf(Color(0xFFF59E0B), Color(0xFFD97706)))
        "ready" -> Brush.linearGradient(colors = listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED)))
        "delivering" -> Brush.linearGradient(colors = listOf(Color(0xFFEC4899), Color(0xFFDB2777)))
        "delivered" -> Brush.linearGradient(colors = listOf(Color(0xFF10B981), Color(0xFF059669)))
        "cancelled" -> Brush.linearGradient(colors = listOf(Color(0xFFEF4444), Color(0xFFDC2626)))
        else -> Brush.linearGradient(colors = listOf(Color(0xFF6B7280), Color(0xFF4B5563)))
    }

    val statusIcon = when (order.status) {
        "pending" -> Icons.Default.Info
        "confirmed" -> Icons.Default.CheckCircle
        "processing" -> Icons.Default.Refresh
        "ready" -> Icons.Default.ShoppingCart
        "delivering" -> Icons.Default.Place
        "delivered" -> Icons.Default.Done
        "cancelled" -> Icons.Default.Close
        else -> Icons.Default.Info
    }

    val statusText = order.status.replaceFirstChar { it.uppercase() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDetails = !showDetails },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Main Card Content
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Icon with gradient
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(statusGradient)
                        .border(2.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        statusIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Order Info
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Order #${order._id.takeLast(8)}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            letterSpacing = 0.2.sp
                        )
                        Text(
                            text = "$${String.format("%.2f", order.totalAmount)}",
                            fontSize = 16.sp,
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = statusText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = statusColor
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${order.items.size} items",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF475569)
                            )
                        }
                    }

                    val customerDisplay = when (order.customer) {
                        is String -> (order.customer as String).takeLast(8)
                        is Customer -> (order.customer as Customer).name ?: "Unknown"
                        else -> "Unknown"
                    }
                    Text(
                        text = "Customer: $customerDisplay",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Action Buttons
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Status Update Button
                    Button(
                        onClick = { showDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .width(48.dp)
                            .height(36.dp),
                        border = BorderStroke(1.dp, Color(0xFF1E40AF))
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Update Status",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Expand/Collapse Button
                    IconButton(
                        onClick = { showDetails = !showDetails },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFF1F5F9), CircleShape)
                    ) {
                        Icon(
                            if (showDetails) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (showDetails) "Collapse" else "Expand",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Expandable Details Section
            if (showDetails) {
                Divider(
                    color = Color(0xFFF1F5F9),
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Prescription Section
                    if (!order.prescriptionUrl.isNullOrBlank()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFEFF6FF)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFF3B82F6),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Prescription Required",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E40AF)
                                    )
                                }

                                Text(
                                    text = "This order requires a prescription. Please verify the document before processing.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                Button(
                                    onClick = { showPrescriptionDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF3B82F6)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "View Prescription",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    // Check if any items require prescription
                    val prescriptionRequiredItems = order.items.filter { it.medicine.requiresPrescription }
                    if (prescriptionRequiredItems.isNotEmpty() && order.prescriptionUrl.isNullOrBlank()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFEF3C7)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFFCD34D))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Prescription Required",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E)
                                    )
                                }

                                Text(
                                    text = "The following items require a prescription:",
                                    fontSize = 12.sp,
                                    color = Color(0xFF78350F),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                prescriptionRequiredItems.forEach { item ->
                                    Text(
                                        text = "• ${item.medicine.name}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF92400E),
                                        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = "Order Items",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    order.items.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = item.medicine.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF0F172A)
                                    )
                                    if (item.medicine.requiresPrescription) {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = "Requires Prescription",
                                            tint = Color(0xFFF59E0B),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Qty: ${item.quantity}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Text(
                                text = "$${String.format("%.2f", item.price * item.quantity)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                        if (index < order.items.size - 1) {
                            Divider(
                                color = Color(0xFFF1F5F9),
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "$${String.format("%.2f", order.totalAmount)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF10B981)
                        )
                    }
                }
            }
        }
    }

    // Prescription Image Viewer Dialog
    if (showPrescriptionDialog) {
        AlertDialog(
            onDismissRequest = { showPrescriptionDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Prescription Document",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Actual image loading with Coil
                    SubcomposeAsyncImage(
                        model = order.prescriptionUrl,
                        contentDescription = "Prescription Image",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF1F5F9))
                    ) {
                        when (painter.state) {
                            is coil.compose.AsyncImagePainter.State.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = Color(0xFF3B82F6),
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Loading prescription...",
                                            fontSize = 14.sp,
                                            color = Color(0xFF64748B),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                            is coil.compose.AsyncImagePainter.State.Error -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFFFEE2E2), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(64.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Failed to load image",
                                            fontSize = 14.sp,
                                            color = Color(0xFFDC2626),
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = order.prescriptionUrl?.takeLast(30) ?: "No URL",
                                            fontSize = 11.sp,
                                            color = Color(0xFFEF4444),
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }
                            else -> {
                                SubcomposeAsyncImageContent()
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                // TODO: Implement download functionality
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                // TODO: Implement open in browser functionality
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3B82F6)
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                Icons.Default.ExitToApp,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Open", fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrescriptionDialog = false }) {
                    Text("Close", color = Color(0xFF6B7280))
                }
            }
        )
    }

    // Status Update Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Update Order Status",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Order #${order._id.takeLast(8)}",
                        fontSize = 14.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    val statuses = listOf(
                        "pending" to "Pending - Awaiting confirmation",
                        "confirmed" to "Confirmed - Order accepted",
                        "processing" to "Processing - Preparing order",
                        "ready" to "Ready - Ready for pickup/delivery",
                        "delivering" to "Delivering - On the way",
                        "delivered" to "Delivered - Order completed",
                        "cancelled" to "Cancelled - Order cancelled"
                    )

                    statuses.forEach { (status, description) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onStatusUpdate(status)
                                    showDialog = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = order.status == status,
                                onClick = {
                                    onStatusUpdate(status)
                                    showDialog = false
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = when (status) {
                                        "pending" -> Color(0xFF3B82F6)
                                        "confirmed" -> Color(0xFF10B981)
                                        "processing" -> Color(0xFFF59E0B)
                                        "ready" -> Color(0xFF8B5CF6)
                                        "delivering" -> Color(0xFFEC4899)
                                        "delivered" -> Color(0xFF10B981)
                                        "cancelled" -> Color(0xFFEF4444)
                                        else -> Color(0xFF6B7280)
                                    }
                                )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = description.split(" - ")[0],
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = description.split(" - ").getOrNull(1) ?: "",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            }
        )
    }
}