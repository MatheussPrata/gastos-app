package com.matheus.gastos.ui.screen.reports

import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matheus.gastos.R
import com.matheus.gastos.data.entity.Transaction
import com.matheus.gastos.ui.components.AmountText
import com.matheus.gastos.ui.components.CategoryChip
import com.matheus.gastos.ui.components.DateText
import com.matheus.gastos.ui.components.EmptyState
import com.matheus.gastos.ui.components.TransactionItem
import com.matheus.gastos.ui.theme.GastosTheme
import com.matheus.gastos.viewmodel.TransactionViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ReportsScreen(
    transactionViewModel: TransactionViewModel,
    categoryViewModel: com.matheus.gastos.viewmodel.CategoryViewModel,
    creditCardViewModel: com.matheus.gastos.viewmodel.CreditCardViewModel
) {
    val expensesByCategory by transactionViewModel.expensesByCategory.collectAsStateWithLifecycle()
    val expensesByCard by transactionViewModel.expensesByCard.collectAsStateWithLifecycle()
    val totalExpenses by transactionViewModel.totalExpenses.collectAsStateWithLifecycle()
    val totalIncome by transactionViewModel.totalIncome.collectAsStateWithLifecycle()
    val balance by transactionViewModel.balance.collectAsStateWithLifecycle()
    val selectedMonth by transactionViewModel.selectedMonth.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(0) } // 0 = Category, 1 = Card, 2 = Trends
    val tabs = listOf("Por Categoria", "Por Cartão", "Tendências")

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Relatórios") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow
            )
        )

        // Summary cards
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryCard(
                    title = "Total Gasto",
                    amount = totalExpenses,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                    icon = androidx.compose.material.icons.filled.Icons.Default.TrendingDown
                )
                SummaryCard(
                    title = "Total Receita",
                    amount = totalIncome,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.tertiary,
                    icon = androidx.compose.material.icons.filled.Icons.Default.TrendingUp
                )
                SummaryCard(
                    title = "Saldo",
                    amount = balance,
                    color = if (balance >= 0) androidx.compose.material3.MaterialTheme.colorScheme.tertiary
                        else androidx.compose.material3.MaterialTheme.colorScheme.error,
                    icon = androidx.compose.material.icons.filled.Icons.Default.AccountBalanceWallet
                )
            }
        }

        // Tab selection
        androidx.compose.material3.ScrollableTabRow(
            selectedTabIndex = selectedTab,
            onTabSelected = { selectedTab = it },
            modifier = Modifier.fillMaxWidth(),
            indicator = { tabPositions ->
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .width(tabPositions[selectedTab].width)
                        .height(3.dp)
                        .graphicsLayer { translationX = tabPositions[selectedTab].left }
                        .background(androidx.compose.material3.MaterialTheme.colorScheme.primary)
                )
            },
            divider = { Divider(color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp) }
        ) {
            tabs.forEachIndexed { index, title ->
                androidx.compose.material3.Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        // Tab content
        androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                0 -> CategoryBreakdown(expensesByCategory)
                1 -> CardBreakdown(expensesByCard)
                2 -> TrendsChart()
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    amount: Double,
    color: androidx.compose.ui.graphics.Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier
            .weight(1f, fill = true)
            .padding(0.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = String.format(Locale.getDefault(), "R$ %.2f", amount),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun CategoryBreakdown(expensesByCategory: List<com.matheus.gastos.viewmodel.TransactionViewModel.CategoryExpense>) {
    val categories = remember { mutableStateOf(expensesByCategory) }
    categories.value = expensesByCategory
    
    val total = categories.value.sumOf { it.total }
    
    if (categories.value.isEmpty()) {
        EmptyState(
            icon = androidx.compose.material.icons.filled.Icons.Default.PieChart,
            title = "Nenhum gasto por categoria",
            message = "Os gastos aparecerão aqui conforme você adicionar transações"
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories.value) { item ->
            val percentage = if (total > 0) (item.total / total * 100) else 0f
            CategoryBreakdownItem(
                categoryName = getCategoryName(item.categoryId),
                categoryColor = getCategoryColor(item.categoryId),
                amount = item.total,
                percentage = percentage
            )
        }
    }
}

@Composable
fun CategoryBreakdownItem(
    categoryName: String,
    categoryColor: Int,
    amount: Double,
    percentage: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(categoryColor.toColor(), RoundedCornerShape(6.dp))
                )
                androidx.compose.foundation.layout.Spacer(Modifier.width(12.dp))
                Column {
                    Text(categoryName, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f%% do total", percentage),
                        fontSize = 12.sp,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format(Locale.getDefault(), "R$ %.2f", amount),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                androidx.compose.material3.LinearProgressIndicator(
                    modifier = Modifier
                        .width(80.dp)
                        .padding(top = 4.dp),
                    progress = percentage / 100f,
                    color = categoryColor.toColor()
                )
            }
        }
    }
}

@Composable
fun CardBreakdown(expensesByCard: List<com.matheus.gastos.viewmodel.TransactionViewModel.CardExpense>) {
    val cards = remember { mutableStateOf(expensesByCard) }
    cards.value = expensesByCard
    
    val total = cards.value.sumOf { it.total }
    
    if (cards.value.isEmpty()) {
        EmptyState(
            icon = androidx.compose.material.icons.filled.Icons.Default.CreditCard,
            title = "Nenhum gasto por cartão",
            message = "Os gastos aparecerão aqui conforme você usar seus cartões"
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(cards.value) { item ->
            val percentage = if (total > 0) (item.total / total * 100) else 0f
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(getCardColor(item.creditCardId).toColor(), RoundedCornerShape(8.dp))
                        )
                        androidx.compose.foundation.layout.Spacer(Modifier.width(12.dp))
                        Column {
                            Text(getCardName(item.creditCardId), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f%% do total", percentage),
                                fontSize = 12.sp,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = String.format(Locale.getDefault(), "R$ %.2f", item.total),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        androidx.compose.material3.LinearProgressIndicator(
                            modifier = Modifier
                                .width(80.dp)
                                .padding(top = 4.dp),
                            progress = percentage / 100f,
                            color = getCardColor(item.creditCardId).toColor()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TrendsChart() {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Gráfico de Tendências", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Em desenvolvimento - será implementado com compose-charts", 
                fontSize = 14.sp, 
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
            
            // Placeholder for chart
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(8.dp))
            ) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📊", fontSize = 48.sp)
                }
            }
        }
    }
}

// Helper functions
fun getCategoryName(categoryId: Long): String {
    return "Categoria $categoryId" // Placeholder
}

fun getCategoryColor(categoryId: Long): Int {
    val colors = intArrayOf(
        0xFF6750A4, 0xFF4ECDC4, 0xFFFF6B6B, 0xFFFFD93D, 0xFF6BCB77,
        0xFF8E8EFF, 0xFFB347FF, 0xFFB8B8B8
    )
    return colors[(categoryId % colors.size).toInt()]
}

fun getCardName(cardId: Long): String {
    return "Cartão $cardId" // Placeholder
}

fun getCardColor(cardId: Long): Int {
    val colors = intArrayOf(0xFF2196F3, 0xFF4CAF50, 0xFFFF9800, 0xFFF44336, 0xFF9C27B0, 0xFF009688)
    return colors[(cardId % colors.size).toInt()]
}

fun Int.toColor(): androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color(this)