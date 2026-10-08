package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.data.model.CustomerEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.NavyBluePrimary
import com.example.ui.theme.SafetyOrange
import java.util.Locale

@Composable
fun CustomersScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val customers by viewModel.customers.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var filterDebtOnly by remember { mutableStateOf(false) }

    var customerForPayment by remember { mutableStateOf<CustomerEntity?>(null) }
    var showNewCustomerDialog by remember { mutableStateOf(false) }

    val filteredCustomers = remember(customers, searchQuery, filterDebtOnly) {
        customers.filter { c ->
            val matchesSearch = searchQuery.isBlank() ||
                c.name.contains(searchQuery, ignoreCase = true) ||
                c.rfc.contains(searchQuery, ignoreCase = true) ||
                c.phone.contains(searchQuery, ignoreCase = true)
            val matchesDebt = !filterDebtOnly || c.currentBalance > 0
            matchesSearch && matchesDebt
        }
    }

    val totalDebt = customers.sumOf { it.currentBalance }

    Column(modifier = modifier.fillMaxSize().background(Color(0xFFF1F5F9))) {
        // Top Header and Metrics
        Surface(
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Cartera de Clientes & Cuentas por Cobrar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = NavyBluePrimary
                        )
                        Text(
                            text = "Saldo total por cobrar: $${String.format(Locale.US, "%.2f", totalDebt)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SafetyOrange
                        )
                    }

                    Button(
                        onClick = { showNewCustomerDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("new_customer_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Nuevo")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nuevo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar cliente por nombre o RFC...", fontSize = 12.sp) },
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

                    FilterChip(
                        selected = filterDebtOnly,
                        onClick = { filterDebtOnly = !filterDebtOnly },
                        label = { Text("Con Saldo Pendiente", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SafetyOrange,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // List of Customers
        LazyColumn(
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredCustomers, key = { it.id }) { customer ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(NavyBluePrimary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Cliente",
                                        tint = NavyBluePrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = customer.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "RFC: ${customer.rfc}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            if (customer.isCreditAllowed) {
                                Surface(
                                    color = if (customer.currentBalance > 0) Color(0xFFFEF3C7) else Color(0xFFDCFCE7),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = if (customer.currentBalance > 0) "Con Crédito Activo" else "Crédito al Corriente",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (customer.currentBalance > 0) Color(0xFF92400E) else Color(0xFF166534),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        if (customer.phone.isNotEmpty() || customer.address.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (customer.phone.isNotEmpty()) {
                                    Icon(Icons.Default.Phone, contentDescription = "Tel", tint = Color.Gray, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(customer.phone, fontSize = 11.sp, color = Color.Gray)
                                    Spacer(modifier = Modifier.width(12.dp))
                                }
                                if (customer.address.isNotEmpty()) {
                                    Icon(Icons.Default.LocationOn, contentDescription = "Dir", tint = Color.Gray, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(customer.address, fontSize = 11.sp, color = Color.Gray, maxLines = 1)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Credit metrics and Abono button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (customer.isCreditAllowed) {
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Column {
                                        Text("Saldo Deudor", fontSize = 10.sp, color = Color.Gray)
                                        Text(
                                            "$${String.format(Locale.US, "%.2f", customer.currentBalance)}",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 15.sp,
                                            color = if (customer.currentBalance > 0) Color(0xFFDC2626) else Color(0xFF16A34A)
                                        )
                                    }
                                    Column {
                                        Text("Límite Autorizado", fontSize = 10.sp, color = Color.Gray)
                                        Text(
                                            "$${String.format(Locale.US, "%.2f", customer.creditLimit)}",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF334155)
                                        )
                                    }
                                    Column {
                                        Text("Disponible", fontSize = 10.sp, color = Color.Gray)
                                        Text(
                                            "$${String.format(Locale.US, "%.2f", customer.creditLimit - customer.currentBalance)}",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF16A34A)
                                        )
                                    }
                                }

                                if (customer.currentBalance > 0) {
                                    Button(
                                        onClick = { customerForPayment = customer },
                                        colors = ButtonDefaults.buttonColors(containerColor = NavyBluePrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("abono_button_${customer.id}")
                                    ) {
                                        Icon(Icons.Default.Payments, contentDescription = "Abonar", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Abonar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                Text("Cliente de Contado Mostrador", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Abono / Payment Dialog
    customerForPayment?.let { customer ->
        CustomerPaymentDialog(
            customer = customer,
            onDismiss = { customerForPayment = null },
            onConfirmPayment = { amount, notes ->
                viewModel.processCustomerPayment(customer, amount, notes)
                customerForPayment = null
                Toast.makeText(context, "Abono registrado para ${customer.name}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal: New Customer Dialog
    if (showNewCustomerDialog) {
        NewCustomerDialog(
            onDismiss = { showNewCustomerDialog = false },
            onConfirm = { newCust ->
                viewModel.saveCustomer(newCust)
                showNewCustomerDialog = false
                Toast.makeText(context, "Cliente ${newCust.name} guardado", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun CustomerPaymentDialog(
    customer: CustomerEntity,
    onDismiss: () -> Unit,
    onConfirmPayment: (amount: Double, notes: String) -> Unit
) {
    var amountText by remember { mutableStateOf(String.format(Locale.US, "%.2f", customer.currentBalance)) }
    var notes by remember { mutableStateOf("Pago en caja mostrador") }
    val amount = amountText.toDoubleOrNull() ?: 0.0

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Registrar Abono a Cuenta",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = NavyBluePrimary
                )
                Text(
                    text = customer.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color(0xFF334155)
                )
                Text(
                    text = "Saldo Pendiente: $${String.format(Locale.US, "%.2f", customer.currentBalance)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFFDC2626)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Monto del Abono ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    prefix = { Text("$ ") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Concepto / Referencia") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            if (amount > 0) {
                                onConfirmPayment(amount, notes)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Text("Registrar & Recibo", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun NewCustomerDialog(
    onDismiss: () -> Unit,
    onConfirm: (CustomerEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var rfc by remember { mutableStateOf("XAXX010101000") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var isCredit by remember { mutableStateOf(false) }
    var creditLimitText by remember { mutableStateOf("10000") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                item {
                    Text(
                        text = "Registrar Nuevo Cliente",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = NavyBluePrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre o Razón Social*") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = rfc,
                            onValueChange = { rfc = it },
                            label = { Text("R.F.C.") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Teléfono") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Dirección / Obra") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isCredit,
                            onCheckedChange = { isCredit = it }
                        )
                        Text("Autorizar Línea de Crédito", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }

                    if (isCredit) {
                        OutlinedTextField(
                            value = creditLimitText,
                            onValueChange = { creditLimitText = it },
                            label = { Text("Límite de Crédito ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            prefix = { Text("$ ") },
                            modifier = Modifier.fillMaxWidth()
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
                                val limit = if (isCredit) creditLimitText.toDoubleOrNull() ?: 0.0 else 0.0
                                if (name.isNotBlank()) {
                                    val newCustomer = CustomerEntity(
                                        name = name.trim(),
                                        rfc = rfc.trim().ifBlank { "XAXX010101000" },
                                        phone = phone.trim(),
                                        email = email.trim(),
                                        address = address.trim(),
                                        isCreditAllowed = isCredit,
                                        creditLimit = limit
                                    )
                                    onConfirm(newCustomer)
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
