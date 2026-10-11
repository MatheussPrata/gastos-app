package com.matheus.gastos.ui.screen.add_card

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.matheus.gastos.data.entity.CreditCard
import com.matheus.gastos.ui.components.AmountText
import com.matheus.gastos.ui.components.CategoryChip
import com.matheus.gastos.ui.components.DateText
import com.matheus.gastos.ui.components.EmptyState
import com.matheus.gastos.ui.components.TransactionItem
import com.matheus.gastos.ui.theme.GastosTheme
import com.matheus.gastos.viewmodel.CreditCardViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AddCardScreen(
    cardId: Long? = null,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    // Form state
    var name by remember { mutableStateOf("") }
    var limit by remember { mutableStateOf("") }
    var closingDay by remember { mutableStateOf(1) }
    var dueDay by remember { mutableStateOf(10) }
    var selectedColor by remember { mutableStateOf(0xFF2196F3) }
    var isActive by remember { mutableStateOf(true) }
    var showColorPicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Predefined colors for cards
    val cardColors = remember {
        listOf(
            0xFF2196F3, // Blue
            0xFF4CAF50, // Green
            0xFFFF9800, // Orange
            0xFFF44336, // Red
            0xFF9C27B0, // Purple
            0xFF009688, // Teal
            0xFFE91E63, // Pink
            0xFF3F51B5, // Indigo
            0xFF795548, // Brown
            0xFF607D8B, // Blue Grey
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(if (cardId != null) "Editar Cartão" else "Novo Cartão") },
            navigationIcon = {
                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = androidx.compose.material.icons.filled.Icons.Default.Close,
                        contentDescription = "Cancelar"
                    )
                }
            },
            actions = {
                if (cardId != null) {
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
            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome do Cartão") },
                placeholder = { Text("Ex: Nubank, Itaú, C6") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Limit field
            OutlinedTextField(
                value = limit,
                onValueChange = { limit = it.filter { it.isDigit() || it == '.' || it == ',' }.replace(',', '.') },
                label = { Text("Limite") },
                leadingIcon = { Text("R$ ", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Next
                )
            )

            // Closing day and Due day
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DaySelector(
                    label = "Dia de Fechamento",
                    day = closingDay,
                    onDayChange = { closingDay = it },
                    maxDay = 28
                )
                DaySelector(
                    label = "Dia de Vencimento",
                    day = dueDay,
                    onDayChange = { dueDay = it },
                    maxDay = 28
                )
            }

            // Color picker
            ColorSelector(
                label = "Cor do Cartão",
                selectedColor = selectedColor,
                colors = cardColors,
                onColorClick = { showColorPicker = true }
            )

            // Active switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Cartão Ativo", fontSize = 16.sp)
                    Text("Desative para ocultar da lista principal", fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Checkbox(
                    checked = isActive,
                    onCheckedChange = { isActive = it }
                )
            }

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
                    text = if (cardId != null) "Salvar Alterações" else "Adicionar Cartão",
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }

    // Color Picker Dialog
    if (showColorPicker) {
        ColorPickerDialog(
            currentColor = selectedColor,
            colors = cardColors,
            onColorSelected = { color ->
                selectedColor = color
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }

    fun validateAndSave() {
        if (name.isBlank()) {
            errorMessage = "Digite o nome do cartão"
            return
        }
        if (limit.isBlank() || limit.toDoubleOrNull() == null || limit.toDouble() <= 0) {
            errorMessage = "Digite um limite válido"
            return
        }
        if (closingDay < 1 || closingDay > 28) {
            errorMessage = "Dia de fechamento deve ser entre 1 e 28"
            return
        }
        if (dueDay < 1 || dueDay > 28) {
            errorMessage = "Dia de vencimento deve ser entre 1 e 28"
            return
        }
        
        errorMessage = null
        
        // Create card
        val card = CreditCard(
            id = cardId ?: 0,
            name = name,
            color = selectedColor,
            limit = limit.toDouble(),
            closingDay = closingDay,
            dueDay = dueDay,
            isActive = isActive
        )
        
        // TODO: Save via ViewModel
        onSave()
    }
}

@Composable
fun DaySelector(
    label: String,
    day: Int,
    onDayChange: (Int) -> Unit,
    maxDay: Int
) {
    Column(
        modifier = Modifier
            .weight(1f, fill = true)
            .fillMaxWidth()
    ) {
        OutlinedTextField(
            value = day.toString(),
            onValueChange = { 
                val value = it.toIntOrNull()
                if (value != null && value >= 1 && value <= maxDay) {
                    onDayChange(value)
                }
            },
            label = { Text(label) },
            singleLine = true,
            keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ColorSelector(
    label: String,
    selectedColor: Int,
    colors: List<Int>,
    onColorClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            colors.forEach { color ->
                val isSelected = color == selectedColor
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(if (isSelected) 48.dp else 40.dp)
                        .background(color.toColor(), RoundedCornerShape(20.dp))
                        .border(if (isSelected) 3.dp else 0.dp, androidx.compose.material3.MaterialTheme.colorScheme.onSurface, RoundedCornerShape(20.dp))
                        .padding(if (isSelected) 4.dp else 0.dp)
                        .clickable { /* Handled by parent */ }
                        .fillMaxWidth()
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = androidx.compose.material.icons.filled.Icons.Default.Check,
                            contentDescription = "Selecionado",
                            tint = if (color == 0xFFFFFFFF || color == 0xFF000000) Color.Black else Color.White
                        )
                    }
                }
                .clickable { /* Handled by parent */ }
            }
        }
        .clickable { onColorClick() }
    }
}

// Color Picker Dialog
@Composable
fun ColorPickerDialog(
    currentColor: Int,
    colors: List<Int>,
    onColorSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Escolher Cor do Cartão") },
        text = { Text("Esta cor será usada para identificar o cartão no app") },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Cancelar") }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) { Text("OK") }
        }
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(colors) { color ->
                    val isSelected = color == currentColor
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .size(if (isSelected) 56.dp else 48.dp)
                            .background(color.toColor(), RoundedCornerShape(24.dp))
                            .border(if (isSelected) 3.dp else 0.dp, androidx.compose.material3.MaterialTheme.colorScheme.onSurface, RoundedCornerShape(24.dp))
                            .padding(if (isSelected) 4.dp else 0.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = androidx.compose.material.icons.filled.Icons.Default.Check,
                                contentDescription = "Selecionado",
                                tint = if (color == 0xFFFFFFFF || color == 0xFF000000) Color.Black else Color.White
                            )
                        }
                    }
                    .clickable { onColorSelected(color) }
                }
            }
        }
    }
}

fun Int.toColor(): androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color(this)