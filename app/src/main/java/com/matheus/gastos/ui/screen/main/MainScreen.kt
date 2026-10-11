package com.matheus.gastos.ui.screen.main

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomNavigation
import androidx.compose.material3.BottomNavigationItem
import androidx.compose.material3.Icon
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
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentDestination
import androidx.navigation.compose.getString
import androidx.navigation.compose.rememberNavController
import com.matheus.gastos.R
import com.matheus.gastos.ui.navigation.Screen
import com.matheus.gastos.ui.screen.add_card.AddCardScreen
import com.matheus.gastos.ui.screen.add_expense.AddExpenseScreen
import com.matheus.gastos.ui.screen.cards.CardsScreen
import com.matheus.gastos.ui.screen.categories.CategoriesScreen
import com.matheus.gastos.ui.screen.expenses.ExpensesScreen
import com.matheus.gastos.ui.screen.reports.ReportsScreen
import com.matheus.gastos.ui.screen.settings.SettingsScreen
import com.matheus.gastos.ui.theme.GastosTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matheus.gastos.viewmodel.CategoryViewModel
import com.matheus.gastos.viewmodel.CreditCardViewModel
import com.matheus.gastos.viewmodel.SettingsViewModel
import com.matheus.gastos.viewmodel.TransactionViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.factory
import android.app.Application

@AndroidEntryPoint
@Composable
fun MainScreen(
    currentScreen: Screen = Screen.Expenses,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToEditExpense: (Long) -> Unit,
    onNavigateToAddCard: () -> Unit,
    onNavigateToEditCard: (Long) -> Unit,
    onNavigateToCategories: () -> Unit
) {
    val navController = rememberNavController()
    val transactionViewModel: TransactionViewModel = viewModel()
    val categoryViewModel: CategoryViewModel = viewModel()
    val creditCardViewModel: CreditCardViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()
    
    var selectedScreen by remember { mutableStateOf(currentScreen) }
    
    val screens = listOf(Screen.Expenses, Screen.Cards, Screen.Reports, Screen.Settings)
    
    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(navController, startDestination = selectedScreen.route) {
            composable(Screen.Expenses.route) {
                ExpensesScreen(
                    viewModel = transactionViewModel,
                    categoryViewModel = categoryViewModel,
                    creditCardViewModel = creditCardViewModel,
                    onAddExpense = onNavigateToAddExpense,
                    onEditExpense = onNavigateToEditExpense
                )
            }
            composable(Screen.Cards.route) {
                CardsScreen(
                    viewModel = creditCardViewModel,
                    transactionViewModel = transactionViewModel,
                    onAddCard = onNavigateToAddCard,
                    onEditCard = onNavigateToEditCard
                )
            }
            composable(Screen.Reports.route) {
                ReportsScreen(
                    transactionViewModel = transactionViewModel,
                    categoryViewModel = categoryViewModel,
                    creditCardViewModel = creditCardViewModel
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    settingsViewModel = settingsViewModel,
                    categoryViewModel = categoryViewModel,
                    creditCardViewModel = creditCardViewModel,
                    onNavigateToCategories = onNavigateToCategories
                )
            }
            composable(
                route = "add_expense",
                arguments = listOf()
            ) {
                AddExpenseScreen(
                    categoryViewModel = categoryViewModel,
                    creditCardViewModel = creditCardViewModel,
                    onSave = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }
            composable(
                route = "edit_expense/{expenseId}",
                arguments = listOf(navArgument("expenseId") { type = androidx.navigation.navTypeLongType })
            ) { backStackEntry ->
                val expenseId = backStackEntry.getLong("expenseId")
                AddExpenseScreen(
                    expenseId = expenseId,
                    categoryViewModel = categoryViewModel,
                    creditCardViewModel = creditCardViewModel,
                    onSave = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }
            composable(
                route = "add_card",
                arguments = listOf()
            ) {
                AddCardScreen(
                    onSave = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }
            composable(
                route = "edit_card/{cardId}",
                arguments = listOf(navArgument("cardId") { type = androidx.navigation.navTypeLongType })
            ) { backStackEntry ->
                val cardId = backStackEntry.getLong("cardId")
                AddCardScreen(
                    cardId = cardId,
                    onSave = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }
            composable(route = "categories") {
                CategoriesScreen(
                    categoryViewModel = categoryViewModel,
                    onClose = { navController.popBackStack() }
                )
            }
        }
        
        // Bottom Navigation
        BottomNavigation(
            backgroundColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow,
            content = {
                screens.forEach { screen ->
                    val isSelected = selectedScreen == screen
                    BottomNavigationItem(
                        selected = isSelected,
                        onClick = { selectedScreen = screen },
                        icon = {
                            Icon(
                                painter = androidx.compose.material.icons.filled.Icons.Default.get(screen.icon),
                                contentDescription = screen.title,
                                tint = if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = { Text(screen.title) },
                        selectedContentColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        unselectedContentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }
}

// Helper for nav argument
import androidx.navigation.navArgument
fun navArgument(name: String, builder: androidx.navigation.NavArgumentBuilder.() -> Unit): androidx.navigation.NamedNavArgument {
    return androidx.navigation.NamedNavArgument(name, androidx.navigation.NavArgument(builder))
}