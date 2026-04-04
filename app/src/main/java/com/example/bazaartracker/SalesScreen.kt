package com.example.bazaartracker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SalesScreen(
    viewModel: SalesViewModel,
    productViewModel: ProductViewModel,
    vendorViewModel: VendorViewModel
) {
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    var isAddingSale by remember { mutableStateOf(false) }
    var saleToDelete by remember { mutableStateOf<Sale?>(null) }

    if (isAddingSale) {
        NewSaleScreen(
            productViewModel = productViewModel,
            vendorViewModel = vendorViewModel,
            onDismiss = { isAddingSale = false },
            onSave = { request ->
                viewModel.addSale(request) {
                    isAddingSale = false
                }
            }
        )
    } else {
        Scaffold(
            containerColor = Color(0xFF121212),
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { isAddingSale = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Sale")
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                if (isLoading && sales.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (sales.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                        Text("No Sales Recorded", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text("Sales History", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        items(sales) { sale ->
                            SaleCard(sale, onDelete = { saleToDelete = sale })
                        }
                    }
                }
            }
        }
    }

    saleToDelete?.let { sale ->
        AlertDialog(
            onDismissRequest = { saleToDelete = null },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("Delete Sale", color = Color.White) },
            text = { Text("Are you sure you want to delete this sale record?", color = Color.Gray) },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteSale(sale.id)
                    saleToDelete = null
                }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { saleToDelete = null }) { Text("Cancel", color = Color.Gray) }
            }
        )
    }
}

@Composable
fun SaleCard(sale: Sale, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(sale.saleType.name, color = if(sale.saleType == SaleType.CASH) Color.Green else Color.Yellow, fontWeight = FontWeight.Bold)
                    Text(sale.date ?: "", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Text("₹${sale.totalAmount}", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
            }
            if (sale.vendorName != null) {
                Text("Vendor: ${sale.vendorName}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.6f))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSaleScreen(
    productViewModel: ProductViewModel,
    vendorViewModel: VendorViewModel,
    onDismiss: () -> Unit,
    onSave: (CreateSaleRequest) -> Unit
) {
    val products by productViewModel.products.collectAsStateWithLifecycle()
    val vendors by vendorViewModel.vendors.collectAsStateWithLifecycle()
    
    var saleType by remember { mutableStateOf(SaleType.CASH) }
    var selectedVendor by remember { mutableStateOf<Vendor?>(null) }
    val cart = remember { mutableStateListOf<Pair<Product, Int>>() }
    
    var showProductPicker by remember { mutableStateOf(false) }
    var vendorExpanded by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFF121212),
        topBar = {
            TopAppBar(
                title = { Text("New Sale", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null, tint = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E)),
                actions = {
                    TextButton(
                        onClick = {
                            if (cart.isNotEmpty() && (saleType == SaleType.CASH || selectedVendor != null)) {
                                val items = cart.map { CreateSaleItemRequest(it.first.id, it.second) }
                                onSave(CreateSaleRequest(selectedVendor?.id, saleType, items))
                            }
                        },
                        enabled = cart.isNotEmpty() && (saleType == SaleType.CASH || selectedVendor != null)
                    ) {
                        Text("SAVE", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState())) {
            // Sale Type Toggle
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Sale Type:", color = Color.White, modifier = Modifier.weight(1f))
                FilterChip(
                    selected = saleType == SaleType.CASH,
                    onClick = { saleType = SaleType.CASH },
                    label = { Text("CASH") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = saleType == SaleType.CREDIT,
                    onClick = { saleType = SaleType.CREDIT },
                    label = { Text("CREDIT") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }

            if (saleType == SaleType.CREDIT) {
                ExposedDropdownMenuBox(
                    expanded = vendorExpanded,
                    onExpandedChange = { vendorExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedVendor?.name ?: "Select Vendor",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Vendor *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vendorExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = vendorExpanded,
                        onDismissRequest = { vendorExpanded = false },
                        modifier = Modifier.background(Color(0xFF1E1E1E))
                    ) {
                        vendors.forEach { vendor ->
                            DropdownMenuItem(
                                text = { Text(vendor.name, color = Color.White) },
                                onClick = {
                                    selectedVendor = vendor
                                    vendorExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Cart Items", style = MaterialTheme.typography.titleMedium, color = Color.White, modifier = Modifier.weight(1f))
                Button(onClick = { showProductPicker = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Add Item")
                }
            }

            cart.forEachIndexed { index, pair ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(pair.first.name, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("₹${pair.first.price} x ${pair.second}", color = Color.Gray)
                    }
                    IconButton(onClick = { cart.removeAt(index) }) {
                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, tint = Color.Red)
                    }
                }
            }
            
            if (cart.isNotEmpty()) {
                val total = cart.sumOf { it.first.price * it.second }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.DarkGray)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Amount", fontWeight = FontWeight.Bold, color = Color.White)
                    Text("₹$total", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }

    if (showProductPicker) {
        var quantity by remember { mutableStateOf("1") }
        var selectedProduct by remember { mutableStateOf<Product?>(null) }
        var productExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showProductPicker = false },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("Add Product to Cart", color = Color.White) },
            text = {
                Column {
                    ExposedDropdownMenuBox(
                        expanded = productExpanded,
                        onExpandedChange = { productExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedProduct?.name ?: "Select Product",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Product") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = productExpanded,
                            onDismissRequest = { productExpanded = false },
                            modifier = Modifier.background(Color(0xFF1E1E1E))
                        ) {
                            products.forEach { product ->
                                DropdownMenuItem(
                                    text = { Text("${product.name} (₹${product.price})", color = Color.White) },
                                    onClick = {
                                        selectedProduct = product
                                        productExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { if(it.all { char -> char.isDigit() }) quantity = it },
                        label = { Text("Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val q = quantity.toIntOrNull() ?: 1
                    selectedProduct?.let { cart.add(it to q) }
                    showProductPicker = false
                }, enabled = selectedProduct != null && quantity.isNotEmpty()) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProductPicker = false }) { Text("Cancel", color = Color.Gray) }
            }
        )
    }
}
