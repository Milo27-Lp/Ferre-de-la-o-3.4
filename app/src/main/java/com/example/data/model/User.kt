package com.example.data.model

data class User(
    val id: Int,
    val username: String,
    val fullName: String,
    val role: String,
    val permissions: String = "ALL"
)

object SystemUsers {
    val users = listOf(
        User(1, "BERE", "Berenice Morales (Cajera Principal)", "Caja y Administración"),
        User(2, "ADMIN", "Administrador General", "Gerencia y Configuración"),
        User(3, "ALE", "Alejandra Torres", "Ventas Mostrador"),
        User(4, "PEPE", "José 'Pepe' Ramírez", "Ventas y Almacén"),
        User(5, "SAM", "Samuel Delgado", "Mostrador y Crédito"),
        User(6, "CAJA", "Caja Turno 2", "Operación de Caja"),
        User(7, "VENDEDOR 1", "Vendedor Ruta 1", "Ventas Externas")
    )

    var currentUser: User = users[0] // Default to BERE from INI file: LastUser=BERE
}
