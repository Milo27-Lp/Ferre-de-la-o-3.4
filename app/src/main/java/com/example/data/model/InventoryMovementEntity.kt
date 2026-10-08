package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_movements")
data class InventoryMovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String, // ENTRADA, SALIDA, AJUSTE, MERMA, VENTA
    val productCode: String,
    val productDescription: String,
    val quantity: Double,
    val previousStock: Double,
    val newStock: Double,
    val reason: String = "",
    val user: String = "BERE"
)
