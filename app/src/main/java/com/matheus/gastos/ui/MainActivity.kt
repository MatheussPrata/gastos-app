package com.matheus.gastos.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import com.matheus.gastos.ui.navigation.AppNavHost
import com.matheus.gastos.ui.navigation.Screen
import com.matheus.gastos.ui.screen.add_card.AddCardScreen
import com.matheus.gastos.ui.screen.add_expense.AddExpenseScreen
import com.matheus.gastos.ui.screen.categories.CategoriesScreen
import com.matheus.gastos.ui.screen.cards.CardsScreen
import com.matheus.gastos.ui.screen.expenses.ExpensesScreen
import com.matheus.gastos.ui.screen.main.MainScreen
import com.matheus.gastos.ui.screen.reports.ReportsScreen
import com.matheus.gastos.ui.screen.settings.SettingsScreen
import com.matheus.gastos.ui.theme.GastosTheme
import com.matheus.gastos.viewmodel.CategoryViewModel
import com.matheus.gastos.viewmodel.CreditCardViewModel
import com.matheus.gastos.viewmodel.SettingsViewModel
import com.matheus.gastos.viewmodel.TransactionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val transactionViewModel: TransactionViewModel by viewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()
    private val creditCardViewModel: CreditCardViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GastosTheme(
                darkTheme = settingsViewModel.themeMode == 2,
                dynamicColor = false,
                accentColor = settingsViewModel.accentColor
            ) {
                Surface(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    AppNavHost(
                        navController = rememberNavController(),
                        onNavigateToAddExpense = { 
                            navController.navigate(Screen.AddExpense.route) 
                        },
                        onNavigateToEditExpense = { expenseId ->
                            navController.navigate("${Screen.EditExpense.route.replace("{expenseId}", expenseId.toString())}")
                        },
                        onNavigateToAddCard = { 
                            navController.navigate(Screen.AddCard.route) 
                        },
                        onNavigateToEditCard = { cardId ->
                            navController.navigate("${Screen.EditCard.route.replace("{cardId}", cardId.toString())}")
                        },
                        onNavigateToCategories = { 
                            navController.navigate(Screen.Categories.route) 
                        }
                    )
                }
            }
        }
    }
}