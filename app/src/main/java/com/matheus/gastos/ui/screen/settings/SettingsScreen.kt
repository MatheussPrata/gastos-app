package com.matheus.gastos.ui.screen.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material3.ListItem
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
import com.matheus.gastos.ui.components.AmountText
import com.matheus.gastos.ui.components.CategoryChip
import com.matheus.gastos.ui.components.DateText
import com.matheus.gastos.ui.components.EmptyState
import com.matheus.gastos.ui.components.TransactionItem
import com.matheus.gastos.ui.theme.GastosTheme
import com.matheus.gastos.viewmodel.SettingsViewModel
import com.matheus.gastos.viewmodel.CategoryViewModel
import com.matheus.gastos.viewmodel.CreditCardViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    categoryViewModel: CategoryViewModel,
    creditCardViewModel: CreditCardViewModel,
    onNavigateToCategories: () -> Unit
) {
    val themeMode by settingsViewModel.themeMode
    val accentColor by settingsViewModel.accentColor
    val notificationsEnabled by settingsViewModel.notificationsEnabled
    
    var showColorPicker by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Configurações") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
        ) {
            // Theme Section
            SettingsSection(title = "Aparência") {
                SettingsItem(
                    title = "Tema",
                    subtitle = when (themeMode) {
                        0 -> "Sistema"
                        1 -> "Claro"
                        2 -> "Escuro"
                        else -> "Sistema"
                    },
                    icon = androidx.compose.material.icons.filled.Icons.Default.Palette,
                    onClick = { /* Show theme dialog */ }
                )
                
                SettingsItem(
                    title = "Cor de Destaque",
                    subtitle = "Toque para escolher",
                    icon = androidx.compose.material.icons.filled.Icons.Default.ColorLens,
                    trailing = {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(accentColor.toColor(), RoundedCornerShape(12.dp))
                        )
                    },
                    onClick = { showColorPicker = true }
                )
            }

            // Notifications Section
            SettingsSection(title = "Notificações") {
                SettingsItem(
                    title = "Avisos de Limite de Gasto",
                    subtitle = "Receba notificações quando atingir limites definidos",
                    icon = androidx.compose.material.icons.filled.Icons.Default.Notifications,
                    trailing = {
                        Checkbox(
                            checked = notificationsEnabled,
                            onCheckedChange = { settingsViewModel.setNotificationsEnabled(it) },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    },
                    onClick = { settingsViewModel.setNotificationsEnabled(!notificationsEnabled) }
                )
            }

            // Budget Limits Section
            SettingsSection(title = "Limites de Gasto") {
                SettingsItem(
                    title = "Gerenciar Limites",
                    subtitle = "Defina limites por categoria e período",
                    icon = androidx.compose.material.icons.filled.Icons.Default.Speed,
                    onClick = { /* Navigate to budget limits screen */ }
                )
            }

            // Categories Section
            SettingsSection(title = "Categorias") {
                SettingsItem(
                    title = "Gerenciar Categorias",
                    subtitle = "Adicione, edite ou remova categorias e subcategorias",
                    icon = androidx.compose.material.icons.filled.Icons.Default.Category,
                    onClick = onNavigateToCategories
                )
            }

            // Backup Section
            SettingsSection(title = "Backup e Restauração") {
                SettingsItem(
                    title = "Backup no Google Drive",
                    subtitle = "Faça backup automático dos seus dados na nuvem",
                    icon = androidx.compose.material.icons.filled.Icons.Default.CloudUpload,
                    onClick = { showBackupDialog = true }
                )
                
                SettingsItem(
                    title = "Exportar Dados (JSON)",
                    subtitle = "Salve um arquivo local com todos os seus dados",
                    icon = androidx.compose.material.icons.filled.Icons.Default.Download,
                    onClick = { showExportDialog = true }
                )
                
                SettingsItem(
                    title = "Importar Dados (JSON)",
                    subtitle = "Restaure dados a partir de um arquivo exportado",
                    icon = androidx.compose.material.icons.filled.Icons.Default.Upload,
                    onClick = { /* Show file picker for import */ }
                )
            }

            // About Section
            SettingsSection(title = "Sobre") {
                SettingsItem(
                    title = "Versão",
                    subtitle = "1.0.0",
                    icon = androidx.compose.material.icons.filled.Icons.Default.Info,
                    onClick = { /* Show about dialog */ }
                )
                
                SettingsItem(
                    title = "Código Aberto",
                    subtitle = "Ver no GitHub",
                    icon = androidx.compose.material.icons.filled.Icons.Default.Code,
                    onClick = { /* Open GitHub */ }
                )
            }
        }
    }

    // Color Picker Dialog
    if (showColorPicker) {
        ColorPickerDialog(
            currentColor = accentColor,
            onColorSelected = { color ->
                settingsViewModel.setAccentColor(color)
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }

    // Backup Dialog
    if (showBackupDialog) {
        BackupDialog(
            onBackup = { /* Trigger backup */ showBackupDialog = false },
            onRestore = { /* Trigger restore */ showBackupDialog = false },
            onDismiss = { showBackupDialog = false }
        )
    }

    // Export Dialog
    if (showExportDialog) {
        ExportDialog(
            onExport = { /* Trigger export */ showExportDialog = false },
            onDismiss = { showExportDialog = false }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title.uppercase(Locale.getDefault()),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp, bottom = 4.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    trailing: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    androidx.compose.material3.ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        headlineContent = { Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium) },
        supportingContent = { Text(subtitle, fontSize = 14.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        },
        trailingContent = trailing,
        onClick = onClick
    )
}

// Color Picker Dialog
@Composable
fun ColorPickerDialog(
    currentColor: Int,
    onColorSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = remember {
        listOf(
            0xFF6750A4, 0xFF2196F3, 0xFF4CAF50, 0xFFFF9800,
            0xFFF44336, 0xFF9C27B0, 0xFF009688, 0xFFE91E63,
            0xFF3F51B5, 0xFF00BCD4, 0xFF8BC34A, 0xFFFFC107,
            0xFF795548, 0xFF607D8B, 0xFFFF5722, 0xFF673AB7
        )
    }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Escolher Cor de Destaque") },
        text = { Text("Esta cor será usada como cor principal do app") },
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

// Backup Dialog
@Composable
fun BackupDialog(
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Backup no Google Drive") },
        text = { Text("Escolha uma ação:") },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = { onBackup(); onDismiss() },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Fazer Backup") }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(
                onClick = { onRestore(); onDismiss() },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Restaurar") }
        }
    )
}

// Export Dialog
@Composable
fun ExportDialog(
    onExport: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Exportar Dados") },
        text = { Text("Os dados serão salvos como arquivo JSON na pasta Documentos/GastosApp") },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = { onExport(); onDismiss() },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Exportar") }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Cancelar") }
        }
    )
}

fun Int.toColor(): androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color(this)