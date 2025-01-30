package com.example.swipeapp

import android.app.Application
import androidx.work.*
import com.example.swipeapp.work.ProductSyncWorker
import java.util.concurrent.TimeUnit

class SwipeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        setupPeriodicSync()
    }

    private fun setupPeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicSync = PeriodicWorkRequestBuilder<ProductSyncWorker>(
            15, TimeUnit.MINUTES,
            5, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "periodic_product_sync",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicSync
            )
    }
} 