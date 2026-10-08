package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val barcode: String = "",
    val description: String,
    val line: String, // Herramientas, Plomería, Eléctrico, etc.
    val brand: String = "",
    val unit: String = "PZA",
    val cost: Double,
    val priceList: Double,
    val priceMin: Double = priceList * 0.9,
    val priceWholesale: Double = priceList * 0.85,
    val stock: Double,
    val minStock: Double = 5.0,
    val reorderStock: Double = 10.0,
    val isActive: Boolean = true
)
