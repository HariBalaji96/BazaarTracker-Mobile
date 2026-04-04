package com.example.bazaartracker

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val state = viewModel.dashboardState

    LaunchedEffect(Unit) {
        viewModel.fetchDashboardData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF121212),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (state) {
                is DashboardState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                is DashboardState.Error -> {
                    Text(
                        text = state.message,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                is DashboardState.Success -> {
                    DashboardContent(state.data)
                }
            }
        }
    }
}

@Composable
fun DashboardContent(data: DashboardResponse) {
    val cards = listOf(
        DashboardCardData("Total Sales", data.totalSales, Icons.Default.TrendingUp, Color(0xFF4CAF50)),
        DashboardCardData("Total Credit", data.totalCredit, Icons.Default.AccountBalanceWallet, Color(0xFFFFC107)),
        DashboardCardData("Total Payments", data.totalPayments, Icons.Default.Payments, Color(0xFF2196F3)),
        DashboardCardData("Total Expenses", data.totalExpenses, Icons.Default.TrendingDown, Color(0xFFF44336)),
        DashboardCardData("Profit", data.profit, Icons.Default.AttachMoney, Color(0xFF8BC34A), isProfit = true)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(cards) { card ->
            DashboardCard(card)
        }
    }
}

data class DashboardCardData(
    val title: String,
    val value: Double,
    val icon: ImageVector,
    val color: Color,
    val isProfit: Boolean = false
)

@Composable
fun DashboardCard(card: DashboardCardData) {
    val animatedValue = remember { Animatable(0f) }
    
    LaunchedEffect(card.value) {
        animatedValue.animateTo(
            targetValue = card.value.toFloat(),
            animationSpec = tween(durationMillis = 1000)
        )
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color(0xFF1E1E1E)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = card.icon,
                    contentDescription = null,
                    tint = card.color,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Column {
                Text(
                    text = card.title,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "₹${String.format(Locale.getDefault(), "%.2f", animatedValue.value)}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (card.isProfit) {
                        if (card.value >= 0) Color(0xFF8BC34A) else Color(0xFFF44336)
                    } else Color.White
                )
            }
        }
    }
}
