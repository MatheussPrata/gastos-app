package com.matheus.gastos

import android.app.Application
import androidx.work.WorkManager
import com.matheus.gastos.worker.BudgetAlertWorker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GastosApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Schedule periodic budget alert checks
        BudgetAlertWorker.schedulePeriodicCheck(this)
    }
}