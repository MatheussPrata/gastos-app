package com.matheus.gastos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.matheus.gastos.data.entity.Category
import com.matheus.gastos.data.entity.Subcategory
import com.matheus.gastos.data.entity.CreditCard
import com.matheus.gastos.data.entity.Transaction
import com.matheus.gastos.data.entity.BudgetLimit
import com.matheus.gastos.data.repository.CategoryRepository
import com.matheus.gastos.data.repository.CreditCardRepository
import com.matheus.gastos.data.repository.TransactionRepository
import com.matheus.gastos.data.repository.BudgetLimitRepository
import com.matheus.gastos.data.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sharingStarted

class CategoryViewModel(application: Application, private val repo: CategoryRepository) : AndroidViewModel(application) {
    val categories: LiveData<List<Category>> = repo.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    val defaultCategories: LiveData<List<Category>> = repo.getDefaults().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    val customCategories: LiveData<List<Category>> = repo.getCustom().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    suspend fun insert(category: Category) = repo.insert(category)
    suspend fun update(category: Category) = repo.update(category)
    suspend fun delete(category: Category) = repo.delete(category)
    suspend fun deleteById(id: Long) = repo.deleteById(id)

    // Subcategories
    fun getSubcategories(categoryId: Long): Flow<List<Subcategory>> = repo.getSubcategories(categoryId)
    suspend fun insertSubcategory(sub: Subcategory) = repo.insertSubcategory(sub)
    suspend fun updateSubcategory(sub: Subcategory) = repo.updateSubcategory(sub)
    suspend fun deleteSubcategory(sub: Subcategory) = repo.deleteSubcategory(sub)
    suspend fun deleteSubcategoryById(id: Long) = repo.deleteSubcategoryById(id)
    suspend fun deleteByCategoryId(categoryId: Long) = repo.deleteByCategoryId(categoryId)
    suspend fun getSubcategoryById(id: Long) = repo.getSubcategoryById(id)
}

class CreditCardViewModel(application: Application, private val repo: CreditCardRepository) : AndroidViewModel(application) {
    val activeCards: LiveData<List<CreditCard>> = repo.getActive().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    val allCards: LiveData<List<CreditCard>> = repo.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    suspend fun insert(card: CreditCard) = repo.insert(card)
    suspend fun update(card: CreditCard) = repo.update(card)
    suspend fun delete(card: CreditCard) = repo.delete(card)
    suspend fun deleteById(id: Long) = repo.deleteById(id)
    suspend fun getById(id: Long) = repo.getById(id)
}

class TransactionViewModel(application: Application, private val repo: TransactionRepository) : AndroidViewModel(application) {
    private val _selectedMonth = MutableLiveData<Long>(System.currentTimeMillis())
    val selectedMonth: LiveData<Long> = _selectedMonth

    fun setSelectedMonth(timestamp: Long) {
        _selectedMonth.value = timestamp
    }

    val currentMonthTransactions: LiveData<List<Transaction>> = kotlinx.coroutines.flow
        .combine(
            selectedMonth.asFlow().distinctUntilChanged(),
            repo.getByDateRange(0, Long.MAX_VALUE) // We'll filter in the combine
        ) { month, allTransactions ->
            val (start, end) = TransactionRepository.getMonthBounds(java.util.Date(month))
            allTransactions.filter { it.date >= start && it.date <= end }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val totalExpenses: LiveData<Double> = kotlinx.coroutines.flow
        .combine(selectedMonth.asFlow().distinctUntilChanged()) { month ->
            val (start, end) = TransactionRepository.getMonthBounds(java.util.Date(month))
            repo.getTotalExpenses(start, end)
        }
        .flattenMerge()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.0)

