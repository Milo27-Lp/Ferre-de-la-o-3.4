package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val folio: String,
    val timestamp: Long = System.currentTimeMillis(),
    val cashierName: String,
    val customerId: Long,
    val customerName: String,
    val subtotal: Double,
    val taxIva: Double,
    val total: Double,
    val paymentMethod: String, // Efectivo, Tarjeta Débito, Tarjeta Crédito, CLIP, Transferencia, Crédito, Apartado
    val amountReceived: Double,
    val changeGiven: Double,
    val isCredit: Boolean = false,
    val status: String = "EMITIDA", // EMITIDA, CANCELADA
    val notes: String = ""
)
