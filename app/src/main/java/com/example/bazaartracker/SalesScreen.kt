package com.example.bazaartracker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bazaartracker.ui.theme.*

@Composable
fun SalesScreen(
    viewModel: SalesViewModel,
    productViewModel: ProductViewModel,
    vendorViewModel: VendorViewModel
) {
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val saleDetails by viewModel.saleDetails.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    
    val products by productViewModel.products.collectAsStateWithLifecycle()
    val vendors by vendorViewModel.vendors.collectAsStateWithLifecycle()
    
    var isAddingSale by remember { mutableStateOf(false) }
    var saleToEdit by remember { mutableStateOf<Sale?>(null) }
    var saleToDelete by remember { mutableStateOf<Sale?>(null) }
    var showDetails by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.resetState()
        }
    }

    if (isAddingSale || saleToEdit != null) {
        NewSaleScreen(
            productViewModel = productViewModel,
            vendorViewModel = vendorViewModel,
            initialSale = saleToEdit,
            onDismiss = { 
                isAddingSale = false
                saleToEdit = null
            },
            onSave = { request ->
                if (saleToEdit != null) {
                    viewModel.updateSale(saleToEdit!!.id, request) {
                        saleToEdit = null
                    }
                } else {
                    viewModel.addSale(request) {
                        isAddingSale = false
                    }
                }
            }
        )
    } else {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.background,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { isAddingSale = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Sale")
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                if (isLoading && sales.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
                } else if (sales.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        Text("No Sales Recorded", color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                "Sales History", 
                                style = MaterialTheme.typography.headlineMedium, 
                                color = MaterialTheme.colorScheme.onBackground, 
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        items(sales) { sale ->
                            SaleCard(
                                sale = sale, 
                                vendors = vendors,
                                onClick = {
                                    viewModel.fetchSaleDetails(sale.id)
                                    showDetails = true
                                },
                                onEdit = { saleToEdit = sale },
                                onDelete = { saleToDelete = sale }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDetails && saleDetails != null) {
        SaleDetailDialog(
            sale = saleDetails!!,
            products = products,
            vendors = vendors,
            onDismiss = { 
                showDetails = false
                viewModel.clearSaleDetails()
            }
        )
    }

    saleToDelete?.let { sale ->
        AlertDialog(
            onDismissRequest = { saleToDelete = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Delete Sale", color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("Are you sure you want to delete this sale record?", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteSale(sale.id)
                    saleToDelete = null
                }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { saleToDelete = null }) { Text("Cancel", color = MaterialTheme.colorScheme.outline) }
            }
        )
    }
}

@Composable
fun SaleCard(sale: Sale, vendors: List<Vendor>, onClick: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val displayVendorName = sale.vendorName ?: vendors.find { it.id == sale.vendorId }?.name
    
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Surface(
                        color = (if(sale.saleType == SaleType.CASH) Secondary else Tertiary).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            sale.saleType.name, 
                            color = if(sale.saleType == SaleType.CASH) Secondary else Tertiary, 
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(sale.date ?: "N/A", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("₹${sale.totalAmount}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            if (displayVendorName != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, size = 14.dp, tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(displayVendorName, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun SaleDetailDialog(sale: Sale, products: List<Product>, vendors: List<Vendor>, onDismiss: () -> Unit) {
    val displayVendorName = sale.vendorName ?: vendors.find { it.id == sale.vendorId }?.name
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Sale Details", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                DetailRow("Type", sale.saleType.name, if(sale.saleType == SaleType.CASH) Secondary else Tertiary)
                DetailRow("Date", sale.date ?: "N/A", MaterialTheme.colorScheme.onSurface)
                DetailRow("Total", "₹${sale.totalAmount}", MaterialTheme.colorScheme.primary)
                if (displayVendorName != null) {
                    DetailRow("Vendor", displayVendorName, MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text("Items", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                if (sale.items.isNullOrEmpty()) {
                    Text("No item details available", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                } else {
                    sale.items.forEach { item ->
                        val productName = item.productName ?: products.find { it.id == item.productId }?.name ?: "Unknown Product"
                        val price = item.price ?: products.find { it.id == item.productId }?.price ?: 0.0
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp)).padding(8.dp), 
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(productName, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            Text("${item.quantity} x ₹$price", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) { Text("Close") }
        }
    )
}

@Composable
fun DetailRow(label: String, value: String, valueColor: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Text(value, color = valueColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSaleScreen(
    productViewModel: ProductViewModel,
    vendorViewModel: VendorViewModel,
    initialSale: Sale? = null,
    onDismiss: () -> Unit,
    onSave: (CreateSaleRequest) -> Unit
) {
    val products by productViewModel.products.collectAsStateWithLifecycle()
    val vendors by vendorViewModel.vendors.collectAsStateWithLifecycle()
    
    var saleType by remember { mutableStateOf(initialSale?.saleType ?: SaleType.CASH) }
    var selectedVendor by remember { mutableStateOf<Vendor?>(null) }
    
    val cart = remember { mutableStateListOf<Pair<Product, Int>>() }
    
    LaunchedEffect(products, vendors, initialSale) {
        if (initialSale != null) {
            if (selectedVendor == null && initialSale.vendorId != null) {
                selectedVendor = vendors.find { it.id == initialSale.vendorId }
            }
            if (cart.isEmpty()) {
                initialSale.items?.forEach { item ->
                    val product = products.find { it.id == item.productId }
                    if (product != null) {
                        cart.add(product to item.quantity)
                    }
                }
            }
        }
    }
    
    var showProductPicker by remember { mutableStateOf(false) }
    var vendorExpanded by remember { mutableStateOf(false) }

    val isSaveEnabled = cart.isNotEmpty() && (saleType == SaleType.CASH || selectedVendor != null)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (initialSale != null) "Edit Sale" else "New Sale", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    Button(
                        onClick = {
                            if (isSaveEnabled) {
                                val items = cart.map { CreateSaleItemRequest(it.first.id, it.second) }
                                onSave(CreateSaleRequest(selectedVendor?.id, saleType, items))
                            }
                        },
                        enabled = isSaveEnabled,
                        modifier = Modifier.padding(end = 8.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("SAVE")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState())) {
            Text("Select Sale Type", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                FilterChip(
                    selected = saleType == SaleType.CASH,
                    onClick = { saleType = SaleType.CASH },
                    label = { Text("CASH") },
                    leadingIcon = if (saleType == SaleType.CASH) { { Icon(Icons.Default.Check, contentDescription = null, size = 18.dp) } } else null,
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                FilterChip(
                    selected = saleType == SaleType.CREDIT,
                    onClick = { saleType = SaleType.CREDIT },
                    label = { Text("CREDIT") },
                    leadingIcon = if (saleType == SaleType.CREDIT) { { Icon(Icons.Default.Check, contentDescription = null, size = 18.dp) } } else null,
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            ExposedDropdownMenuBox(
                expanded = vendorExpanded,
                onExpandedChange = { vendorExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedVendor?.name ?: "Select Vendor",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(if (saleType == SaleType.CREDIT) "Vendor *" else "Vendor (Optional)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vendorExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
                ExposedDropdownMenu(
                    expanded = vendorExpanded,
                    onDismissRequest = { vendorExpanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    vendors.forEach { vendor ->
                        DropdownMenuItem(
                            text = { Text(vendor.name) },
                            onClick = {
                                selectedVendor = vendor
                                vendorExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Cart Items", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
                FilledTonalButton(
                    onClick = { showProductPicker = true },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, size = 18.dp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Item")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            if (cart.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "No items in cart", 
                        color = MaterialTheme.colorScheme.outline, 
                        modifier = Modifier.padding(24.dp).align(Alignment.CenterHorizontally),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                cart.forEachIndexed { index, pair ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(pair.first.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("₹${pair.first.price} x ${pair.second}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("₹${pair.first.price * pair.second}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { cart.removeAt(index) }) {
                                Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
                
                val total = cart.sumOf { it.first.price * it.second }
                Spacer(modifier = Modifier.height(24.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp), 
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Amount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("₹$total", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }

    if (showProductPicker) {
        var quantity by remember { mutableStateOf("1") }
        var selectedProduct by remember { mutableStateOf<Product?>(null) }
        var productExpanded by remember { mutableStateOf(false) }
        var quantityError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showProductPicker = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Add Product", style = MaterialTheme.typography.headlineSmall) },
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
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = productExpanded,
                            onDismissRequest = { productExpanded = false }
                        ) {
                            products.forEach { product ->
                                DropdownMenuItem(
                                    text = { Text("${product.name} (Stock: ${product.stock})") },
                                    onClick = {
                                        selectedProduct = product
                                        productExpanded = false
                                        quantityError = null
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { 
                            if(it.all { char -> char.isDigit() }) {
                                quantity = it
                                val q = it.toIntOrNull() ?: 0
                                if (selectedProduct != null && q > selectedProduct!!.stock) {
                                    quantityError = "Only ${selectedProduct!!.stock} available"
                                } else {
                                    quantityError = null
                                }
                            }
                        },
                        label = { Text("Quantity") },
                        isError = quantityError != null,
                        supportingText = { quantityError?.let { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val q = quantity.toIntOrNull() ?: 1
                        selectedProduct?.let { cart.add(it to q) }
                        showProductPicker = false
                    }, 
                    enabled = selectedProduct != null && quantity.isNotEmpty() && quantityError == null && (quantity.toIntOrNull() ?: 0) > 0,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Add to Cart")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProductPicker = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun Icon(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String?,
    size: androidx.compose.ui.unit.Dp,
    tint: Color = LocalContentColor.current
) {
    androidx.compose.material3.Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = Modifier.size(size),
        tint = tint
    )
}
