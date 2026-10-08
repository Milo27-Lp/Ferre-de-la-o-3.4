package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.CashSessionDao
import com.example.data.dao.CustomerDao
import com.example.data.dao.InventoryDao
import com.example.data.dao.ProductDao
import com.example.data.dao.SaleDao
import com.example.data.model.CashSessionEntity
import com.example.data.model.CustomerEntity
import com.example.data.model.InventoryMovementEntity
import com.example.data.model.ProductEntity
import com.example.data.model.SaleEntity
import com.example.data.model.SaleItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProductEntity::class,
        CustomerEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        CashSessionEntity::class,
        InventoryMovementEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun saleDao(): SaleDao
    abstract fun cashSessionDao(): CashSessionDao
    abstract fun inventoryDao(): InventoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ERP_FERRETERIADELAO.db"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            // Seed Customers
            val customers = listOf(
                CustomerEntity(
                    id = 1,
                    name = "PUBLICO EN GENERAL",
                    rfc = "XAXX010101000",
                    phone = "",
                    email = "",
                    address = "Mostrador",
                    isCreditAllowed = false,
                    creditLimit = 0.0,
                    currentBalance = 0.0,
                    notes = "Cliente eventual de mostrador"
                ),
                CustomerEntity(
                    id = 2,
                    name = "CONSTRUCTORA DEL VALLE S.A.",
                    rfc = "CVA9804128K1",
                    phone = "33-1456-7890",
                    email = "compras@constructoradelvalle.mx",
                    address = "Av. Las Américas #1420, Col. Centro",
                    isCreditAllowed = true,
                    creditLimit = 50000.0,
                    currentBalance = 12450.0,
                    notes = "Plazo de crédito 30 días. Facturar con PPD"
                ),
                CustomerEntity(
                    id = 3,
                    name = "TALLER MECÁNICO HERNÁNDEZ",
                    rfc = "HEJA8203159T2",
                    phone = "33-3698-5214",
                    email = "taller.hernandez@gmail.com",
                    address = "Calle Morelos #350, Col. Obrera",
                    isCreditAllowed = true,
                    creditLimit = 20000.0,
                    currentBalance = 4800.0,
                    notes = "Abonos quincenales"
                ),
                CustomerEntity(
                    id = 4,
                    name = "ARQ. ROBERTO MÉNDEZ SÁNCHEZ",
                    rfc = "MESR7511048L9",
                    phone = "33-2587-4102",
                    email = "roberto.mendez@obra.com",
                    address = "Paseo del Lago #88, Col. Valle Real",
                    isCreditAllowed = true,
                    creditLimit = 35000.0,
                    currentBalance = 0.0,
                    notes = "Excelente historial de pago"
                ),
                CustomerEntity(
                    id = 5,
                    name = "PLOMERÍA EXPRESS GUADALAJARA",
                    rfc = "PEG120905M11",
                    phone = "33-3120-4560",
                    email = "servicios@plomeriaexpress.com",
                    address = "Calz. Federalismo #820",
                    isCreditAllowed = true,
                    creditLimit = 15000.0,
                    currentBalance = 2100.0,
                    notes = "Descuento de mayoreo autorizado"
                )
            )
            db.customerDao().insertCustomers(customers)

            // Seed Hardware Store Products
            val products = listOf(
                ProductEntity(
                    id = 1,
                    code = "1001",
                    barcode = "7501206610014",
                    description = "Martillo de uña curva 16 oz mango fibra de vidrio",
                    line = "Herramientas Manuales",
                    brand = "Truper",
                    unit = "PZA",
                    cost = 98.0,
                    priceList = 145.0,
                    priceMin = 135.0,
                    priceWholesale = 125.0,
                    stock = 24.0,
                    minStock = 5.0,
                    reorderStock = 12.0
                ),
                ProductEntity(
                    id = 2,
                    code = "1002",
                    barcode = "7501206610021",
                    description = "Llave ajustable tipo perico 8\" cromada",
                    line = "Herramientas Manuales",
                    brand = "Pretul",
                    unit = "PZA",
                    cost = 82.0,
                    priceList = 125.0,
                    priceMin = 115.0,
                    priceWholesale = 105.0,
                    stock = 18.0,
                    minStock = 4.0,
                    reorderStock = 10.0
                ),
                ProductEntity(
                    id = 3,
                    code = "1003",
                    barcode = "7501206610038",
                    description = "Taladro rotomartillo 1/2\" 650W con maletín y brocas",
                    line = "Herramientas Eléctricas",
                    brand = "DeWalt",
                    unit = "PZA",
                    cost = 1050.0,
                    priceList = 1450.0,
                    priceMin = 1380.0,
                    priceWholesale = 1290.0,
                    stock = 6.0,
                    minStock = 2.0,
                    reorderStock = 5.0
                ),
                ProductEntity(
                    id = 4,
                    code = "1004",
                    barcode = "7501206610045",
                    description = "Pinzas de presión mordaza curva 10\" niqueladas",
                    line = "Herramientas Manuales",
                    brand = "Urrea",
                    unit = "PZA",
                    cost = 120.0,
                    priceList = 180.0,
                    priceMin = 168.0,
                    priceWholesale = 155.0,
                    stock = 15.0,
                    minStock = 3.0,
                    reorderStock = 8.0
                ),
                ProductEntity(
                    id = 5,
                    code = "1005",
                    barcode = "7501206610052",
                    description = "Disco abrasivo de corte para metal 4-1/2\" x 1/16\"",
                    line = "Herramientas Eléctricas",
                    brand = "Austromex",
                    unit = "PZA",
                    cost = 14.5,
                    priceList = 25.0,
                    priceMin = 22.0,
                    priceWholesale = 19.5,
                    stock = 140.0,
                    minStock = 25.0,
                    reorderStock = 60.0
                ),
                ProductEntity(
                    id = 6,
                    code = "1006",
                    barcode = "7501206610069",
                    description = "Flexómetro 5m cinta extra ancha con freno y gancho",
                    line = "Herramientas Manuales",
                    brand = "Truper",
                    unit = "PZA",
                    cost = 58.0,
                    priceList = 95.0,
                    priceMin = 88.0,
                    priceWholesale = 79.0,
                    stock = 35.0,
                    minStock = 6.0,
                    reorderStock = 15.0
                ),
                ProductEntity(
                    id = 7,
                    code = "2001",
                    barcode = "7501206620013",
                    description = "Cinta teflón selladora de roscas 3/4\" x 10m",
                    line = "Plomería",
                    brand = "Pretul",
                    unit = "PZA",
                    cost = 9.5,
                    priceList = 18.5,
                    priceMin = 16.0,
                    priceWholesale = 14.0,
                    stock = 110.0,
                    minStock = 20.0,
                    reorderStock = 50.0
                ),
                ProductEntity(
                    id = 8,
                    code = "2002",
                    barcode = "7501206620020",
                    description = "Tubo CPVC hidráulico 1/2\" tramo de 3 metros",
                    line = "Plomería",
                    brand = "Creska",
                    unit = "PZA",
                    cost = 55.0,
                    priceList = 85.0,
                    priceMin = 79.0,
                    priceWholesale = 72.0,
                    stock = 45.0,
                    minStock = 10.0,
                    reorderStock = 25.0
                ),
                ProductEntity(
                    id = 9,
                    code = "2003",
                    barcode = "7501206620037",
                    description = "Pegamento para CPVC bote 125ml con brocha",
                    line = "Plomería",
                    brand = "Tangit",
                    unit = "PZA",
                    cost = 48.0,
                    priceList = 78.0,
                    priceMin = 72.0,
                    priceWholesale = 65.0,
                    stock = 28.0,
                    minStock = 5.0,
                    reorderStock = 15.0
                ),
                ProductEntity(
                    id = 10,
                    code = "3001",
                    barcode = "7501206630012",
                    description = "Cemento Gris Tolteca Extra bulto 50 kg",
                    line = "Construcción",
                    brand = "Tolteca",
                    unit = "BULTO",
                    cost = 225.0,
                    priceList = 260.0,
                    priceMin = 252.0,
                    priceWholesale = 245.0,
                    stock = 75.0,
                    minStock = 20.0,
                    reorderStock = 50.0
                ),
                ProductEntity(
                    id = 11,
                    code = "3002",
                    barcode = "7501206630029",
                    description = "Clavo para madera con cabeza 2-1/2\" x kilo",
                    line = "Tornillería y Fijación",
                    brand = "DeAcero",
                    unit = "KG",
                    cost = 32.0,
                    priceList = 48.0,
                    priceMin = 44.0,
                    priceWholesale = 40.0,
                    stock = 90.0,
                    minStock = 15.0,
                    reorderStock = 35.0
                ),
                ProductEntity(
                    id = 12,
                    code = "3003",
                    barcode = "7501206630036",
                    description = "Pija autotaladrante punta de broca 1-1/2\" caja 100pz",
                    line = "Tornillería y Fijación",
                    brand = "Fijamex",
                    unit = "CJ",
                    cost = 62.0,
                    priceList = 92.0,
                    priceMin = 85.0,
                    priceWholesale = 78.0,
                    stock = 30.0,
                    minStock = 5.0,
                    reorderStock = 15.0
                ),
                ProductEntity(
                    id = 13,
                    code = "3004",
                    barcode = "7501206630043",
                    description = "Carretilla uso pesado 5.5 ft³ llanta neumática reforzada",
                    line = "Construcción",
                    brand = "Truper",
                    unit = "PZA",
                    cost = 1080.0,
                    priceList = 1380.0,
                    priceMin = 1310.0,
                    priceWholesale = 1240.0,
                    stock = 3.0, // Low stock demo!
                    minStock = 4.0,
                    reorderStock = 8.0
                ),
                ProductEntity(
                    id = 14,
                    code = "4001",
                    barcode = "7501206640011",
                    description = "Cable THW calibre 12 AWG 100% cobre rollo 100m rojo",
                    line = "Electricidad",
                    brand = "IUSA",
                    unit = "ROLLO",
                    cost = 680.0,
                    priceList = 890.0,
                    priceMin = 840.0,
                    priceWholesale = 790.0,
                    stock = 8.0,
                    minStock = 3.0,
                    reorderStock = 6.0
                ),
                ProductEntity(
                    id = 15,
                    code = "4002",
                    barcode = "7501206640028",
                    description = "Apagador sencillo con placa blanco polar Leviton",
                    line = "Electricidad",
                    brand = "Leviton",
                    unit = "PZA",
                    cost = 26.0,
                    priceList = 42.0,
                    priceMin = 38.0,
                    priceWholesale = 34.0,
                    stock = 55.0,
                    minStock = 10.0,
                    reorderStock = 25.0
                ),
                ProductEntity(
                    id = 16,
                    code = "5001",
                    barcode = "7501206650010",
                    description = "Pintura vinílica blanca lavable cubeta 19 Litros",
                    line = "Pinturas y Acabados",
                    brand = "Comex",
                    unit = "CUBETA",
                    cost = 880.0,
                    priceList = 1150.0,
                    priceMin = 1090.0,
                    priceWholesale = 1020.0,
                    stock = 12.0,
                    minStock = 4.0,
                    reorderStock = 8.0
                ),
                ProductEntity(
                    id = 17,
                    code = "5002",
                    barcode = "7501206650027",
                    description = "Brocha de cerda natural 2-1/2\" mango plástico",
                    line = "Pinturas y Acabados",
                    brand = "Éxito",
                    unit = "PZA",
                    cost = 22.0,
                    priceList = 38.0,
                    priceMin = 34.0,
                    priceWholesale = 30.0,
                    stock = 45.0,
                    minStock = 8.0,
                    reorderStock = 20.0
                ),
                ProductEntity(
                    id = 18,
                    code = "6001",
                    barcode = "7501206660019",
                    description = "Candado de hierro latonado 50mm con 3 llaves",
                    line = "Cerrajería",
                    brand = "Phillips",
                    unit = "PZA",
                    cost = 118.0,
                    priceList = 175.0,
                    priceMin = 162.0,
                    priceWholesale = 150.0,
                    stock = 2.0, // Low stock demo!
                    minStock = 5.0,
                    reorderStock = 10.0
                )
            )
            db.productDao().insertProducts(products)

            // Seed Active Cash Session for BERE
            val session = CashSessionEntity(
                id = 1,
                sessionFolio = "SES-2026-001",
                cashierName = "BERE",
                stationName = "DESKTOP-01V4DFC",
                registerName = "CAJA PRINCIPAL",
                printerModel = "POS-80C",
                openTimeMillis = System.currentTimeMillis() - 14400000L, // 4 hours ago
                closeTimeMillis = null,
                initialFund = 1000.0,
                totalCashSales = 1850.0,
                totalCardSales = 1450.0,
                totalCreditSales = 890.0,
                totalWithdrawals = 0.0,
                totalDeposits = 0.0,
                countedCash = 0.0,
                difference = 0.0,
                status = "ABIERTA"
            )
            db.cashSessionDao().insertSession(session)

            // Seed Initial Sale Example for History
            val initialSale = SaleEntity(
                id = 1,
                folio = "TKT-000101",
                timestamp = System.currentTimeMillis() - 7200000L, // 2 hours ago
                cashierName = "BERE",
                customerId = 1,
                customerName = "PUBLICO EN GENERAL",
                subtotal = 344.83,
                taxIva = 55.17,
                total = 400.0,
                paymentMethod = "Efectivo",
                amountReceived = 500.0,
                changeGiven = 100.0,
                isCredit = false,
                status = "EMITIDA",
                notes = "Venta mostrador inicial"
            )
            val saleId = db.saleDao().insertSale(initialSale)
            val saleItems = listOf(
                SaleItemEntity(
                    id = 1,
                    saleId = saleId,
                    productId = 1,
                    productCode = "1001",
                    description = "Martillo de uña curva 16 oz",
                    quantity = 1.0,
                    unit = "PZA",
                    unitPrice = 145.0,
                    lineTotal = 145.0
                ),
                SaleItemEntity(
                    id = 2,
                    saleId = saleId,
                    productId = 2,
                    productCode = "1002",
                    description = "Llave ajustable tipo perico 8\"",
                    quantity = 1.0,
                    unit = "PZA",
                    unitPrice = 125.0,
                    lineTotal = 125.0
                ),
                SaleItemEntity(
                    id = 3,
                    saleId = saleId,
                    productId = 6,
                    productCode = "1006",
                    description = "Flexómetro 5m cinta extra ancha",
                    quantity = 1.0,
                    unit = "PZA",
                    unitPrice = 95.0,
                    lineTotal = 95.0
                ),
                SaleItemEntity(
                    id = 4,
                    saleId = saleId,
                    productId = 7,
                    productCode = "2001",
                    description = "Cinta teflón 3/4\" x 10m",
                    quantity = 2.0,
                    unit = "PZA",
                    unitPrice = 17.5,
                    lineTotal = 35.0
                )
            )
            db.saleDao().insertSaleItems(saleItems)
        }
    }
}
