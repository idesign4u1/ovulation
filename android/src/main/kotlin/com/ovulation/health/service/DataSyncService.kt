package com.ovulation.health.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.IBinder
import androidx.work.BackoffPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ovulation.health.OvulationHealthApp
import com.ovulation.health.work.DataSyncWorker
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Service for synchronizing health data with admin server
 */
class DataSyncService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("DataSyncService started")

        // Check if network is available
        if (isNetworkAvailable()) {
            scheduleDataSync()
        } else {
            Timber.d("Network not available, will retry when connected")
        }

        return START_STICKY
    }

    private fun scheduleDataSync() {
        val workRequest = OneTimeWorkRequestBuilder<DataSyncWorker>()
            .setBackoffPolicy(
                BackoffPolicy.EXPONENTIAL,
                15,
                TimeUnit.MINUTES
            )
            .build()

        WorkManager.getInstance(this).enqueueUniqueWork(
            "data_sync",
            androidx.work.ExistingWorkPolicy.KEEP,
            workRequest
        )

        Timber.d("Data sync work enqueued")
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        return activeNetwork != null
    }
}
