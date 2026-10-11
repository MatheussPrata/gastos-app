package com.matheus.gastos.ui.screen.categories

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
import com.matheus.gastos.data.entity.Category
import com.matheus.gastos.data.entity.Subcategory
import com.matheus.gastos.ui.components.AmountText
import com.matheus.gastos.ui.components.CategoryChip
import com.matheus.gastos.ui.components.DateText
import com.matheus.gastos.ui.components.EmptyState
import com.matheus.gastos.ui.components.TransactionItem
import com.matheus.gastos.ui.theme.GastosTheme
import com.matheus.gastos.viewmodel.CategoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CategoriesScreen(
    categoryViewModel: CategoryViewModel,
    onClose: () -> Unit
) {
    val categories by categoryViewModel.categories.collectAsStateWithLifecycle()
    val defaultCategories by categoryViewModel.defaultCategories.collectAsStateWithLifecycle()
    val customCategories by categoryViewModel.customCategories.collectAsStateWithLifecycle()
    
    var showAddCategory by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<Category?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Categorias") },
            navigationIcon = {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = androidx.compose.material.icons.filled.Icons.Default.Close,
                        contentDescription = "Fechar"
                    )
                }
            },
            actions = {
                IconButton(onClick = { showAddCategory = true }) {
                    Icon(
                        imageVector = androidx.compose.material.icons.filled.Icons.Default.Add,
                        contentDescription = "Nova categoria"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Default Categories Section
            if (defaultCategories.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Categorias Padrão",
                        subtitle = "Não podem ser excluídas, apenas editadas"
                    )
                }
                items(defaultCategories) { category ->
                    CategoryItem(
                        category = category,
                        isDefault = true,
                        onEdit = { editingCategory = it },
                        onDelete = { /* Cannot delete defaults */ },
                        categoryViewModel = categoryViewModel
                    )
                }
            }

            // Custom Categories Section
            item {
                SectionHeader(
                    title = "Suas Categorias",
                    subtitle = "${customCategories.size} categorias personalizadas"
                )
            }
            
            if (customCategories.isEmpty()) {
                item {
                    EmptyState(
                        icon = androidx.compose.material.icons.filled.Icons.Default.Category,
                        title = "Nenhuma categoria personalizada",
                        message = "Toque no + para criar sua primeira categoria"
                    )
                }
            } else {
                items(customCategories) { category ->
                    CategoryItem(
                        category = category,
                        isDefault = false,
                        onEdit = { editingCategory = it },
                        onDelete = { showDeleteConfirm = it },
                        categoryViewModel = categoryViewModel
                    )
                }
            }
        }
    }

    // Add/Edit Category Dialog
    if (showAddCategory || editingCategory != null) {
        CategoryDialog(
            category = editingCategory,
            onSave = { name, color, icon, isDefault ->
                val newCategory = editingCategory?.copy(
                    name = name,
                    color = color,
                    icon = icon
                ) ?: Category(
                    id = 0,
                    name = name,
                    color = color,
                    icon = icon,
                    isDefault = isDefault
                )
                if (editingCategory != null) {
                    categoryViewModel.update(newCategory)
                } else {
                    categoryViewModel.insert(newCategory)
                }
                showAddCategory = false
                editingCategory = null
            },
            onDismiss = {
                showAddCategory = false
                editingCategory = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirm != null) {
        DeleteConfirmDialog(
            categoryName = showDeleteConfirm!!.name,
            onConfirm = {
                categoryViewModel.delete(showDeleteConfirm!!)
                showDeleteConfirm = null
            },
            onDismiss = { showDeleteConfirm = null }
        )
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
        )
        Divider()
    }
}

@Composable
fun CategoryItem(
    category: Category,
    isDefault: Boolean,
    onEdit: (Category) -> Unit,
    onDelete: (Category) -> Unit,
    categoryViewModel: CategoryViewModel
) {
    val subcategories = remember { mutableStateOf<List<Subcategory>>(emptyList()) }
    
    // Load subcategories
    androidx.compose.runtime.LaunchedEffect(category.id) {
        categoryViewModel.getSubcategories(category.id).collect { subs ->
            subcategories.value = subs
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(category.color.toColor(), RoundedCornerShape(8.dp))
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.width(12.dp))
                    Column {
                        Text(category.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        if (isDefault) {
                            Text("Padrão", fontSize = 10.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onEdit(category) }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.filled.Icons.Default.Edit,
                            contentDescription = "Editar"
                        )
                    }
                    if (!isDefault) {
                        IconButton(onClick = { onDelete(category) }) {
                            Icon(
                                imageVector = androidx.compose.material.icons.filled.Icons.Default.Delete,
                                contentDescription = "Excluir",
                                tint = androidx.compose.material3.MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Subcategories
            if (subcategories.value.isNotEmpty()) {
                Divider()
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    subcategories.value.forEach { sub ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.foundation.layout.Spacer(Modifier.width(52.dp))
                            Text(sub.name, fontSize = 16.sp)
                            IconButton(onClick = { /* Edit subcategory */ }) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.filled.Icons.Default.Edit,
                                    contentDescription = "Editar subcategoria"
                                )
                            }
                            IconButton(onClick = { /* Delete subcategory */ }) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.filled.Icons.Default.Delete,
                                    contentDescription = "Excluir subcategoria",
                                    tint = androidx.compose.material3.MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    // Add subcategory button
                    Button(
                        onClick = { /* Show add subcategory dialog */ },
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.filled.Icons.Default.Add,
                            contentDescription = "Adicionar"
                        )
                        androidx.compose.foundation.layout.Spacer(Modifier.width(8.dp))
                        Text("Adicionar Subcategoria")
                    }
                }
            }
        }
    }
}

