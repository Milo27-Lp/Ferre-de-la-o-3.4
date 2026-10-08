package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val rfc: String = "XAXX010101000",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val isCreditAllowed: Boolean = false,
    val creditLimit: Double = 0.0,
    val currentBalance: Double = 0.0,
    val notes: String = ""
)
