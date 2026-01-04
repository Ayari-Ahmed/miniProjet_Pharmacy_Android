package tn.rnu.isetr.miniprojet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import tn.rnu.isetr.miniprojet.data.Medicine
import tn.rnu.isetr.miniprojet.data.Pharmacy
import tn.rnu.isetr.miniprojet.data.PharmacyStock
import tn.rnu.isetr.miniprojet.data.PreferencesManager
import tn.rnu.isetr.miniprojet.viewmodel.AuthState
import tn.rnu.isetr.miniprojet.viewmodel.MedicinesState
import tn.rnu.isetr.miniprojet.viewmodel.PharmacyAuthViewModel
import tn.rnu.isetr.miniprojet.viewmodel.PharmacyViewModel
import tn.rnu.isetr.miniprojet.viewmodel.StockState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacyStockScreen(
    initialPharmacy: Pharmacy,
    preferencesManager: PreferencesManager,
    onNavigateBack: () -> Unit,
    onPharmacyUpdated: (Pharmacy) -> Unit = {},
    viewModel: PharmacyViewModel = viewModel(),
    authViewModel: PharmacyAuthViewModel = viewModel()
) {
    var pharmacy by remember { mutableStateOf(initialPharmacy) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingStockItem by remember { mutableStateOf<PharmacyStock?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }

    val stockState by viewModel.stockState.collectAsState()
    val medicines by viewModel.medicines.collectAsState()
    val authState by authViewModel.authState.collectAsState()

    // Function to refresh pharmacy data
    fun refreshPharmacyData() {
        isRefreshing = true
        viewModel.getPharmacy(pharmacy._id) { updatedPharmacy ->
            updatedPharmacy?.let {
                pharmacy = it
                onPharmacyUpdated(it)
                preferencesManager.savePharmacy(it)
            }
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) {
        viewModel.getMedicines()
        // Refresh pharmacy data on screen entry
        refreshPharmacyData()
    }

    LaunchedEffect(stockState) {
        when (stockState) {
            is StockState.Success -> {
                // Create updated pharmacy object with new stock data
                val updatedStock = (stockState as StockState.Success).stock
                val updatedPharmacy = pharmacy.copy(stock = updatedStock)
                pharmacy = updatedPharmacy
                onPharmacyUpdated(updatedPharmacy)
                preferencesManager.savePharmacy(updatedPharmacy) // Save to preferences
                // Refresh medicines list to update available options
                viewModel.getMedicines()
                showAddDialog = false
                editingStockItem = null
            }
            else -> {}
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Stock Management",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "${pharmacy.stock.size} medicines in stock",
                            fontSize = 14.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF64748B)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { refreshPharmacyData() },
                        enabled = !isRefreshing
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
                    FilledTonalButton(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF10B981)
                        )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Medicine",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Medicine")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1E293B)
                )
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            if (pharmacy.stock.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color(0xFFCBD5E1)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No medicines in stock",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add your first medicine to get started",
                            fontSize = 14.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(pharmacy.stock.sortedBy { it.medicine.name }) { stockItem ->
                    StockItemCard(
                        stockItem = stockItem,
                        onEdit = { editingStockItem = stockItem },
                        onDelete = {
                            viewModel.updateStock(pharmacy._id, stockItem.medicine._id, 0)
                        },
                        stockState = stockState
                    )
                }
            }
        }
    }

    // Add Medicine Dialog
    if (showAddDialog) {
        AddMedicineDialog(
            pharmacy = pharmacy,
            medicines = medicines,
            stockState = stockState,
            onDismiss = { showAddDialog = false },
            onSave = { medicine, quantity, price ->
                viewModel.updateStock(pharmacy._id, medicine._id, quantity, price)
            }
        )
    }

    // Edit Stock Dialog
    editingStockItem?.let { stockItem ->
        EditStockDialog(
            stockItem = stockItem,
            stockState = stockState,
            onDismiss = { editingStockItem = null },
            onSave = { quantity, price ->
                viewModel.updateStock(pharmacy._id, stockItem.medicine._id, quantity, price)
            }
        )
    }
}

