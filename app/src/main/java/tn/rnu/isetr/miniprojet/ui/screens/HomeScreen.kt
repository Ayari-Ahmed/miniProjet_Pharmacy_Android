package tn.rnu.isetr.miniprojet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import tn.rnu.isetr.miniprojet.data.PreferencesManager
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
    pharmacyViewModel: PharmacyViewModel = viewModel(),
    orderViewModel: OrderViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val pharmacyState by pharmacyViewModel.pharmacyState.collectAsState()
    val orderState by orderViewModel.orderState.collectAsState()

    // Dynamic stats
    val pharmaciesCount = when (pharmacyState) {
        is PharmacyState.Success -> (pharmacyState as PharmacyState.Success).pharmacies.size
        else -> 0
    }

    val ordersCount = when (orderState) {
        is OrderState.OrdersLoaded -> (orderState as OrderState.OrdersLoaded).orders.size
        else -> 0
    }

    val stats = listOf(
        StatItem("Pharmacies", pharmaciesCount.toString(), Icons.Default.Place, Color(0xFF3B82F6)),
        StatItem("Orders", ordersCount.toString(), Icons.Default.ShoppingCart, Color(0xFF10B981)),
    )

    LaunchedEffect(Unit) {
        pharmacyViewModel.getPharmacies()
        orderViewModel.getUserOrders()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        // Glassmorphic Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color.White,
                    RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
                )
                .shadow(4.dp, RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                .padding(bottom = 40.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                // Header Top
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "Welcome back",
                            fontSize = 14.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "MediCare User", // TODO: Use actual user name
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A),
                            letterSpacing = (-0.5).sp
                        )
                        // Status Badge
                        Row(
                            modifier = Modifier
                                .background(Color(0xFFECFDF5), RoundedCornerShape(20.dp))
                                .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color(0xFF10B981), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Active",
                                fontSize = 11.sp,
                                color = Color(0xFF065F46),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Logout Button
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .background(Color(0xFFFEF2F2), RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(14.dp))
                    ) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color(0xFFEF4444)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Quick Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    stats.forEach { stat ->
                        StatCard(stat, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color.White,
                    RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
                )
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            // Section Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Quick Actions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    letterSpacing = 0.3.sp
                )
            }

            // Primary Action - Browse Pharmacies
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981))
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Glow effect
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            .align(Alignment.TopEnd)
                            .offset(x = 50.dp, y = (-50).dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(18.dp))
                                    .padding(16.dp)
                            ) {
                                Icon(
                                    Icons.Default.Place,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Browse Pharmacies",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Find medicines near you",
                                    fontSize = 13.sp,
                                    color = Color(0xFFD1FAE5),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                .size(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "→",
                                fontSize = 24.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Secondary Action - My Orders
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFDBEAFE))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFF8FAFC), RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Icon(
                            Icons.Default.KeyboardArrowUp,
                            contentDescription = null,
                            tint = Color(0xFF3B82F680),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "My Orders",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A),
                        letterSpacing = 0.3.sp
                    )

                    Text(
                        text = "Track your prescriptions",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                            .padding(vertical = 8.dp, horizontal = 12.dp)
                    ) {
                        Text(
                            text = "View Orders →",
                            fontSize = 13.sp,
                            color = Color(0xFF3B82F6),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Tip Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFECFDF5), RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .padding(8.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Tip: Use the bottom navigation to quickly access different sections",
                    fontSize = 13.sp,
                    color = Color(0xFF064E3B),
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

data class StatItem(
    val label: String,
    val value: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)

@Composable
fun StatCard(stat: StatItem, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .background(stat.color.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                    .padding(8.dp)
            ) {
                Icon(
                    stat.icon,
                    contentDescription = null,
                    tint = stat.color,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stat.value,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A)
            )

            Text(
                text = stat.label,
                fontSize = 11.sp,
                color = Color(0xFF64748B),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}