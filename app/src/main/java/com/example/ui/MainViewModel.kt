package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CartItem
import com.example.data.HardwareRepository
import com.example.data.PriceTier
import com.example.data.model.CashSessionEntity
import com.example.data.model.CustomerEntity
import com.example.data.model.ProductEntity
import com.example.data.model.SaleEntity
import com.example.data.model.SaleItemEntity
import com.example.data.model.SystemUsers
import com.example.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainScreen(val title: String) {
    POS("Punto de Venta"),
    INVENTORY("Inventario"),
    CUSTOMERS("Clientes & Crédito"),
    CASH_CUT("Corte de Caja"),
    SALES_HISTORY("Historial")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    val repository = HardwareRepository(database)

    // Current User
    private val _currentUser = MutableStateFlow<User>(SystemUsers.currentUser)
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    // Current Screen
    private val _currentScreen = MutableStateFlow(MainScreen.POS)
    val currentScreen: StateFlow<MainScreen> = _currentScreen.asStateFlow()

    // Data Flows
    val products = repository.allProducts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val lines = repository.allLines.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val lowStockProducts = repository.lowStockProducts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val customers = repository.allCustomers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val sales = repository.allSales.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val activeCashSession = repository.activeCashSession.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val inventoryMovements = repository.inventoryMovements.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // POS State
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<CustomerEntity?>(null)
    val selectedCustomer: StateFlow<CustomerEntity?> = _selectedCustomer.asStateFlow()

    private val _posSearchQuery = MutableStateFlow("")
    val posSearchQuery: StateFlow<String> = _posSearchQuery.asStateFlow()

    private val _posSelectedLine = MutableStateFlow<String?>(null)
    val posSelectedLine: StateFlow<String?> = _posSelectedLine.asStateFlow()

    // Dialogs & Receipts
    private val _showCheckoutDialog = MutableStateFlow(false)
    val showCheckoutDialog: StateFlow<Boolean> = _showCheckoutDialog.asStateFlow()

    private val _activeReceiptText = MutableStateFlow<String?>(null)
    val activeReceiptText: StateFlow<String?> = _activeReceiptText.asStateFlow()

    private val _showUserSwitcherDialog = MutableStateFlow(false)
    val showUserSwitcherDialog: StateFlow<Boolean> = _showUserSwitcherDialog.asStateFlow()

    init {
        // Set default customer to PUBLICO EN GENERAL when customers load
        viewModelScope.launch {
            customers.collect { list ->
                if (_selectedCustomer.value == null && list.isNotEmpty()) {
                    _selectedCustomer.value = list.firstOrNull { it.name.contains("PUBLICO", ignoreCase = true) } ?: list.first()
                }
            }
        }
    }

    fun setScreen(screen: MainScreen) {
        _currentScreen.value = screen
    }

    fun switchUser(user: User) {
        SystemUsers.currentUser = user
        _currentUser.value = user
        _showUserSwitcherDialog.value = false
    }

    fun setShowUserSwitcher(show: Boolean) {
        _showUserSwitcherDialog.value = show
    }

    fun setPosSearchQuery(query: String) {
        _posSearchQuery.value = query
    }

    fun setPosSelectedLine(line: String?) {
        _posSelectedLine.value = line
    }

    fun setSelectedCustomer(customer: CustomerEntity) {
        _selectedCustomer.value = customer
    }

    // Cart Management
    fun addToCart(product: ProductEntity, quantity: Double = 1.0) {
        val current = _cart.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val item = current[index]
            current[index] = item.copy(quantity = item.quantity + quantity)
        } else {
            current.add(CartItem(product = product, quantity = quantity))
        }
        _cart.value = current
    }

    fun updateCartQuantity(productId: Long, newQuantity: Double) {
        if (newQuantity <= 0) {
            removeFromCart(productId)
            return
        }
        val current = _cart.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            current[index] = current[index].copy(quantity = newQuantity)
            _cart.value = current
        }
    }

    fun updateCartPriceTier(productId: Long, tier: PriceTier) {
        val current = _cart.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            current[index] = current[index].copy(selectedPriceTier = tier)
            _cart.value = current
        }
    }

    fun removeFromCart(productId: Long) {
        _cart.value = _cart.value.filterNot { it.product.id == productId }
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    fun addProductByCodeOrBarcode(code: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val product = repository.findProductByCode(code.trim())
            if (product != null) {
                addToCart(product, 1.0)
                onResult(true, "Añadido: ${product.description}")
            } else {
                onResult(false, "No se encontró artículo con código '$code'")
            }
        }
    }

    fun openCheckout() {
        if (_cart.value.isNotEmpty()) {
            _showCheckoutDialog.value = true
        }
    }

    fun closeCheckout() {
        _showCheckoutDialog.value = false
    }

    fun showReceipt(receipt: String) {
        _activeReceiptText.value = receipt
    }

    fun dismissReceipt() {
        _activeReceiptText.value = null
    }

    fun completeCheckout(
        paymentMethod: String,
        amountReceived: Double,
        notes: String
    ) {
        val cust = _selectedCustomer.value ?: return
        val items = _cart.value
        if (items.isEmpty()) return

        viewModelScope.launch {
            val (sale, saleItems) = repository.completeSale(
                items = items,
                customer = cust,
                cashierName = _currentUser.value.username,
                paymentMethod = paymentMethod,
                amountReceived = amountReceived,
                notes = notes
            )
            clearCart()
            _showCheckoutDialog.value = false

            // Format POS-80C Ticket and show
            val ticket = com.example.util.TicketFormatter.formatSaleTicket(sale, saleItems)
            showReceipt(ticket)
        }
    }

    // Customer Payment (Abono)
    fun processCustomerPayment(
        customer: CustomerEntity,
        amount: Double,
        notes: String
    ) {
        viewModelScope.launch {
            val folio = repository.registerCustomerPayment(
                customer = customer,
                amount = amount,
                cashierName = _currentUser.value.username,
                notes = notes
            )
            val newBalance = (customer.currentBalance - amount).coerceAtLeast(0.0)
            val receipt = com.example.util.TicketFormatter.formatPaymentReceipt(
                customer = customer,
                amount = amount,
                newBalance = newBalance,
                cashierName = _currentUser.value.username,
                folio = folio
            )
            showReceipt(receipt)
        }
    }

    // Stock Adjustment
    fun adjustStock(
        productId: Long,
        type: String,
        quantity: Double,
        reason: String
    ) {
        viewModelScope.launch {
            repository.adjustInventory(
                productId = productId,
                type = type,
                quantityDelta = quantity,
                reason = reason,
                userName = _currentUser.value.username
            )
        }
    }

    // Add or Edit Product
    fun saveProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.saveProduct(product)
        }
    }

    // Add or Edit Customer
    fun saveCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.saveCustomer(customer)
        }
    }

    // Cash Cut
    fun performCashCut(session: CashSessionEntity, countedCash: Double) {
        viewModelScope.launch {
            val closed = repository.closeCashSession(session, countedCash)
            val ticket = com.example.util.TicketFormatter.formatCashCutTicket(closed, isFinalZ = true)
            showReceipt(ticket)
        }
    }

    fun generatePartialCutX(session: CashSessionEntity, countedCash: Double) {
        val updated = session.copy(countedCash = countedCash)
        val ticket = com.example.util.TicketFormatter.formatCashCutTicket(updated, isFinalZ = false)
        showReceipt(ticket)
    }

    fun openNewSession(initialFund: Double) {
        viewModelScope.launch {
            repository.openNewCashSession(_currentUser.value.username, initialFund)
        }
    }

    fun reprintSaleTicket(sale: SaleEntity) {
        viewModelScope.launch {
            val items = repository.getSaleItemsSync(sale.id)
            val ticket = com.example.util.TicketFormatter.formatSaleTicket(sale, items)
            showReceipt(ticket)
        }
    }
}
