package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainScreen
import com.example.ui.MainViewModel
import com.example.ui.components.HeaderBar
import com.example.ui.components.Pos80cReceiptDialog
import com.example.ui.components.UserSwitcherDialog
import com.example.ui.screens.CashCutScreen
import com.example.ui.screens.CustomersScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.PosScreen
import com.example.ui.screens.SalesHistoryScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyBluePrimary
import com.example.ui.theme.SafetyOrange

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

data class NavItem(
    val screen: MainScreen,
    val icon: ImageVector,
    val label: String
)

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val lowStockProducts by viewModel.lowStockProducts.collectAsState()
    val activeReceipt by viewModel.activeReceiptText.collectAsState()
    val showUserSwitcher by viewModel.showUserSwitcherDialog.collectAsState()

    val navItems = listOf(
        NavItem(MainScreen.POS, Icons.Default.ShoppingCart, "POS"),
        NavItem(MainScreen.INVENTORY, Icons.Default.Inventory, "Inventario"),
        NavItem(MainScreen.CUSTOMERS, Icons.Default.Group, "Clientes"),
        NavItem(MainScreen.CASH_CUT, Icons.Default.PointOfSale, "Caja"),
        NavItem(MainScreen.SALES_HISTORY, Icons.Default.ReceiptLong, "Historial")
    )

    Scaffold(
        topBar = {
            HeaderBar(
                currentUser = currentUser,
                lowStockCount = lowStockProducts.size,
                onUserClick = { viewModel.setShowUserSwitcher(true) }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                navItems.forEach { item ->
                    val isSelected = currentScreen == item.screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setScreen(item.screen) },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = NavyBluePrimary,
                            indicatorColor = NavyBluePrimary,
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B)
                        ),
                        modifier = Modifier.testTag("nav_tab_${item.screen.name}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                MainScreen.POS -> PosScreen(viewModel = viewModel)
                MainScreen.INVENTORY -> InventoryScreen(viewModel = viewModel)
                MainScreen.CUSTOMERS -> CustomersScreen(viewModel = viewModel)
                MainScreen.CASH_CUT -> CashCutScreen(viewModel = viewModel)
                MainScreen.SALES_HISTORY -> SalesHistoryScreen(viewModel = viewModel)
            }
        }
    }

    // Modal: Thermal Receipt Viewer
    activeReceipt?.let { receipt ->
        Pos80cReceiptDialog(
            receiptText = receipt,
            onDismiss = { viewModel.dismissReceipt() }
        )
    }

    // Modal: Quick User Switcher
    if (showUserSwitcher) {
        UserSwitcherDialog(
            currentUser = currentUser,
            onUserSelected = { user -> viewModel.switchUser(user) },
            onDismiss = { viewModel.setShowUserSwitcher(false) }
        )
    }
}
