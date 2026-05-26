package com.pocketguidance

import android.app.Application
import android.util.Log
import com.pocketguidance.data.db.AppDatabase
import com.pocketguidance.data.repository.AuthRepository
import com.pocketguidance.data.repository.FinanceRepository

class PocketGuidanceApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val authRepository by lazy { AuthRepository(database) }
    val financeRepository by lazy { FinanceRepository(database) }

    override fun onCreate() {
        super.onCreate()
        Log.i("PocketGuidanceApp", "Application started")
    }
}
