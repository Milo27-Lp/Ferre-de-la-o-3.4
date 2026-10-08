package com.example.data

import com.example.data.model.CashSessionEntity
import com.example.data.model.CustomerEntity
import com.example.data.model.InventoryMovementEntity
import com.example.data.model.ProductEntity
import com.example.data.model.SaleEntity
import com.example.data.model.SaleItemEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CartItem(
    val product: ProductEntity,
    var quantity: Double,
    var selectedPriceTier: PriceTier = PriceTier.LIST
) {
    val unitPrice: Double
        get() = when (selectedPriceTier) {
            PriceTier.LIST -> product.priceList
            PriceTier.MIN -> product.priceMin
            PriceTier.WHOLESALE -> product.priceWholesale
        }

    val lineTotal: Double
        get() = unitPrice * quantity
}

enum class PriceTier(val label: String) {
    LIST("Lista"),
    MIN("Mínimo"),
    WHOLESALE("Mayoreo")
}

class HardwareRepository(private val db: AppDatabase) {
    private val productDao = db.productDao()
    private val customerDao = db.customerDao()
    private val saleDao = db.saleDao()
    private val cashSessionDao = db.cashSessionDao()
    private val inventoryDao = db.inventoryDao()

    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val allLines: Flow<List<String>> = productDao.getAllLines()
    val lowStockProducts: Flow<List<ProductEntity>> = productDao.getLowStockProducts()
    val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
    val creditCustomers: Flow<List<CustomerEntity>> = customerDao.getCustomersWithDebt()
    val allSales: Flow<List<SaleEntity>> = saleDao.getAllSales()
    val activeCashSession: Flow<CashSessionEntity?> = cashSessionDao.getActiveSession()
    val inventoryMovements: Flow<List<InventoryMovementEntity>> = inventoryDao.getAllMovements()

    fun searchProducts(query: String): Flow<List<ProductEntity>> = productDao.searchProducts(query)

    fun getProductsByLine(line: String): Flow<List<ProductEntity>> = productDao.getProductsByLine(line)

    fun getSaleItems(saleId: Long): Flow<List<SaleItemEntity>> = saleDao.getSaleItems(saleId)

    suspend fun getSaleItemsSync(saleId: Long): List<SaleItemEntity> = saleDao.getSaleItemsSync(saleId)

    suspend fun findProductByCode(code: String): ProductEntity? = productDao.getProductByCodeOrBarcode(code)

    suspend fun saveProduct(product: ProductEntity) {
        if (product.id == 0L) {
            productDao.insertProduct(product)
        } else {
            productDao.updateProduct(product)
        }
    }

    suspend fun saveCustomer(customer: CustomerEntity) {
        if (customer.id == 0L) {
            customerDao.insertCustomer(customer)
        } else {
            customerDao.updateCustomer(customer)
        }
    }

    suspend fun registerCustomerPayment(
        customer: CustomerEntity,
        amount: Double,
        cashierName: String,
        notes: String
    ): String {
        val newBalance = (customer.currentBalance - amount).coerceAtLeast(0.0)
        customerDao.updateBalance(customer.id, newBalance)

        // Update cash session with deposit
        val session = cashSessionDao.getActiveSessionSync()
        if (session != null) {
            val updated = session.copy(
                totalCashSales = session.totalCashSales + amount
            )
            cashSessionDao.updateSession(updated)
        }

        // Return a payment receipt folio
        val folio = "ABONO-${System.currentTimeMillis() % 100000}"
        return folio
    }

    suspend fun adjustInventory(
        productId: Long,
        type: String, // ENTRADA, SALIDA, AJUSTE, MERMA
        quantityDelta: Double,
        reason: String,
        userName: String
    ) {
        val product = productDao.getProductById(productId) ?: return
        val newStock = when (type) {
            "ENTRADA" -> product.stock + quantityDelta
            "SALIDA", "MERMA" -> (product.stock - quantityDelta).coerceAtLeast(0.0)
            "AJUSTE" -> quantityDelta // Direct override
            else -> product.stock
        }

        productDao.updateStock(productId, newStock)

        inventoryDao.insertMovement(
            InventoryMovementEntity(
                type = type,
                productCode = product.code,
                productDescription = product.description,
                quantity = quantityDelta,
                previousStock = product.stock,
                newStock = newStock,
                reason = reason,
                user = userName
            )
        )
    }

