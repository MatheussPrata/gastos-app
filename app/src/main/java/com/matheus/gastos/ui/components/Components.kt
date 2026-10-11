package com.matheus.gastos.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import com.matheus.gastos.data.entity.Transaction
import com.matheus.gastos.ui.theme.GastosTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit,
    categoryName: String,
    categoryColor: Int,
    cardName: String?,
    cardColor: Int
) {
    val isExpense = transaction.isExpense
    val amountColor = if (isExpense) androidx.compose.material3.MaterialTheme.colorScheme.error
        else androidx.compose.material3.MaterialTheme.colorScheme.tertiary
    val amountPrefix = if (isExpense) "- " else "+ "
    val dateStr = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(transaction.date))
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp)
            .fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: category/icon + info
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Category color indicator
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(40.dp)
                        .background(categoryColor.toColor(), RoundedCornerShape(2.dp))
                )
                androidx.compose.foundation.layout.Spacer(Modifier.width(12.dp))
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = transaction.description.ifBlank { categoryName },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = dateStr,
                            fontSize = 12.sp,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (cardName != null) {
                            Text(
                                text = " · ",
                                fontSize = 12.sp,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(cardColor.toColor(), RoundedCornerShape(4.dp))
                            )
                            Text(
                                text = cardName,
                                fontSize = 12.sp,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (transaction.isInstallment) {
                            Text(
                                text = " · ${transaction.installmentNumber}/${transaction.totalInstallments}",
                                fontSize = 12.sp,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            
            // Right side: amount
            Text(
                text = String.format(Locale.getDefault(), "%sR$ %.2f", amountPrefix, transaction.amount),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}

@Composable
fun AmountText(
    amount: Double,
    isExpense: Boolean,
    fontSize: Int = 16
) {
    val color = if (isExpense) androidx.compose.material3.MaterialTheme.colorScheme.error
        else androidx.compose.material3.MaterialTheme.colorScheme.tertiary
    val prefix = if (isExpense) "- " else "+ "
    
    Text(
        text = String.format(Locale.getDefault(), "%sR$ %.2f", prefix, amount),
        fontSize = fontSize.sp,
        fontWeight = FontWeight.Bold,
        color = color
    )
}

@Composable
fun CategoryChip(
    name: String,
    color: Int,
    onClick: (() -> Unit)? = null
) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .background(color.toColor().copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable(onClick != null, onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.size(8.dp).background(color.toColor(), RoundedCornerShape(4.dp))
            )
            Text(name, fontSize = 12.sp, color = color.toColor())
        }
    }
}

@Composable
fun DateText(timestamp: Long) {
    val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
    Text(
        text = dateStr,
        fontSize = 14.sp,
        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = message,
            fontSize = 14.sp,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.TextAlign.Center
        )
        actionLabel?.let { label ->
            Button(onClick = onActionClick!!) {
                Text(label)
            }
        }
    }
}

@Composable
fun BudgetProgressBar(
    spent: Double,
    limit: Double,
    categoryColor: Int
) {
    val progress = if (limit > 0) (spent / limit).coerceIn(0f, 1f) else 0f
    val isOverLimit = spent > limit
    val isWarning = spent > limit * 0.8 && !isOverLimit
    
    val barColor = when {
        isOverLimit -> androidx.compose.material3.MaterialTheme.colorScheme.error
        isWarning -> androidx.compose.material3.MaterialTheme.colorScheme.warning
        else -> categoryColor.toColor()
    }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = String.format(Locale.getDefault(), "R$ %.2f / R$ %.2f", spent, limit),
                fontSize = 12.sp,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = String.format(Locale.getDefault(), "%.0f%%", progress * 100),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = barColor
            )
        }
        
        androidx.compose.material3.LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth(),
            progress = progress,
            color = barColor,
            trackColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
        
        if (isOverLimit) {
            Text(
                text = "Limite excedido!",
                fontSize = 11.sp,
                color = androidx.compose.material3.MaterialTheme.colorScheme.error
            )
        } else if (isWarning) {
            Text(
                text = "Atenção: próximo do limite",
                fontSize = 11.sp,
                color = androidx.compose.material3.MaterialTheme.colorScheme.warning
            )
        }
    }
}

fun Int.toColor(): androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color(this)