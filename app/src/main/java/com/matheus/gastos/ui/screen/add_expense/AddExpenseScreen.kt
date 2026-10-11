package com.matheus.gastos.ui.screen.add_expense

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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import java.util.UUID

@Composable
fun AddExpenseScreen(
    expenseId: Long? = null,
    categoryViewModel: com.matheus.gastos.viewmodel.CategoryViewModel,
    creditCardViewModel: com.matheus.gastos.viewmodel.CreditCardViewModel,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val categories by categoryViewModel.categories.collectAsStateWithLifecycle()
    val cards by creditCardViewModel.activeCards.collectAsStateWithLifecycle()
    
    // Form state
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedSubcategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedCardId by remember { mutableStateOf<Long?>(null) }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var isExpense by remember { mutableStateOf(true) }
    var isInstallment by remember { mutableStateOf(false) }
    var installments by remember { mutableStateOf(1) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showCardPicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // If editing, load existing data
    // TODO: Load transaction data if expenseId != null

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(if (expenseId != null) "Editar Gasto" else "Novo Gasto") },
            navigationIcon = {
                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = androidx.compose.material.icons.filled.Icons.Default.Close,
                        contentDescription = "Cancelar"
                    )
                }
            },
            actions = {
                if (expenseId != null) {
                    IconButton(onClick = { /* Delete confirmation */ }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.filled.Icons.Default.Delete,
                            contentDescription = "Excluir"
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow
            )
        )

        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Amount field
            AmountInputField(
                amount = amount,
                onAmountChange = { amount = it },
                isExpense = isExpense,
                onExpenseTypeChange = { isExpense = it }
            )

            // Description field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descrição (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Category selector
            CategorySelector(
                categories = categories,
                selectedCategoryId = selectedCategoryId,
                selectedSubcategoryId = selectedSubcategoryId,
                onCategoryClick = { showCategoryPicker = true },
                categoryViewModel = categoryViewModel
            )

            // Date selector
            DateSelector(
                selectedDate = selectedDate,
                onClick = { showDatePicker = true }
            )

            // Card selector (only for expenses)
            if (isExpense) {
                CardSelector(
                    cards = cards,
                    selectedCardId = selectedCardId,
                    onClick = { showCardPicker = true }
                )
            }

            // Installment option
            InstallmentOption(
                isInstallment = isInstallment,
                installments = installments,
                onInstallmentChange = { isInstallment = it },
                onInstallmentsChange = { installments = it }
            )

            // Error message
            errorMessage?.let { msg ->
                Text(
                    text = msg,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }

            // Save button
            Button(
                onClick = {
                    validateAndSave()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (expenseId != null) "Salvar Alterações" else "Salvar Gasto",
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }

    // Category Picker Bottom Sheet
    if (showCategoryPicker) {
        CategoryPickerBottomSheet(
            categories = categories,
            selectedCategoryId = selectedCategoryId,
            selectedSubcategoryId = selectedSubcategoryId,
            onCategorySelected = { catId, subId ->
                selectedCategoryId = catId
                selectedSubcategoryId = subId
                showCategoryPicker = false
            },
            onDismiss = { showCategoryPicker = false },
            categoryViewModel = categoryViewModel
        )
    }

    // Card Picker Bottom Sheet
    if (showCardPicker) {
        CardPickerBottomSheet(
            cards = cards,
            selectedCardId = selectedCardId,
            onCardSelected = { cardId ->
                selectedCardId = cardId
                showCardPicker = false
            },
            onDismiss = { showCardPicker = false }
        )
    }

    // Date Picker
    if (showDatePicker) {
        DatePickerDialogFragment(
            initialDate = selectedDate,
            onDateSelected = { date ->
                selectedDate = date
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    fun validateAndSave() {
        if (amount.isBlank() || amount.toDoubleOrNull() == null || amount.toDouble() <= 0) {
            errorMessage = "Digite um valor válido"
            return
        }
        if (selectedCategoryId == null && isExpense) {
            errorMessage = "Selecione uma categoria"
            return
        }
        
        errorMessage = null
        
        // Create transaction(s)
        val amountValue = amount.toDouble()
        val transaction = Transaction(
            id = expenseId ?: 0,
            amount = amountValue,
            description = description,
            date = selectedDate,
            categoryId = selectedCategoryId,
            subcategoryId = selectedSubcategoryId,
            creditCardId = if (isExpense) selectedCardId else null,
            isExpense = isExpense,
            isInstallment = isInstallment && installments > 1,
            installmentNumber = 1,
            totalInstallments = if (isInstallment) installments else 1,
            installmentGroupId = if (isInstallment && installments > 1) UUID.randomUUID().toString() else null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        // TODO: Save via ViewModel
        // For now just navigate back
        onSave()
    }
}

@Composable
fun AmountInputField(
    amount: String,
    onAmountChange: (String) -> Unit,
    isExpense: Boolean,
    onExpenseTypeChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Expense/Income toggle
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Tipo:", fontSize = 16.sp)
            androidx.compose.foundation.layout.Spacer(Modifier.width(8.dp))
            androidx.compose.material3.SegmentedButton(
                selected = isExpense,
                onClick = { onExpenseTypeChange(true) },
                modifier = Modifier
            ) {
                Text("Gasto", color = if (isExpense) androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
            }
            androidx.compose.material3.SegmentedButton(
                selected = !isExpense,
                onClick = { onExpenseTypeChange(false) },
                modifier = Modifier
            ) {
                Text("Receita", color = if (!isExpense) androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        
        androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
        
        // Amount input
        androidx.compose.material3.TextField(
            value = amount,
            onValueChange = { onAmountChange(it.filter { it.isDigit() || it == '.' || it == ',' }.replace(',', '.')) },
            label = { Text("Valor") },
            leadingIcon = { Text("R$ ", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
            singleLine = true,
            keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                imeAction = androidx.compose.ui.text.input.ImeAction.Next
            ),
            modifier = Modifier.width(120.dp)
        )
    }
}

@Composable
fun CategorySelector(
    categories: List<com.matheus.gastos.data.entity.Category>,
    selectedCategoryId: Long?,
    selectedSubcategoryId: Long?,
    onCategoryClick: () -> Unit,
    categoryViewModel: com.matheus.gastos.viewmodel.CategoryViewModel
) {
    val selectedCategory = categories.find { it.id == selectedCategoryId }
    val subcategories = if (selectedCategory != null) {
        // Get subcategories for selected category
        emptyList<com.matheus.gastos.data.entity.Subcategory>() // Would come from ViewModel
    } else emptyList()

    OutlinedTextField(
        value = selectedCategory?.name ?: "",
        onValueChange = {},
        label = { Text("Categoria") },
        readOnly = true,
        trailingIcon = {
            Icon(
                imageVector = androidx.compose.material.icons.filled.Icons.Default.ExpandMore,
                contentDescription = "Selecionar categoria"
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onCategoryClick() }
    )
    
    if (selectedCategory != null && subcategories.isNotEmpty()) {
        OutlinedTextField(
            value = subcategories.find { it.id == selectedSubcategoryId }?.name ?: "",
            onValueChange = {},
            label = { Text("Subcategoria") },
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = androidx.compose.material.icons.filled.Icons.Default.ExpandMore,
                    contentDescription = "Selecionar subcategoria"
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .clickable { /* Show subcategory picker */ }
        )
    }
}

@Composable
fun DateSelector(
    selectedDate: Long,
    onClick: () -> Unit
) {
    val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(selectedDate))
    
    OutlinedTextField(
        value = dateStr,
        onValueChange = {},
        label = { Text("Data") },
        readOnly = true,
        trailingIcon = {
            Icon(
                imageVector = androidx.compose.material.icons.filled.Icons.Default.CalendarMonth,
                contentDescription = "Selecionar data"
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
    )
}

@Composable
fun CardSelector(
    cards: List<com.matheus.gastos.data.entity.CreditCard>,
    selectedCardId: Long?,
    onClick: () -> Unit
) {
    val selectedCard = cards.find { it.id == selectedCardId }
    
    OutlinedTextField(
        value = selectedCard?.name ?: "",
        onValueChange = {},
        label = { Text("Cartão de Crédito") },
        readOnly = true,
        trailingIcon = {
            Icon(
                imageVector = androidx.compose.material.icons.filled.Icons.Default.ExpandMore,
                contentDescription = "Selecionar cartão"
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
    )
}

@Composable
fun InstallmentOption(
    isInstallment: Boolean,
    installments: Int,
    onInstallmentChange: (Boolean) -> Unit,
    onInstallmentsChange: (Int) -> Unit
) {
    androidx.compose.material3.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isInstallment,
            onCheckedChange = onInstallmentChange,
            modifier = Modifier.padding(start = 4.dp)
        )
        Text("Parcelado", fontSize = 16.sp)
        
        if (isInstallment) {
            androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
            Text("${installments}x", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            androidx.compose.material3.IconButton(onClick = { if (installments > 1) onInstallmentsChange(installments - 1) }) {
                Icon(imageVector = androidx.compose.material.icons.filled.Icons.Default.Remove, contentDescription = "Menos parcelas")
            }
            androidx.compose.material3.IconButton(onClick = { if (installments < 24) onInstallmentsChange(installments + 1) }) {
                Icon(imageVector = androidx.compose.material.icons.filled.Icons.Default.Add, contentDescription = "Mais parcelas")
            }
        }
    }
}

// Category Picker Bottom Sheet
@Composable
fun CategoryPickerBottomSheet(
    categories: List<com.matheus.gastos.data.entity.Category>,
    selectedCategoryId: Long?,
    selectedSubcategoryId: Long?,
    onCategorySelected: (Long?, Long?) -> Unit,
    onDismiss: () -> Unit,
    categoryViewModel: com.matheus.gastos.viewmodel.CategoryViewModel
) {
    androidx.compose.material3.ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(initialValue = androidx.compose.material3.BottomSheetValue.Collapsed),
        sheetContent = {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Selecionar Categoria", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Divider()
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable { onCategorySelected(category.id, null) },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                androidx.compose.foundation.layout.Box(
                                    modifier = Modifier.size(16.dp)
                                        .background(category.color.toColor(), RoundedCornerShape(8.dp))
                                )
                                androidx.compose.foundation.layout.Spacer(Modifier.width(12.dp))
                                Text(category.name, fontSize = 16.sp)
                            }
                            if (selectedCategoryId == category.id) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.filled.Icons.Default.Check,
                                    contentDescription = "Selecionado",
                                    tint = androidx.compose.material3.MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancelar")
                }
            }
        }
    )
}

// Card Picker Bottom Sheet
@Composable
fun CardPickerBottomSheet(
    cards: List<com.matheus.gastos.data.entity.CreditCard>,
    selectedCardId: Long?,
    onCardSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(initialValue = androidx.compose.material3.BottomSheetValue.Collapsed),
        sheetContent = {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Selecionar Cartão", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Divider()
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(cards) { card ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable { onCardSelected(card.id) },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                androidx.compose.foundation.layout.Box(
                                    modifier = Modifier.size(40.dp)
                                        .background(card.color.toColor(), RoundedCornerShape(8.dp))
                                )
                                androidx.compose.foundation.layout.Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(card.name, fontSize = 16.sp)
                                    Text(
                                        text = String.format(Locale.getDefault(), "Limite: R$ %.2f | Fecha dia %02d", card.limit, card.closingDay),
                                        fontSize = 12.sp,
                                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (selectedCardId == card.id) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.filled.Icons.Default.Check,
                                    contentDescription = "Selecionado",
                                    tint = androidx.compose.material3.MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    // Option for no card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { onCardSelected(null) },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sem cartão (dinheiro/PIX)", fontSize = 16.sp)
                        if (selectedCardId == null) {
                            Icon(
                                imageVector = androidx.compose.material.icons.filled.Icons.Default.Check,
                                contentDescription = "Selecionado",
                                tint = androidx.compose.material3.MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancelar")
                }
            }
        }
    )
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
        
        android.app.DatePickerDialog(
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
    }
}

// Helper functions
fun Int.toColor(): androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color(this)

// Remember modal bottom sheet state
fun rememberModalBottomSheetState(initialValue: androidx.compose.material3.BottomSheetValue) =
    androidx.compose.material3.rememberModalBottomSheetState(initialValue)