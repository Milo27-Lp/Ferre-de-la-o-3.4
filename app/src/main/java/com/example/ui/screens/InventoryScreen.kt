package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.InventoryMovementEntity
import com.example.data.model.ProductEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.NavyBluePrimary
import com.example.ui.theme.SafetyOrange
import com.example.ui.theme.StockLow
import com.example.ui.theme.StockOk
import com.example.ui.theme.StockWarning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InventoryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val products by viewModel.products.collectAsState()
    val lines by viewModel.lines.collectAsState()
    val lowStockProducts by viewModel.lowStockProducts.collectAsState()
    val movements by viewModel.inventoryMovements.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) } // 0: Catalogo, 1: Kardex
    var searchQuery by remember { mutableStateOf("") }
    var selectedLine by remember { mutableStateOf<String?>(null) }
    var filterLowStockOnly by remember { mutableStateOf(false) }

    var productToAdjust by remember { mutableStateOf<ProductEntity?>(null) }
    var showNewProductDialog by remember { mutableStateOf(false) }

    val filteredProducts = remember(products, searchQuery, selectedLine, filterLowStockOnly) {
        products.filter { p ->
            val matchesQuery = searchQuery.isBlank() ||
                p.description.contains(searchQuery, ignoreCase = true) ||
                p.code.contains(searchQuery, ignoreCase = true) ||
                p.brand.contains(searchQuery, ignoreCase = true) ||
                p.barcode.contains(searchQuery, ignoreCase = true)

            val matchesLine = selectedLine == null || p.line.equals(selectedLine, ignoreCase = true)
            val matchesLowStock = !filterLowStockOnly || (p.stock <= p.minStock)
            matchesQuery && matchesLine && matchesLowStock
        }
    }

    Column(modifier = modifier.fillMaxSize().background(Color(0xFFF1F5F9))) {
        // Top Tab Row
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.White,
            contentColor = NavyBluePrimary
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("Catálogo de Artículos (${products.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("Kardex de Movimientos (${movements.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
        }

        if (selectedTabIndex == 0) {
            // Catalogo view
            Surface(
                color = Color.White,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Buscar por código, descripción, marca...", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Limpiar")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = { showNewProductDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("new_product_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Nuevo")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Nuevo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Filters row
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item {
                            FilterChip(
                                selected = filterLowStockOnly,
                                onClick = { filterLowStockOnly = !filterLowStockOnly },
                                label = { Text("⚠️ Bajo Stock (${lowStockProducts.size})", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFEE2E2),
                                    selectedLabelColor = Color(0xFF991B1B)
                                )
                            )
                        }

                        item {
                            FilterChip(
                                selected = selectedLine == null,
                                onClick = { selectedLine = null },
                                label = { Text("Todas las Líneas", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NavyBluePrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }

                        items(lines) { line ->
                            FilterChip(
                                selected = selectedLine == line,
                                onClick = { selectedLine = if (selectedLine == line) null else line },
                                label = { Text(line, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NavyBluePrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Products list
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredProducts, key = { it.id }) { product ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = NavyBluePrimary.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "CÓD: ${product.code}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = NavyBluePrimary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${product.brand} • ${product.line}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }

                                val stockColor = when {
                                    product.stock <= 0 -> StockLow
                                    product.stock <= product.minStock -> StockWarning
                                    else -> StockOk
                                }
                                Surface(
                                    color = stockColor.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "Stock: ${String.format(Locale.US, "%.0f", product.stock)} ${product.unit}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = stockColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = product.description,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color(0xFF0F172A)
                            )

                            if (product.barcode.isNotEmpty()) {
                                Text(
                                    text = "Código de barras: ${product.barcode}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(8.dp))

                            // Pricing & Actions Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Column {
                                            Text("P. Lista", fontSize = 10.sp, color = Color.Gray)
                                            Text(
                                                "$${String.format(Locale.US, "%.2f", product.priceList)}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = SafetyOrange
                                            )
                                        }
                                        Column {
                                            Text("P. Mayoreo", fontSize = 10.sp, color = Color.Gray)
                                            Text(
                                                "$${String.format(Locale.US, "%.2f", product.priceWholesale)}",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                color = Color(0xFF334155)
                                            )
                                        }
                                        Column {
                                            Text("Costo", fontSize = 10.sp, color = Color.Gray)
                                            Text(
                                                "$${String.format(Locale.US, "%.2f", product.cost)}",
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(
                                        onClick = { productToAdjust = product },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("adjust_stock_${product.code}")
                                    ) {
                                        Icon(Icons.Default.SwapHoriz, contentDescription = "Ajustar", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Ajustar", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Kardex / Movements history
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (movements.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No hay movimientos registrados en el kardex", color = Color.Gray)
                        }
                    }
                }

                items(movements) { mov ->
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    val typeColor = when (mov.type) {
                        "ENTRADA" -> Color(0xFF16A34A)
                        "VENTA", "SALIDA" -> NavyBluePrimary
                        "MERMA" -> Color(0xFFDC2626)
                        else -> SafetyOrange
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = typeColor.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = mov.type,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = typeColor,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = dateFormat.format(Date(mov.timestamp)),
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "[${mov.productCode}] ${mov.productDescription}",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Por: ${mov.user} • ${mov.reason}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${if (mov.type == "ENTRADA") "+" else "-"}${String.format(Locale.US, "%.0f", mov.quantity)}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = typeColor
                                )
                                Text(
                                    text = "${String.format(Locale.US, "%.0f", mov.previousStock)} ➔ ${String.format(Locale.US, "%.0f", mov.newStock)}",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Stock Adjustment Dialog
    productToAdjust?.let { product ->
        StockAdjustmentDialog(
            product = product,
            onDismiss = { productToAdjust = null },
            onConfirm = { type, qty, reason ->
                viewModel.adjustStock(product.id, type, qty, reason)
                productToAdjust = null
                Toast.makeText(context, "Stock de ${product.code} actualizado", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal: New Product Dialog
    if (showNewProductDialog) {
        NewProductDialog(
            existingLines = lines,
            onDismiss = { showNewProductDialog = false },
            onConfirm = { newProd ->
                viewModel.saveProduct(newProd)
                showNewProductDialog = false
                Toast.makeText(context, "Producto ${newProd.code} guardado", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun StockAdjustmentDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onConfirm: (type: String, quantity: Double, reason: String) -> Unit
) {
    var type by remember { mutableStateOf("ENTRADA") } // ENTRADA, SALIDA, MERMA, AJUSTE
    var quantityText by remember { mutableStateOf("1") }
    var reason by remember { mutableStateOf("") }

    val qty = quantityText.toDoubleOrNull() ?: 1.0
    val types = listOf("ENTRADA", "SALIDA", "MERMA", "AJUSTE")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Ajuste de Stock",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = NavyBluePrimary
                )
                Text(
                    text = "[${product.code}] ${product.description}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Stock Actual: ${String.format(Locale.US, "%.0f", product.stock)} ${product.unit}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = SafetyOrange
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Adjustment type selection
                Text("Tipo de Movimiento:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    types.forEach { t ->
                        val isSelected = type == t
                        Surface(
                            color = if (isSelected) NavyBluePrimary else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f).clickable { type = t }
                        ) {
                            Text(
                                text = t,
                                color = if (isSelected) Color.White else Color(0xFF334155),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text(if (type == "AJUSTE") "Nuevo Stock Total" else "Cantidad a Modificar") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Motivo / Referencia (ej. Factura compra, daño)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = { onConfirm(type, qty, reason) },
                        colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Aplicar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun NewProductDialog(
    existingLines: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (ProductEntity) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var line by remember { mutableStateOf(existingLines.firstOrNull() ?: "Herramientas Manuales") }
    var brand by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("PZA") }
    var costText by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var stockText by remember { mutableStateOf("10") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                item {
                    Text(
                        text = "Registrar Nuevo Artículo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = NavyBluePrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = code,
                            onValueChange = { code = it },
                            label = { Text("Código*") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = barcode,
                            onValueChange = { barcode = it },
                            label = { Text("Cód. Barras") },
                            modifier = Modifier.weight(1.5f),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descripción del Producto*") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = brand,
                            onValueChange = { brand = it },
                            label = { Text("Marca (ej. Truper)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("Unidad (PZA, KG...)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    OutlinedTextField(
                        value = line,
                        onValueChange = { line = it },
                        label = { Text("Línea / Categoría") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = costText,
                            onValueChange = { costText = it },
                            label = { Text("Costo ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it },
                            label = { Text("Precio Lista ($)*") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = stockText,
                            onValueChange = { stockText = it },
                            label = { Text("Stock Inicial") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                            Text("Cancelar")
                        }
                        Button(
                            onClick = {
                                val price = priceText.toDoubleOrNull() ?: 0.0
                                val cost = costText.toDoubleOrNull() ?: (price * 0.7)
                                val stock = stockText.toDoubleOrNull() ?: 0.0
                                if (code.isNotBlank() && description.isNotBlank() && price > 0) {
                                    val newP = ProductEntity(
                                        code = code.trim(),
                                        barcode = barcode.trim(),
                                        description = description.trim(),
                                        line = line.trim(),
                                        brand = brand.trim(),
                                        unit = unit.trim(),
                                        cost = cost,
                                        priceList = price,
                                        priceMin = price * 0.92,
                                        priceWholesale = price * 0.86,
                                        stock = stock
                                    )
                                    onConfirm(newP)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Guardar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
