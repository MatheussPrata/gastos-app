package com.matheus.gastos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import com.matheus.gastos.ui.screen.add_expense.AddExpenseScreen
import com.matheus.gastos.ui.screen.add_card.AddCardScreen
import com.matheus.gastos.ui.screen.categories.CategoriesScreen
import com.matheus.gastos.ui.screen.cards.CardsScreen
import com.matheus.gastos.ui.screen.expenses.ExpensesScreen
import com.matheus.gastos.ui.screen.main.MainScreen
import com.matheus.gastos.ui.screen.reports.ReportsScreen
import com.matheus.gastos.ui.screen.settings.SettingsScreen

sealed class Screen(val route: String, val title: String, val icon: String) {
    object Expenses : Screen("expenses", "Gastos", "ic_expenses")
    object Cards : Screen("cards", "Cartões", "ic_cards")
    object Reports : Screen("reports", "Relatórios", "ic_reports")
    object Settings : Screen("settings", "Configurações", "ic_settings")
    object AddExpense : Screen("add_expense", "Novo Gasto", "")
    object EditExpense : Screen("edit_expense/{expenseId}", "Editar Gasto", "")
    object AddCard : Screen("add_card", "Novo Cartão", "")
    object EditCard : Screen("edit_card/{cardId}", "Editar Cartão", "")
    object Categories : Screen("categories", "Categorias", "")
}

@Composable
fun rememberNavControllerWithState(): NavHostController {
    return rememberNavController()
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String = Screen.Expenses.route,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToEditExpense: (Long) -> Unit,
    onNavigateToAddCard: () -> Unit,
    onNavigateToEditCard: (Long) -> Unit,
    onNavigateToCategories: () -> Unit
) {
    androidx.navigation.compose.NavHost(navController, startDestination) {
        composable(Screen.Expenses.route) {
            MainScreen(
                currentScreen = Screen.Expenses,
                onNavigateToAddExpense = onNavigateToAddExpense,
                onNavigateToEditExpense = onNavigateToEditExpense,
                onNavigateToAddCard = onNavigateToAddCard,
                onNavigateToEditCard = onNavigateToEditCard,
                onNavigateToCategories = onNavigateToCategories
            )
        }
        composable(Screen.Cards.route) {
            MainScreen(
                currentScreen = Screen.Cards,
                onNavigateToAddExpense = onNavigateToAddExpense,
                onNavigateToEditExpense = onNavigateToEditExpense,
                onNavigateToAddCard = onNavigateToAddCard,
                onNavigateToEditCard = onNavigateToEditCard,
                onNavigateToCategories = onNavigateToCategories
            )
        }
        composable(Screen.Reports.route) {
            MainScreen(
                currentScreen = Screen.Reports,
                onNavigateToAddExpense = onNavigateToAddExpense,
                onNavigateToEditExpense = onNavigateToEditExpense,
                onNavigateToAddCard = onNavigateToAddCard,
                onNavigateToEditCard = onNavigateToEditCard,
                onNavigateToCategories = onNavigateToCategories
            )
        }
        composable(Screen.Settings.route) {
            MainScreen(
                currentScreen = Screen.Settings,
                onNavigateToAddExpense = onNavigateToAddExpense,
                onNavigateToEditExpense = onNavigateToEditExpense,
                onNavigateToAddCard = onNavigateToAddCard,
                onNavigateToEditCard = onNavigateToEditCard,
                onNavigateToCategories = onNavigateToCategories
            )
        }
        composable(
            route = Screen.AddExpense.route,
            arguments = listOf()
        ) {
            AddExpenseScreen(
                onSave = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.EditExpense.route,
            arguments = listOf(navArgument("expenseId") { type = androidx.navigation.navTypeLongType })
        ) { backStackEntry ->
            val expenseId = backStackEntry.getLong("expenseId")
            AddExpenseScreen(
                expenseId = expenseId,
                onSave = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.AddCard.route,
            arguments = listOf()
        ) {
            AddCardScreen(
                onSave = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.EditCard.route,
            arguments = listOf(navArgument("cardId") { type = androidx.navigation.navTypeLongType })
        ) { backStackEntry ->
            val cardId = backStackEntry.getLong("cardId")
            AddCardScreen(
                cardId = cardId,
                onSave = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(Screen.Categories.route) {
            CategoriesScreen(
                onClose = { navController.popBackStack() }
            )
        }
    }
}

// Extension for getting long from nav args
import androidx.navigation.getLong
fun androidx.navigation.NavBackStackEntry.getLong(key: String): Long {
    return getArguments().getLong(key, 0L)
}