// Category Dialog
@Composable
fun CategoryDialog(
    category: Category?,
    onSave: (String, Int, String, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var color by remember { mutableStateOf(category?.color ?: 0xFF6750A4) }
    var icon by remember { mutableStateOf(category?.icon ?: "category") }
    var isDefault by remember { mutableStateOf(category?.isDefault ?: false) }
    var error by remember { mutableStateOf<String?>(null) }

    val colors = remember {
        listOf(
            0xFF6750A4, 0xFF2196F3, 0xFF4CAF50, 0xFFFF9800,
            0xFFF44336, 0xFF9C27B0, 0xFF009688, 0xFFE91E63,
            0xFF3F51B5, 0xFF00BCD4, 0xFF8BC34A, 0xFFFFC107,
            0xFF795548, 0xFF607D8B, 0xFFFF5722, 0xFF673AB7
        )
    }

    val icons = remember {
        listOf(
            "category", "restaurant", "directions_bus", "shopping_cart",
            "movie", "local_hospital", "school", "receipt",
            "fitness_center", "pets", "directions_car", "flight",
            "hotel", "local_gas_station", "pharmacy", "store"
        )
    }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (category != null) "Editar Categoria" else "Nova Categoria") },
        text = {
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da categoria") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                error?.let { msg ->
                    Text(text = msg, color = androidx.compose.material3.MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                Text("Cor:", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(colors) { c ->
                        val isSelected = c == color
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .size(if (isSelected) 44.dp else 36.dp)
                                .background(c.toColor(), RoundedCornerShape(18.dp))
                                .border(if (isSelected) 2.dp else 0.dp, androidx.compose.material3.MaterialTheme.colorScheme.onSurface, RoundedCornerShape(18.dp))
                                .padding(if (isSelected) 4.dp else 0.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.filled.Icons.Default.Check,
                                    contentDescription = "Selecionado",
                                    tint = if (c == 0xFFFFFFFF || c == 0xFF000000) Color.Black else Color.White
                                )
                            }
                        }
                        .clickable { color = c }
                    }
                }

                Text("Ícone:", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(icons) { i ->
                        val isSelected = i == icon
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer else androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(8.dp))
                                .border(if (isSelected) 2.dp else 0.dp, androidx.compose.material3.MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getIconForName(i),
                                contentDescription = i,
                                tint = if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        .clickable { icon = i }
                    }
                }

                if (category == null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Categoria padrão do sistema", fontSize = 14.sp)
                        Checkbox(
                            checked = isDefault,
                            onCheckedChange = { isDefault = it }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        error = "Digite um nome"
                        return@Button
                    }
                    error = null
                    onSave(name, color, icon, isDefault)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
        }
    )
}

@Composable
fun DeleteConfirmDialog(
    categoryName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Excluir Categoria") },
        text = { Text("Tem certeza que deseja excluir \"$categoryName\"? Todas as subcategorias também serão removidas.") },
        confirmButton = {
            Button(
                onClick = { onConfirm() },
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.error
                )
            ) { Text("Excluir") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
        }
    )
}

// Helper to get icon
fun getIconForName(name: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (name) {
        "restaurant" -> androidx.compose.material.icons.filled.Icons.Default.Restaurant
        "directions_bus" -> androidx.compose.material.icons.filled.Icons.Default.DirectionsBus
        "shopping_cart" -> androidx.compose.material.icons.filled.Icons.Default.ShoppingCart
        "movie" -> androidx.compose.material.icons.filled.Icons.Default.Movie
        "local_hospital" -> androidx.compose.material.icons.filled.Icons.Default.LocalHospital
        "school" -> androidx.compose.material.icons.filled.Icons.Default.School
        "receipt" -> androidx.compose.material.icons.filled.Icons.Default.Receipt
        "fitness_center" -> androidx.compose.material.icons.filled.Icons.Default.FitnessCenter
        "pets" -> androidx.compose.material.icons.filled.Icons.Default.Pets
        "directions_car" -> androidx.compose.material.icons.filled.Icons.Default.DirectionsCar
        "flight" -> androidx.compose.material.icons.filled.Icons.Default.Flight
        "hotel" -> androidx.compose.material.icons.filled.Icons.Default.Hotel
        "local_gas_station" -> androidx.compose.material.icons.filled.Icons.Default.LocalGasStation
        "pharmacy" -> androidx.compose.material.icons.filled.Icons.Default.Pharmacy
        "store" -> androidx.compose.material.icons.filled.Icons.Default.Store
        else -> androidx.compose.material.icons.filled.Icons.Default.Category
    }
}

fun Int.toColor(): androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color(this)