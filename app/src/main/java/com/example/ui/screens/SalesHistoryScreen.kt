package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.NavyBluePrimary
import com.example.ui.theme.SafetyOrange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SalesHistoryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val sales by viewModel.sales.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredSales = remember(sales, searchQuery) {
        if (searchQuery.isBlank()) sales
        else sales.filter {
            it.folio.contains(searchQuery, ignoreCase = true) ||
                it.customerName.contains(searchQuery, ignoreCase = true) ||
                it.cashierName.contains(searchQuery, ignoreCase = true)
        }
    }

    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    Column(modifier = modifier.fillMaxSize().background(Color(0xFFF1F5F9))) {
        Surface(
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Historial de Ventas y Comprobantes",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = NavyBluePrimary
                )
                Text(
                    text = "Reimpresión de tickets para impresora térmica POS-80C",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar por folio, cliente o cajero...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (filteredSales.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay ventas registradas con el criterio de búsqueda", color = Color.Gray)
                    }
                }
            }

            items(filteredSales, key = { it.id }) { sale ->
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
                                Surface(
                                    color = NavyBluePrimary.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = sale.folio,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp,
                                        color = NavyBluePrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = dateFormat.format(Date(sale.timestamp)),
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }

                            Surface(
                                color = if (sale.status == "EMITIDA") Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = sale.status,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (sale.status == "EMITIDA") Color(0xFF166534) else Color(0xFF991B1B),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Cliente: ${sale.customerName}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.5.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Cajero(a): ${sale.cashierName} • Forma de Pago: ${sale.paymentMethod}",
                            fontSize = 11.5.sp,
                            color = Color(0xFF64748B)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Total Venta", fontSize = 10.sp, color = Color.Gray)
                                Text(
                                    "$${String.format(Locale.US, "%.2f", sale.total)}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = SafetyOrange
                                )
                            }

                            Button(
                                onClick = { viewModel.reprintSaleTicket(sale) },
                                colors = ButtonDefaults.buttonColors(containerColor = NavyBluePrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("reprint_ticket_${sale.folio}")
                            ) {
                                Icon(Icons.Default.Print, contentDescription = "Ticket", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Reimprimir POS-80C", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
