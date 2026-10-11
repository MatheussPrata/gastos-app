package com.matheus.gastos.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LiveData
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.matheus.gastos.R
import com.matheus.gastos.data.entity.BudgetLimit
import com.matheus.gastos.data.entity.Category
import com.matheus.gastos.data.entity.Transaction
import com.matheus.gastos.data.local.AppDatabase
import com.matheus.gastos.data.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class BudgetAlertWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val CHANNEL_ID = "budget_alerts"
    private val NOTIFICATION_ID_BASE = 1000

    override suspend fun doWork(): Result {
        // Check if notifications are enabled
        val settingsRepo = SettingsRepository(
            AppDatabase.getInstance(applicationContext).appSettingsDao()
        )
        val notificationsEnabled = settingsRepo.getBoolean("notifications_enabled", true).await()
        
        if (!notificationsEnabled) {
            return Result.success()
        }

        val db = AppDatabase.getInstance(applicationContext)
        val budgetRepo = com.matheus.gastos.data.repository.BudgetLimitRepository(
            db.budgetLimitDao(),
            com.matheus.gastos.data.repository.CategoryRepository(
                db.categoryDao(),
                db.subcategoryDao()
            )
        )
        val transactionRepo = com.matheus.gastos.data.repository.TransactionRepository(db.transactionDao())
        val categoryRepo = com.matheus.gastos.data.repository.CategoryRepository(
            db.categoryDao(),
            db.subcategoryDao()
        )

        // Get active budget limits
        val activeLimits = budgetRepo.getActive().first()
        
        if (activeLimits.isEmpty()) {
            return Result.success()
        }

        // Check each budget limit
        for (limitWithCategory in activeLimits) {
            val limit = limitWithCategory.limit
            val category = limitWithCategory.category
            
            category?.let { cat ->
                checkBudgetLimit(
                    limit = limit,
                    category = cat,
                    transactionRepo = transactionRepo,
                    categoryRepo = categoryRepo
                )
            }
        }

        return Result.success()
    }

    private suspend fun checkBudgetLimit(
        limit: BudgetLimit,
        category: Category,
        transactionRepo: com.matheus.gastos.data.repository.TransactionRepository,
        categoryRepo: com.matheus.gastos.data.repository.CategoryRepository
    ) {
        // Get date range based on period
        val (startDate, endDate) = when (limit.period) {
            com.matheus.gastos.data.entity.BudgetPeriod.WEEKLY -> TransactionRepository.getWeekBounds()
            com.matheus.gastos.data.entity.BudgetPeriod.MONTHLY -> TransactionRepository.getMonthBounds()
            com.matheus.gastos.data.entity.BudgetPeriod.YEARLY -> {
                val cal = Calendar.getInstance()
                cal.set(Calendar.MONTH, Calendar.JANUARY)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.add(Calendar.YEAR, 1)
                cal.add(Calendar.MILLISECOND, -1)
                val end = cal.timeInMillis
                start to end
            }
        }

        // Get total spent in this category for the period
        val expenses = transactionRepo.getByCategoryAndDateRange(category.id, startDate, endDate).first()
        val totalSpent = expenses.filter { it.isExpense }.sumOf { it.amount }

        val percentage = if (limit.amount > 0) (totalSpent / limit.amount * 100) else 0f
        val warningThreshold = limit.warningThreshold * 100

        // Check if limit exceeded
        if (totalSpent >= limit.amount) {
            sendNotification(
                title = "Limite de gasto atingido",
                body = "Você atingiu o limite de R$ %.2f para ${category.name}".format(limit.amount),
                categoryId = category.id
            )
        }
        // Check if warning threshold reached
        else if (percentage >= warningThreshold && percentage < 100) {
            sendNotification(
                title = "Perto do limite de gasto",
                body = "Você gastou %.0f%% do limite de R$ %.2f para ${category.name}".format(percentage, limit.amount),
                categoryId = category.id
            )
        }
    }

    private fun sendNotification(title: String, body: String, categoryId: Long) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alertas de Orçamento",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações quando limites de gasto são atingidos"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID_BASE + categoryId.toInt(), notification)
    }

    companion object {
        fun schedulePeriodicCheck(context: Context) {
            val workRequest = androidx.work.PeriodicWorkRequestBuilder<BudgetAlertWorker>(
                1, TimeUnit.HOURS // Check every hour
            )
                .setInitialDelay(15, TimeUnit.MINUTES)
                .addTag("budget_alert_check")
                .build()

            androidx.work.WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    "budget_alert_check",
                    androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )
        }

        fun cancelPeriodicCheck(context: Context) {
            androidx.work.WorkManager.getInstance(context)
                .cancelUniqueWork("budget_alert_check")
        }
    }
}