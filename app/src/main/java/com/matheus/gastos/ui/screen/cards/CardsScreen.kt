package com.matheus.gastos.ui.screen.cards

import android.content.Context
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
import com.matheus.gastos.data.entity.CreditCard
import com.matheus.gastos.data.entity.Transaction
import com.matheus.gastos.ui.components.AmountText
import com.matheus.gastos.ui.components.CardItem
import com.matheus.gastos.ui.components.EmptyState
import com.matheus.gastos.ui.theme.GastosTheme
import com.matheus.gastos.viewmodel.CreditCardViewModel
import com.matheus.gastos.viewmodel.TransactionViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun CardsScreen(
    viewModel: CreditCardViewModel,
    transactionViewModel: TransactionViewModel,
    onAddCard: () -> Unit,
    onEditCard: (Long) -> Unit
) {
    val activeCards by viewModel.activeCards.collectAsStateWithLifecycle()
    val allCards by viewModel.allCards.collectAsStateWithLifecycle()
    
    var showAddCard by remember { mutableStateOf(false) }
    var selectedMonth by remember { mutableStateOf(System.currentTimeMillis()) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header with add button
        TopAppBar(
            title = { Text("Cartões de Crédito") },
            actions = {
                IconButton(onClick = { showAddCard = true }) {
                    Icon(
                        imageVector = androidx.compose.material.icons.filled.Icons.Default.Add,
                        contentDescription = "Adicionar cartão"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow
            )
        )

        if (activeCards.isEmpty()) {
            EmptyState(
                icon = androidx.compose.material.icons.filled.Icons.Default.CreditCard,
                title = "Nenhum cartão cadastrado",
                message = "Toque no botão + para adicionar seu primeiro cartão de crédito",
                actionLabel = "Adicionar Cartão",
                onActionClick = { showAddCard = true }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(activeCards) { card ->
                    CardItem(
                        card = card,
                        currentMonthSpent = getCurrentMonthSpent(card.id, selectedMonth),
                        onClick = { onEditCard(card.id) },
                        onLongClick = { /* show options */ }
                    )
                }
            }
        }
    }

    // Add/Edit Card Dialog would be shown via showAddCard state
    // In a real implementation, this would be a Dialog or BottomSheet
}

@Composable
fun CardItem(
    card: CreditCard,
    currentMonthSpent: Double,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val limit = card.limit
    val available = limit - currentMonthSpent
    val usagePercent = if (limit > 0) (currentMonthSpent / limit * 100) else 0f
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp)
            .fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = card.color.toColor()
        ),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = card.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = String.format(Locale.getDefault(), "Fechamento: dia %02d", card.closingDay),
                    fontSize = 12.sp,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
            
            // Progress bar for limit usage
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Limite",
                        fontSize = 14.sp,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%.0f%% usado", usagePercent),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                    )
                }
                
                androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
                
                androidx.compose.material3.LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    progress = usagePercent / 100f,
                    color = if (usagePercent >= 90) androidx.compose.material3.MaterialTheme.colorScheme.error
                        else if (usagePercent >= 70) androidx.compose.material3.MaterialTheme.colorScheme.warning
                        else androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    trackColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
            }
            
            androidx.compose.foundation.layout.Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Gasto este mês",
                        fontSize = 12.sp,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "R$ %.2f", currentMonthSpent),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Disponível",
                        fontSize = 12.sp,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "R$ %.2f", available),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (available >= 0) androidx.compose.material3.MaterialTheme.colorScheme.tertiary
                            else androidx.compose.material3.MaterialTheme.colorScheme.error
                    )
                }
            }
            
            androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                Text(
                    text = String.format(Locale.getDefault(), "Limite total: R$ %.2f", limit),
                    fontSize = 12.sp,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = String.format(Locale.getDefault(), "Vencimento: dia %02d", card.dueDay),
                    fontSize = 12.sp,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

// Helper function
fun getCurrentMonthSpent(cardId: Long, selectedMonth: Long): Double {
    // In real implementation, this would come from TransactionViewModel
    return 0.0
}

// Extension for Color
fun Int.toColor(): androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color(this)