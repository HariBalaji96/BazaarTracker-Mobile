package com.example.bazaartracker

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Sales : Screen("sales", "Sales", Icons.Default.PointOfSale)
    object Vendors : Screen("vendors", "Vendors", Icons.Default.People)
    object Products : Screen("products", "Products", Icons.Default.ShoppingCart)
    object Expenses : Screen("expenses", "Expenses", Icons.Default.MoneyOff)
    object Payments : Screen("payments", "Payments", Icons.Default.Payments)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onLogout: () -> Unit) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    val context = LocalContext.current
    val sessionKey = remember { ApiClient.getToken(context) ?: "default" }
    
    val dashboardViewModel: DashboardViewModel = viewModel(key = "db_$sessionKey")
    val productViewModel: ProductViewModel = viewModel(key = "prod_$sessionKey")
    val vendorViewModel: VendorViewModel = viewModel(key = "vend_$sessionKey")
    val expenseViewModel: ExpenseViewModel = viewModel(key = "exp_$sessionKey")
    val salesViewModel: SalesViewModel = viewModel(key = "sale_$sessionKey")
    val paymentViewModel: PaymentViewModel = viewModel(key = "pay_$sessionKey")
    val profileViewModel: ProfileViewModel = viewModel(key = "prof_$sessionKey")

    // We can only fit so many in the bottom bar, let's pick the top 5
    val navItems = listOf(
        Screen.Dashboard,
        Screen.Products,
        Screen.Sales,
        Screen.Vendors,
        Screen.Expenses
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bazaar Tracker", color = Color.White) },
                actions = {
                    // Quick access to Payments and Profile
                    IconButton(onClick = { currentScreen = Screen.Payments }) {
                        Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = "Payments", tint = Color.White)
                    }
                    IconButton(onClick = { currentScreen = Screen.Profile }) {
                        Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "Profile", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E1E1E),
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1E1E1E),
                contentColor = Color.White
            ) {
                navItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentScreen == screen,
                        onClick = { currentScreen = screen },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color(0xFF2C2C2C)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier.padding(innerPadding),
            color = Color(0xFF121212)
        ) {
            when (currentScreen) {
                is Screen.Dashboard -> DashboardScreen(dashboardViewModel)
                is Screen.Products -> ProductListScreen(productViewModel)
                is Screen.Sales -> SalesScreen(salesViewModel, productViewModel, vendorViewModel)
                is Screen.Vendors -> VendorScreen(vendorViewModel)
                is Screen.Expenses -> ExpenseScreen(expenseViewModel)
                is Screen.Payments -> PaymentScreen(paymentViewModel, vendorViewModel)
                is Screen.Profile -> ProfileScreen(profileViewModel) {
                    dashboardViewModel.resetState()
                    productViewModel.resetState()
                    vendorViewModel.resetState()
                    expenseViewModel.resetState()
                    salesViewModel.resetState()
                    paymentViewModel.resetState()
                    profileViewModel.resetState()
                    Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    onLogout()
                }
            }
        }
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(text = name, color = Color.White, fontSize = 24.sp)
    }
}