    val totalIncome: LiveData<Double> = kotlinx.coroutines.flow
        .combine(selectedMonth.asFlow().distinctUntilChanged()) { month ->
            val (start, end) = TransactionRepository.getMonthBounds(java.util.Date(month))
            repo.getTotalIncome(start, end)
        }
        .flattenMerge()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.0)

    val balance: LiveData<Double> = kotlinx.coroutines.flow
        .combine(totalIncome.asFlow(), totalExpenses.asFlow()) { income, expense -> income - expense }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.0)

    val expensesByCategory: LiveData<List<TransactionRepository.CategoryExpense>> = kotlinx.coroutines.flow
        .combine(selectedMonth.asFlow().distinctUntilChanged()) { month ->
            val (start, end) = TransactionRepository.getMonthBounds(java.util.Date(month))
            repo.getExpensesByCategory(start, end)
        }
        .flattenMerge()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val expensesByCard: LiveData<List<TransactionRepository.CardExpense>> = kotlinx.coroutines.flow
        .combine(selectedMonth.asFlow().distinctUntilChanged()) { month ->
            val (start, end) = TransactionRepository.getMonthBounds(java.util.Date(month))
            repo.getExpensesByCard(start, end)
        }
        .flattenMerge()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    suspend fun insert(transaction: Transaction) = repo.insert(transaction)
    suspend fun insertAll(transactions: List<Transaction>) = repo.insertAll(transactions)
    suspend fun update(transaction: Transaction) = repo.update(transaction)
    suspend fun delete(transaction: Transaction) = repo.delete(transaction)
    suspend fun deleteById(id: Long) = repo.deleteById(id)
    suspend fun deleteByInstallmentGroup(groupId: String) = repo.deleteByInstallmentGroup(groupId)
    suspend fun getById(id: Long) = repo.getById(id)

    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> = repo.getByDateRange(startDate, endDate)
    fun getByCategoryAndDateRange(categoryId: Long, startDate: Long, endDate: Long): Flow<List<Transaction>> = repo.getByCategoryAndDateRange(categoryId, startDate, endDate)
    fun getByCardAndDateRange(cardId: Long, startDate: Long, endDate: Long): Flow<List<Transaction>> = repo.getByCardAndDateRange(cardId, startDate, endDate)
    fun getFutureInstallments(currentDate: Long): Flow<List<Transaction>> = repo.getFutureInstallments(currentDate)
    fun getInstallmentsByGroup(groupId: String): Flow<List<Transaction>> = repo.getInstallmentsByGroup(groupId)

    // Helper to create installments
    suspend fun createInstallments(
        baseTransaction: Transaction,
        totalInstallments: Int,
        categoryId: Long?,
        subcategoryId: Long?,
        creditCardId: Long?
    ) {
        val groupId = UUID.randomUUID().toString()
        val installments = mutableListOf<Transaction>()
        
        for (i in 1..totalInstallments) {
            val cal = java.util.Calendar.getInstance()
            cal.timeInMillis = baseTransaction.date
            cal.add(java.util.Calendar.MONTH, i - 1)
            
            val installment = baseTransaction.copy(
                id = 0,
                installmentNumber = i,
                totalInstallments = totalInstallments,
                installmentGroupId = groupId,
                isInstallment = totalInstallments > 1,
                date = cal.timeInMillis,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            installments.add(installment)
        }
        repo.insertAll(installments)
    }
}

class BudgetLimitViewModel(application: Application, private val repo: BudgetLimitRepository) : AndroidViewModel(application) {
    val activeLimits: LiveData<List<BudgetLimitRepository.BudgetLimitWithCategory>> = 
        repo.getActiveWithCategories().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    suspend fun insert(limit: BudgetLimit) = repo.insert(limit)
    suspend fun update(limit: BudgetLimit) = repo.update(limit)
    suspend fun delete(limit: BudgetLimit) = repo.delete(limit)
    suspend fun deleteById(id: Long) = repo.deleteById(id)
    fun getByCategory(categoryId: Long): Flow<List<BudgetLimit>> = repo.getByCategory(categoryId)
    suspend fun getById(id: Long) = repo.getById(id)
}

class SettingsViewModel(application: Application, private val repo: SettingsRepository) : AndroidViewModel(application) {
    // Theme settings
    var themeMode: Int by mutableStateOf(0) // 0=system, 1=light, 2=dark
        private set
    
    var accentColor: Int by mutableStateOf(0xFF6750A4) // Default Material purple
        private set

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            themeMode = repo.getInt("theme_mode", 0)
            val colorInt = repo.getInt("accent_color", 0xFF6750A4)
            accentColor = colorInt
        }
    }

    suspend fun setThemeMode(mode: Int) {
        themeMode = mode
        repo.putInt("theme_mode", mode)
    }

    suspend fun setAccentColor(color: Int) {
        accentColor = color
        repo.putInt("accent_color", color)
    }

    // Notifications
    var notificationsEnabled: Boolean by mutableStateOf(true)
        private set

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        notificationsEnabled = enabled
        repo.putBoolean("notifications_enabled", enabled)
    }

    // Backup
    suspend fun setLastBackupTime(time: Long) = repo.putLong("last_backup_time", time)
    fun getLastBackupTime(): Long = repo.getLong("last_backup_time", 0)
}