package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cash_sessions")
data class CashSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionFolio: String,
    val cashierName: String,
    val stationName: String = "DESKTOP-01V4DFC",
    val registerName: String = "CAJA PRINCIPAL",
    val printerModel: String = "POS-80C",
    val openTimeMillis: Long = System.currentTimeMillis(),
    val closeTimeMillis: Long? = null,
    val initialFund: Double = 1000.0,
    val totalCashSales: Double = 0.0,
    val totalCardSales: Double = 0.0,
    val totalCreditSales: Double = 0.0,
    val totalWithdrawals: Double = 0.0,
    val totalDeposits: Double = 0.0,
    val countedCash: Double = 0.0,
    val difference: Double = 0.0,
    val status: String = "ABIERTA" // ABIERTA, CERRADA
)
