package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.CartItem
import com.example.data.PriceTier
import com.example.data.model.CustomerEntity
import com.example.data.model.ProductEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.NavyBlueDark
import com.example.ui.theme.NavyBluePrimary
import com.example.ui.theme.SafetyOrange
import com.example.ui.theme.StockLow
import com.example.ui.theme.StockOk
import com.example.ui.theme.StockWarning
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val products by viewModel.products.collectAsState()
    val lines by viewModel.lines.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val selectedCustomer by viewModel.selectedCustomer.collectAsState()
    val searchQuery by viewModel.posSearchQuery.collectAsState()
    val selectedLine by viewModel.posSelectedLine.collectAsState()
    val showCheckoutDialog by viewModel.showCheckoutDialog.collectAsState()

    var showCartModal by remember { mutableStateOf(false) }

    // Filter products
    val filteredProducts = remember(products, searchQuery, selectedLine) {
        products.filter { p ->
            val matchesQuery = searchQuery.isBlank() ||
                p.description.contains(searchQuery, ignoreCase = true) ||
                p.code.contains(searchQuery, ignoreCase = true) ||
                p.barcode.contains(searchQuery, ignoreCase = true) ||
                p.brand.contains(searchQuery, ignoreCase = true)

            val matchesLine = selectedLine == null || p.line.equals(selectedLine, ignoreCase = true)
            matchesQuery && matchesLine
        }
    }

    val cartTotal = cart.sumOf { it.lineTotal }
    val cartItemsCount = cart.sumOf { it.quantity }.toInt()

    Box(modifier = modifier.fillMaxSize().background(Color(0xFFF1F5F9))) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Customer Selector & Search Bar Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    // Customer Dropdown Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        var customerExpanded by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = customerExpanded,
                            onExpandedChange = { customerExpanded = !customerExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedCustomer?.name ?: "Seleccione cliente",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Cliente para Facturación / Venta", fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Cliente",
                                        tint = NavyBluePrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                                    .testTag("customer_dropdown"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NavyBluePrimary,
                                    unfocusedBorderColor = Color(0xFFCBD5E1)
                                )
                            )

                            ExposedDropdownMenu(
                                expanded = customerExpanded,
                                onDismissRequest = { customerExpanded = false }
                            ) {
                                customers.forEach { customer ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(customer.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                if (customer.isCreditAllowed) {
                                                    Text(
                                                        "Crédito disp: $${String.format(Locale.US, "%.2f", customer.creditLimit - customer.currentBalance)}",
                                                        fontSize = 11.sp,
                                                        color = SafetyOrange
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            viewModel.setSelectedCustomer(customer)
                                            customerExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Search by code, barcode, or name
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setPosSearchQuery(it) },
                            placeholder = { Text("Buscar artículo (código, barras, nombre, marca)...", fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Buscar", tint = Color.Gray)
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setPosSearchQuery("") }) {
                                        Icon(Icons.Default.Close, contentDescription = "Limpiar")
                                    }
                                } else {
                                    IconButton(
                                        onClick = {
                                            // Scanner simulation shortcut: pick first product
                                            products.firstOrNull()?.let {
                                                viewModel.setPosSearchQuery(it.code)
                                                Toast.makeText(context, "Código escaneado: ${it.code}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Escanear", tint = SafetyOrange)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("product_search_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NavyBluePrimary,
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Category filter chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedLine == null,
                                onClick = { viewModel.setPosSelectedLine(null) },
                                label = { Text("Todos", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NavyBluePrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        items(lines) { line ->
                            FilterChip(
                                selected = selectedLine == line,
                                onClick = { viewModel.setPosSelectedLine(if (selectedLine == line) null else line) },
                                label = { Text(line, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NavyBluePrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Product Cards List
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (filteredProducts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No se encontraron artículos con el filtro actual",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                items(filteredProducts, key = { it.id }) { product ->
                    ProductPosCard(
                        product = product,
                        onAddToCart = { qty -> viewModel.addToCart(product, qty) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp)) // Padding for bottom cart bar
                }
            }
        }

        // Floating Bottom Cart Bar
        if (cart.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(16.dp),
                color = NavyBlueDark,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { showCartModal = true }
                            .weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(SafetyOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Carrito",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "$cartItemsCount art. • Ver detalle",
                                color = Color(0xFFCBD5E1),
                                fontSize = 12.sp
                            )
                            Text(
                                text = "$${String.format(Locale.US, "%.2f", cartTotal)}",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.openCheckout() },
                        colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("cobrar_button")
                    ) {
                        Text(
                            text = "COBRAR",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

    // Modal: Cart Details and Price Tiers
    if (showCartModal) {
        CartDetailsDialog(
            cart = cart,
            total = cartTotal,
            onUpdateQuantity = { id, q -> viewModel.updateCartQuantity(id, q) },
            onUpdateTier = { id, t -> viewModel.updateCartPriceTier(id, t) },
            onRemove = { viewModel.removeFromCart(it) },
            onClear = {
                viewModel.clearCart()
                showCartModal = false
            },
            onCheckout = {
                showCartModal = false
                viewModel.openCheckout()
            },
            onDismiss = { showCartModal = false }
        )
    }

    // Modal: Checkout Dialog (Payment options, change calculator, POS-80C ticket print)
    if (showCheckoutDialog) {
        CheckoutDialog(
            cartTotal = cartTotal,
            customer = selectedCustomer ?: customers.firstOrNull() ?: CustomerEntity(id = 1, name = "PUBLICO EN GENERAL"),
            onDismiss = { viewModel.closeCheckout() },
            onConfirmSale = { method, amount, notes ->
                viewModel.completeCheckout(method, amount, notes)
            }
        )
    }
}

@Composable
fun ProductPosCard(
    product: ProductEntity,
    onAddToCart: (Double) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFFE2E8F0),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = product.code,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (product.brand.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = product.brand,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NavyBluePrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${product.line}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = product.description,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0F172A),
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Price
                    Text(
                        text = "$${String.format(Locale.US, "%.2f", product.priceList)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = SafetyOrange
                    )
                    Text(
                        text = " / ${product.unit}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Stock status pill
                    val stockColor = when {
                        product.stock <= 0 -> StockLow
                        product.stock <= product.minStock -> StockWarning
                        else -> StockOk
                    }
                    Surface(
                        color = stockColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Exist: ${String.format(Locale.US, "%.0f", product.stock)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = stockColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Add Button
            Button(
                onClick = { onAddToCart(1.0) },
                colors = ButtonDefaults.buttonColors(containerColor = NavyBluePrimary),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier.testTag("add_product_${product.code}")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Agregar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CartDetailsDialog(
    cart: List<CartItem>,
    total: Double,
    onUpdateQuantity: (Long, Double) -> Unit,
    onUpdateTier: (Long, PriceTier) -> Unit,
    onRemove: (Long) -> Unit,
    onClear: () -> Unit,
    onCheckout: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Carrito de Compra (${cart.size} productos)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = NavyBluePrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(cart) { item ->
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = item.product.description,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { onRemove(item.product.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Price tier selector pills (Lista, Min, Mayoreo)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Tarifa:", fontSize = 11.sp, color = Color.Gray)
                                    PriceTier.values().forEach { tier ->
                                        val isSelected = item.selectedPriceTier == tier
                                        Surface(
                                            color = if (isSelected) NavyBluePrimary else Color(0xFFE2E8F0),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.clickable { onUpdateTier(item.product.id, tier) }
                                        ) {
                                            Text(
                                                text = "${tier.label} ($" + String.format(
                                                    Locale.US,
                                                    "%.2f",
                                                    when (tier) {
                                                        PriceTier.LIST -> item.product.priceList
                                                        PriceTier.MIN -> item.product.priceMin
                                                        PriceTier.WHOLESALE -> item.product.priceWholesale
                                                    }
                                                ) + ")",
                                                color = if (isSelected) Color.White else Color(0xFF334155),
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Quantity selector
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { onUpdateQuantity(item.product.id, item.quantity - 1) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "Restar", modifier = Modifier.size(16.dp))
                                        }
                                        Text(
                                            text = "${item.quantity.toInt()} ${item.product.unit}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp)
                                        )
                                        IconButton(
                                            onClick = { onUpdateQuantity(item.product.id, item.quantity + 1) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Sumar", modifier = Modifier.size(16.dp))
                                        }
                                    }

                                    // Line Total
                                    Text(
                                        text = "$${String.format(Locale.US, "%.2f", item.lineTotal)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = SafetyOrange
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Totals
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total a Pagar (IVA incl.):", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("$${String.format(Locale.US, "%.2f", total)}", fontWeight = FontWeight.Black, fontSize = 18.sp, color = NavyBluePrimary)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = onClear,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Vaciar", color = Color.Red)
                    }
                    Button(
                        onClick = onCheckout,
                        colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange),
                        modifier = Modifier.weight(2f)
                    ) {
                        Text("Proceder al Cobro", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CheckoutDialog(
    cartTotal: Double,
    customer: CustomerEntity,
    onDismiss: () -> Unit,
    onConfirmSale: (paymentMethod: String, amountReceived: Double, notes: String) -> Unit
) {
    var selectedMethod by remember { mutableStateOf("Efectivo") }
    var amountReceivedText by remember { mutableStateOf(String.format(Locale.US, "%.2f", cartTotal)) }
    var notes by remember { mutableStateOf("") }

    val amountReceived = amountReceivedText.toDoubleOrNull() ?: cartTotal
    val change = (amountReceived - cartTotal).coerceAtLeast(0.0)

    val methods = listOf(
        "Efectivo",
        "Tarjeta Débito",
        "Tarjeta Crédito",
        "CLIP",
        "Transferencia",
        "Crédito"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Cobro de Venta",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = NavyBluePrimary
                        )
                        Text(
                            text = "Cliente: ${customer.name}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Big Total Banner
                Surface(
                    color = NavyBluePrimary,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("TOTAL A COBRAR", color = Color(0xFFCBD5E1), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", cartTotal)}",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 26.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Payment method selector
                Text("Forma de Pago:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(methods) { method ->
                        val isSelected = selectedMethod == method
                        Surface(
                            color = if (isSelected) SafetyOrange else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clickable {
                                    selectedMethod = method
                                    if (method == "Crédito") {
                                        amountReceivedText = "0.00"
                                    } else if (selectedMethod == "Efectivo") {
                                        amountReceivedText = String.format(Locale.US, "%.2f", cartTotal)
                                    }
                                }
                        ) {
                            Text(
                                text = method,
                                color = if (isSelected) Color.White else Color(0xFF1E293B),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // If Credit, show customer limit status
                if (selectedMethod == "Crédito") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFFFFFBEB),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Crédito Autorizado para este cliente:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF92400E))
                            Text("Límite: $${String.format(Locale.US, "%.2f", customer.creditLimit)} • Saldo Actual: $${String.format(Locale.US, "%.2f", customer.currentBalance)}", fontSize = 11.sp)
                            val remaining = customer.creditLimit - customer.currentBalance - cartTotal
                            Text(
                                text = if (remaining >= 0) "Disponible después de compra: $${String.format(Locale.US, "%.2f", remaining)}"
                                else "¡ADVERTENCIA: Excede límite por $${String.format(Locale.US, "%.2f", -remaining)}!",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (remaining >= 0) Color(0xFF16A34A) else Color(0xFFDC2626)
                            )
                        }
                    }
                }

                // If Cash, quick bill buttons & change calculation
                if (selectedMethod == "Efectivo") {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Importe Recibido:", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = amountReceivedText,
                            onValueChange = { amountReceivedText = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("amount_received_input"),
                            singleLine = true,
                            prefix = { Text("$ ") }
                        )

                        // Quick buttons: Exact, 200, 500, 1000
                        listOf(cartTotal, 200.0, 500.0, 1000.0).distinct().forEach { quickVal ->
                            if (quickVal >= cartTotal || quickVal == cartTotal) {
                                Surface(
                                    color = Color(0xFFE2E8F0),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.clickable {
                                        amountReceivedText = String.format(Locale.US, "%.2f", quickVal)
                                    }
                                ) {
                                    Text(
                                        text = if (quickVal == cartTotal) "Exacto" else "$${quickVal.toInt()}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Change banner
                    Surface(
                        color = Color(0xFFF0FDF4),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("CAMBIO A ENTREGAR:", fontWeight = FontWeight.Bold, color = Color(0xFF166534), fontSize = 13.sp)
                            Text(
                                text = "$${String.format(Locale.US, "%.2f", change)}",
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF166534),
                                fontSize = 18.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("Notas de venta / Folio de orden (opcional)...", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        onConfirmSale(selectedMethod, amountReceived, notes)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("confirm_sale_button")
                ) {
                    Text(
                        text = "COBRAR & IMPRIMIR TICKET POS-80C",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
