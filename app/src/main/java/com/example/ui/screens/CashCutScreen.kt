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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableStateMapOf
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
import com.example.data.model.CashSessionEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.NavyBlueDark
import com.example.ui.theme.NavyBluePrimary
import com.example.ui.theme.SafetyOrange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CashCutScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeSession by viewModel.activeCashSession.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showOpenSessionDialog by remember { mutableStateOf(false) }

    // Denominations for Mexican Peso cash counting
    val billDenominations = listOf(1000.0, 500.0, 200.0, 100.0, 50.0, 20.0)
    val coinDenominations = listOf(10.0, 5.0, 2.0, 1.0, 0.5)

    val denominationCounts = remember {
        mutableStateMapOf<Double, Int>().apply {
            billDenominations.forEach { put(it, 0) }
            coinDenominations.forEach { put(it, 0) }
            // Seed a realistic cash drawer sample
            put(500.0, 3)  // 1500
            put(200.0, 4)  // 800
            put(100.0, 4)  // 400
            put(50.0, 2)   // 100
            put(20.0, 2)   // 40
            put(10.0, 1)   // 10
        }
    }

    val totalCountedCash = remember(denominationCounts.values.toList()) {
        denominationCounts.entries.sumOf { (denom, count) -> denom * count }
    }

    val currentOrNewSession = activeSession ?: CashSessionEntity(
        sessionFolio = "SES-SIN-ABRIR",
        cashierName = currentUser.username,
        initialFund = 0.0,
        status = "CERRADA"
    )

    val expectedCashInDrawer = currentOrNewSession.initialFund + currentOrNewSession.totalCashSales +
        currentOrNewSession.totalDeposits - currentOrNewSession.totalWithdrawals

    val cashDifference = totalCountedCash - expectedCashInDrawer

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
    ) {
        // Status & Session Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavyBlueDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(SafetyOrange),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PointOfSale,
                                    contentDescription = "Caja",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Turno de Caja: ${currentOrNewSession.sessionFolio}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Cajero: ${currentOrNewSession.cashierName} • Estación: ${currentOrNewSession.stationName}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Surface(
                            color = if (currentOrNewSession.status == "ABIERTA") Color(0xFF10B981) else Color(0xFFEF4444),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = if (currentOrNewSession.status == "ABIERTA") Icons.Default.LockOpen else Icons.Default.Lock,
                                    contentDescription = "Estado",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = currentOrNewSession.status,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Sales Metrics Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Fondo Inicial", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text("$${String.format(Locale.US, "%.2f", currentOrNewSession.initialFund)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Column {
                            Text("Ventas Efectivo", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text("$${String.format(Locale.US, "%.2f", currentOrNewSession.totalCashSales)}", color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Column {
                            Text("Tarjetas / CLIP", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text("$${String.format(Locale.US, "%.2f", currentOrNewSession.totalCardSales)}", color = Color(0xFF60A5FA), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Column {
                            Text("A Crédito", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text("$${String.format(Locale.US, "%.2f", currentOrNewSession.totalCreditSales)}", color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = Color.White.copy(alpha = 0.08f),
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
                            Text(
                                text = "EFECTIVO ESPERADO EN CAJA:",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "$${String.format(Locale.US, "%.2f", expectedCashInDrawer)}",
                                color = SafetyOrange,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        }

        // Arqueo / Conteo de Billetes y Monedas
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Arqueo de Caja (Conteo de Efectivo)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = NavyBluePrimary
                    )
                    Text(
                        text = "Capture la cantidad de billetes y monedas en el cajón",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Billetes:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(6.dp))

                    billDenominations.forEach { denom ->
                        val count = denominationCounts[denom] ?: 0
                        DenominationRow(
                            denomination = denom,
                            count = count,
                            isBill = true,
                            onCountChange = { denominationCounts[denom] = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Monedas:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(6.dp))

                    coinDenominations.forEach { denom ->
                        val count = denominationCounts[denom] ?: 0
                        DenominationRow(
                            denomination = denom,
                            count = count,
                            isBill = false,
                            onCountChange = { denominationCounts[denom] = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Reconciliation Banner
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Contado Físico:", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                "$${String.format(Locale.US, "%.2f", totalCountedCash)}",
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = NavyBluePrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Diferencia:", fontSize = 12.sp, color = Color.Gray)
                            val diffColor = when {
                                kotlin.math.abs(cashDifference) < 0.01 -> Color(0xFF16A34A)
                                cashDifference > 0 -> Color(0xFF2563EB) // Sobrante
                                else -> Color(0xFFDC2626) // Faltante
                            }
                            val diffText = when {
                                kotlin.math.abs(cashDifference) < 0.01 -> "Cuadrado Exacto ($0.00)"
                                cashDifference > 0 -> "+$${String.format(Locale.US, "%.2f", cashDifference)} (Sobrante)"
                                else -> "-$${String.format(Locale.US, "%.2f", kotlin.math.abs(cashDifference))} (Faltante)"
                            }

                            Text(
                                text = diffText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = diffColor
                            )
                        }
                    }
                }
            }
        }

        // Actions: Corte X, Corte Z, Apertura
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Operaciones de Corte (Impresora POS-80C)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = NavyBluePrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (activeSession != null) {
                                    viewModel.generatePartialCutX(activeSession!!, totalCountedCash)
                                } else {
                                    Toast.makeText(context, "No hay sesión abierta", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("corte_x_button")
                        ) {
                            Icon(Icons.Default.Print, contentDescription = "Corte X", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Corte 'X' Parcial", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                if (activeSession != null) {
                                    viewModel.performCashCut(activeSession!!, totalCountedCash)
                                    Toast.makeText(context, "Corte Z Finalizado e Impreso", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "La sesión ya está cerrada", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange),
                            enabled = activeSession != null,
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("corte_z_button")
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = "Corte Z", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cerrar Turno (Corte Z)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (activeSession == null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { showOpenSessionDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyBluePrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("open_session_button")
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = "Abrir", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Abrir Nueva Sesión de Caja", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showOpenSessionDialog) {
        OpenSessionDialog(
            cashierName = currentUser.username,
            onDismiss = { showOpenSessionDialog = false },
            onConfirm = { fund ->
                viewModel.openNewSession(fund)
                showOpenSessionDialog = false
                Toast.makeText(context, "Sesión abierta con fondo de $$fund", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun DenominationRow(
    denomination: Double,
    count: Int,
    isBill: Boolean,
    onCountChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                color = if (isBill) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = if (denomination >= 1.0) "$${denomination.toInt()}" else "$0.50",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (isBill) Color(0xFF166534) else Color(0xFF92400E),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "= $${String.format(Locale.US, "%.2f", denomination * count)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF334155)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { if (count > 0) onCountChange(count - 1) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Menos", modifier = Modifier.size(16.dp))
            }

            Text(
                text = "$count",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            IconButton(
                onClick = { onCountChange(count + 1) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Más", modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun OpenSessionDialog(
    cashierName: String,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var fundText by remember { mutableStateOf("1000") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Apertura de Caja",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = NavyBluePrimary
                )
                Text(
                    text = "Cajero(a): $cashierName • Estación: DESKTOP-01V4DFC",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = fundText,
                    onValueChange = { fundText = it },
                    label = { Text("Fondo Inicial de Caja ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    prefix = { Text("$ ") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            val fund = fundText.toDoubleOrNull() ?: 1000.0
                            onConfirm(fund)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Abrir Turno", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
