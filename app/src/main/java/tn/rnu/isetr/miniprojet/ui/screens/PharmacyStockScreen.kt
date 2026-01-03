package tn.rnu.isetr.miniprojet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import tn.rnu.isetr.miniprojet.data.Pharmacy
import tn.rnu.isetr.miniprojet.data.PreferencesManager
import tn.rnu.isetr.miniprojet.viewmodel.PharmacyViewModel
import tn.rnu.isetr.miniprojet.viewmodel.StockState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacyStockScreen(
    pharmacy: Pharmacy,
    preferencesManager: PreferencesManager,
    onNavigateBack: () -> Unit,
    viewModel: PharmacyViewModel = viewModel()
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedMedicine by remember { mutableStateOf<String?>(null) }
    var stockQuantity by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    val stockState by viewModel.stockState.collectAsState()

    LaunchedEffect(stockState) {
        when (stockState) {
            is StockState.Success -> {
                // Refresh pharmacy data or show success message
                showAddDialog = false
                stockQuantity = ""
                price = ""
            }
            else -> {}
        }
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
                    text = "Manage Stock",
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
                        text = pharmacy.stock.size.toString(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Medicine",
                        tint = Color(0xFF64748B)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(pharmacy.stock) { stockItem ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Medicine icon
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color(0xFFDBEAFE), RoundedCornerShape(16.dp))
                                .border(2.dp, Color(0xFFBFDBFE), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Rx",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E40AF)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Medicine info
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stockItem.medicine.name,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                letterSpacing = 0.2.sp
                            )

                            if (stockItem.medicine.genericName != null) {
                                Text(
                                    text = stockItem.medicine.genericName!!,
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (stockItem.stock > 0) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "${stockItem.stock} in stock",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (stockItem.stock > 0) Color(0xFF166534) else Color(0xFFDC2626)
                                    )
                                }
                            }

                            Text(
                                text = "$${String.format("%.2f", stockItem.price)}",
                                fontSize = 16.sp,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        // Update button
                        IconButton(
                            onClick = {
                                selectedMedicine = stockItem.medicine._id
                                stockQuantity = stockItem.stock.toString()
                                price = stockItem.price.toString()
                                showAddDialog = true
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF10B981), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Update Stock",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Add/Update Stock Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(if (selectedMedicine != null) "Update Stock" else "Add Medicine") },
            text = {
                Column {
                    OutlinedTextField(
                        value = stockQuantity,
                        onValueChange = { stockQuantity = it },
                        label = { Text("Stock Quantity") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Price (DT)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val quantity = stockQuantity.toIntOrNull() ?: 0
                        val itemPrice = price.toDoubleOrNull() ?: 0.0
                        if (selectedMedicine != null) {
                            viewModel.updateStock(pharmacy._id, selectedMedicine!!, quantity, itemPrice)
                        }
                    },
                    enabled = stockState !is StockState.Loading
                ) {
                    if (stockState is StockState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Text("Save")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}