    suspend fun completeSale(
        items: List<CartItem>,
        customer: CustomerEntity,
        cashierName: String,
        paymentMethod: String,
        amountReceived: Double,
        notes: String
    ): Pair<SaleEntity, List<SaleItemEntity>> {
        val total = items.sumOf { it.lineTotal }
        val subtotal = total / 1.16
        val taxIva = total - subtotal
        val isCredit = paymentMethod == "Crédito" || paymentMethod == "Apartado"
        val change = if (isCredit) 0.0 else (amountReceived - total).coerceAtLeast(0.0)

        val nextCount = saleDao.getSalesCount() + 1
        val folio = "TKT-${String.format(Locale.US, "%06d", nextCount)}"

        val sale = SaleEntity(
            folio = folio,
            timestamp = System.currentTimeMillis(),
            cashierName = cashierName,
            customerId = customer.id,
            customerName = customer.name,
            subtotal = subtotal,
            taxIva = taxIva,
            total = total,
            paymentMethod = paymentMethod,
            amountReceived = amountReceived,
            changeGiven = change,
            isCredit = isCredit,
            status = "EMITIDA",
            notes = notes
        )

        val saleId = saleDao.insertSale(sale)
        val savedSale = sale.copy(id = saleId)

        val saleItems = items.map { item ->
            SaleItemEntity(
                saleId = saleId,
                productId = item.product.id,
                productCode = item.product.code,
                description = item.product.description,
                quantity = item.quantity,
                unit = item.product.unit,
                unitPrice = item.unitPrice,
                lineTotal = item.lineTotal
            )
        }
        saleDao.insertSaleItems(saleItems)

        // Decrement product inventory and record movement
        for (item in items) {
            val newStock = (item.product.stock - item.quantity).coerceAtLeast(0.0)
            productDao.updateStock(item.product.id, newStock)
            inventoryDao.insertMovement(
                InventoryMovementEntity(
                    type = "VENTA",
                    productCode = item.product.code,
                    productDescription = item.product.description,
                    quantity = item.quantity,
                    previousStock = item.product.stock,
                    newStock = newStock,
                    reason = "Folio: $folio",
                    user = cashierName
                )
            )
        }

        // If Credit, update customer balance
        if (isCredit) {
            customerDao.updateBalance(customer.id, customer.currentBalance + total)
        }

        // Update active cash session
        val activeSession = cashSessionDao.getActiveSessionSync()
        if (activeSession != null) {
            val updatedSession = when {
                paymentMethod == "Efectivo" -> activeSession.copy(totalCashSales = activeSession.totalCashSales + total)
                paymentMethod.contains("Tarjeta") || paymentMethod == "CLIP" || paymentMethod == "Transferencia" ->
                    activeSession.copy(totalCardSales = activeSession.totalCardSales + total)
                isCredit -> activeSession.copy(totalCreditSales = activeSession.totalCreditSales + total)
                else -> activeSession.copy(totalCashSales = activeSession.totalCashSales + total)
            }
            cashSessionDao.updateSession(updatedSession)
        }

        return Pair(savedSale, saleItems)
    }

    suspend fun closeCashSession(
        session: CashSessionEntity,
        countedCash: Double
    ): CashSessionEntity {
        val expectedCash = session.initialFund + session.totalCashSales - session.totalWithdrawals + session.totalDeposits
        val diff = countedCash - expectedCash

        val closed = session.copy(
            closeTimeMillis = System.currentTimeMillis(),
            countedCash = countedCash,
            difference = diff,
            status = "CERRADA"
        )
        cashSessionDao.updateSession(closed)
        return closed
    }

    suspend fun openNewCashSession(cashierName: String, initialFund: Double): CashSessionEntity {
        val count = (System.currentTimeMillis() % 10000).toInt()
        val newSession = CashSessionEntity(
            sessionFolio = "SES-2026-$count",
            cashierName = cashierName,
            openTimeMillis = System.currentTimeMillis(),
            initialFund = initialFund,
            totalCashSales = 0.0,
            totalCardSales = 0.0,
            totalCreditSales = 0.0,
            status = "ABIERTA"
        )
        val id = cashSessionDao.insertSession(newSession)
        return newSession.copy(id = id)
    }
}