@Composable
private fun StockItemCard(
    stockItem: PharmacyStock,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    stockState: StockState
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Medicine Icon with gradient background
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "💊",
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Medicine Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stockItem.medicine.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    letterSpacing = 0.3.sp
                )

                stockItem.medicine.genericName?.let {
                    Text(
                        text = it,
                        fontSize = 14.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Stock Status and Price
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Stock Badge
                    Surface(
                        color = when {
                            stockItem.stock == 0 -> Color(0xFFFEE2E2)
                            stockItem.stock < 10 -> Color(0xFFFEF3C7)
                            else -> Color(0xFFDCFCE7)
                        },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "${stockItem.stock} in stock",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = when {
                                stockItem.stock == 0 -> Color(0xFFDC2626)
                                stockItem.stock < 10 -> Color(0xFFD97706)
                                else -> Color(0xFF166534)
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    // Price
                    Surface(
                        color = Color(0xFFE6F7F0),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "${String.format("%.2f", stockItem.price)} DT",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF059669),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Action Buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.width(48.dp)
            ) {
                // Edit Button
                FilledTonalIconButton(
                    onClick = onEdit,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color(0xFFF1F5F9)
                    ),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Stock",
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Delete Button
                FilledTonalIconButton(
                    onClick = onDelete,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color(0xFFFEE2E2)
                    ),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete from Stock",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMedicineDialog(
    pharmacy: Pharmacy,
    medicines: MedicinesState,
    stockState: StockState,
    onDismiss: () -> Unit,
    onSave: (Medicine, Int, Double) -> Unit
) {
    var selectedMedicine by remember { mutableStateOf<Medicine?>(null) }
    var quantity by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf<String?>(null) }

    // Get current stock medicine IDs to filter out already stocked medicines
    val currentStockMedicineIds = remember(pharmacy) {
        pharmacy.stock.map { it.medicine._id }.toSet()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Add Medicine to Stock",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Medicine Selection
                Text(
                    text = "Select Medicine",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF374151)
                )

                when (medicines) {
                    is MedicinesState.Success -> {
                        val availableMedicines = medicines.medicines.filter { medicine ->
                            medicine._id !in currentStockMedicineIds
                        }

                        if (availableMedicines.isEmpty()) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFF3F4F6)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFF6B7280),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "All medicines are already in your stock",
                                        fontSize = 14.sp,
                                        color = Color(0xFF6B7280)
                                    )
                                }
                            }
                        } else {
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedMedicine?.name ?: "Choose a medicine",
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                    },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF10B981),
                                        unfocusedBorderColor = Color(0xFFE5E7EB)
                                    ),
                                    isError = selectedMedicine == null && showError != null
                                )

                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    availableMedicines.forEach { medicine ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(
                                                        text = medicine.name,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    medicine.genericName?.let {
                                                        Text(
                                                            text = it,
                                                            fontSize = 12.sp,
                                                            color = Color(0xFF6B7280)
                                                        )
                                                    }
                                                }
                                            },
                                            onClick = {
                                                selectedMedicine = medicine
                                                expanded = false
                                                showError = null
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    is MedicinesState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                    is MedicinesState.Error -> {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFEE2E2)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Failed to load medicines. Please try again.",
                                    fontSize = 14.sp,
                                    color = Color(0xFFDC2626)
                                )
                            }
                        }
                    }
                    else -> {}
                }

                // Show error message
                showError?.let {
                    Text(
                        text = it,
                        color = Color(0xFFDC2626),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Quantity Input
                OutlinedTextField(
                    value = quantity,
                    onValueChange = {
                        quantity = it.filter { char -> char.isDigit() }
                        showError = null
                    },
                    label = { Text("Quantity") },
                    placeholder = { Text("Enter quantity (e.g., 50)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    ),
                    isError = quantity.isBlank() && showError != null
                )

                // Price Input
                OutlinedTextField(
                    value = price,
                    onValueChange = {
                        price = it
                        showError = null
                    },
                    label = { Text("Price (DT)") },
                    placeholder = { Text("Enter price (e.g., 5.50)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    ),
                    isError = price.isBlank() && showError != null
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        selectedMedicine == null -> {
                            showError = "Please select a medicine"
                        }
                        quantity.isBlank() -> {
                            showError = "Please enter a quantity"
                        }
                        price.isBlank() -> {
                            showError = "Please enter a price"
                        }
                        else -> {
                            val qty = quantity.toIntOrNull() ?: 0
                            val prc = price.toDoubleOrNull() ?: 0.0

                            if (qty <= 0) {
                                showError = "Quantity must be greater than 0"
                            } else if (prc <= 0) {
                                showError = "Price must be greater than 0"
                            } else {
                                selectedMedicine?.let { medicine ->
                                    onSave(medicine, qty, prc)
                                }
                            }
                        }
                    }
                },
                enabled = stockState !is StockState.Loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981)
                )
            ) {
                if (stockState is StockState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White
                    )
                } else {
                    Text("Add to Stock")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF6B7280))
            }
        }
    )
}

@Composable
private fun EditStockDialog(
    stockItem: PharmacyStock,
    stockState: StockState,
    onDismiss: () -> Unit,
    onSave: (Int, Double) -> Unit
) {
    var quantity by remember { mutableStateOf(stockItem.stock.toString()) }
    var price by remember { mutableStateOf(stockItem.price.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Update Stock",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Medicine Info
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF8FAFC)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF3B82F6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💊", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stockItem.medicine.name,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            stockItem.medicine.genericName?.let {
                                Text(
                                    text = it,
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }

                // Quantity Input
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it.filter { char -> char.isDigit() } },
                    label = { Text("Quantity") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )

                // Price Input
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price (DT)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantity.toIntOrNull() ?: 0
                    val prc = price.toDoubleOrNull() ?: 0.0
                    onSave(qty, prc)
                },
                enabled = quantity.isNotBlank() && price.isNotBlank() && stockState !is StockState.Loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3B82F6)
                )
            ) {
                if (stockState is StockState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White
                    )
                } else {
                    Text("Update Stock")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF6B7280))
            }
        }
    )
}
