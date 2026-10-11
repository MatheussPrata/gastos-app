package com.matheus.gastos.ui.screen.expenses

import android.app.DatePickerDialog
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
fun ExpensesScreen(
    viewModel: TransactionViewModel,
    categoryViewModel: com.matheus.gastos.viewmodel.CategoryViewModel,
    creditCardViewModel: com.matheus.gastos.viewmodel.CreditCardViewModel,
    onAddExpense: () -> Unit,
    onEditExpense: (Long) -> Unit
) {
    val transactions by viewModel.currentMonthTransactions.collectAsStateWithLifecycle()
    val totalExpenses by viewModel.totalExpenses.collectAsStateWithLifecycle()
    val totalIncome by viewModel.totalIncome.collectAsStateWithLifecycle()
    val balance by viewModel.balance.collectAsStateWithLifecycle()
    
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Summary Card
        SummaryCard(
            totalExpenses = totalExpenses,
            totalIncome = totalIncome,
            balance = balance,
            onDateClick = { showDatePicker = true }
        )

        Divider()

        // Transactions List
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header with date and add button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(selectedDate)),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Button(onClick = onAddExpense) {
                    Icon(
                        imageVector = androidx.compose.material.icons.filled.Icons.Default.Add,
                        contentDescription = "Adicionar"
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.width(8.dp))
                    Text("Novo Gasto")
                }
            }

            if (transactions.isEmpty()) {
                EmptyState(
                    icon = androidx.compose.material.icons.filled.Icons.Default.ReceiptLong,
                    title = "Nenhum gasto este mês",
                    message = "Toque no botão + para adicionar seu primeiro gasto"
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transactions) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onClick = { onEditExpense(transaction.id) },
                            categoryName = getCategoryName(transaction.categoryId),
                            categoryColor = getCategoryColor(transaction.categoryId),
                            cardName = getCardName(transaction.creditCardId),
                            cardColor = getCardColor(transaction.creditCardId)
                        )
                    }
                }
            }
        }
    }

    // Date picker dialog
    if (showDatePicker) {
        DatePickerDialogFragment(
            initialDate = selectedDate,
            onDateSelected = { date ->
                selectedDate = date
                viewModel.setSelectedMonth(date)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
fun SummaryCard(
    totalExpenses: Double,
    totalIncome: Double,
    balance: Double,
    onDateClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Resumo do Mês",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
                )
                IconButton(onClick = onDateClick) {
                    Icon(
                        imageVector = androidx.compose.material.icons.filled.Icons.Default.CalendarMonth,
                        contentDescription = "Mudar mês",
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    label = "Gastos",
                    amount = totalExpenses,
                    isExpense = true,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.error
                )
                SummaryItem(
                    label = "Receitas",
                    amount = totalIncome,
                    isExpense = false,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.tertiary
                )
                SummaryItem(
                    label = "Saldo",
                    amount = balance,
                    isExpense = balance < 0,
                    color = if (balance >= 0) androidx.compose.material3.MaterialTheme.colorScheme.tertiary else androidx.compose.material3.MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun SummaryItem(
    label: String,
    amount: Double,
    isExpense: Boolean,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = String.format(Locale.getDefault(), "R$ %.2f", amount),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

// Helper functions (in real app these would come from ViewModels)
fun getCategoryName(categoryId: Long?): String {
    return "Categoria" // Placeholder
}

fun getCategoryColor(categoryId: Long?): Int {
    return 0xFF6750A4 // Placeholder
}

fun getCardName(cardId: Long?): String? {
    return null // Placeholder
}

fun getCardColor(cardId: Long?): Int {
    return 0xFF2196F3 // Placeholder
}

// Date Picker Dialog Fragment
class DatePickerDialogFragment(
    initialDate: Long,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    init {
        val context = androidx.compose.ui.platform.LocalContext.current
        val cal = Calendar.getInstance()
        cal.timeInMillis = initialDate
        
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val selectedCal = Calendar.getInstance()
                selectedCal.set(year, month, day, 0, 0, 0)
                selectedCal.set(Calendar.MILLISECOND, 0)
                onDateSelected(selectedCal.timeInMillis)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
        
        // Dismiss callback would need DialogInterface.OnDismissListener
    }
}