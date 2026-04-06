package com.ovulation.health.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.BackoffPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ovulation.health.work.DailyHealthTestWorker
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Broadcast receiver for triggering daily health tests
 */
class DailyTestReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.ovulation.DAILY_TEST") {
            Timber.d("Daily test broadcast received, scheduling work")

            // Schedule the daily test using WorkManager
            scheduleDailyTest(context)
        }
    }

    private fun scheduleDailyTest(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<DailyHealthTestWorker>()
            .setBackoffPolicy(
                BackoffPolicy.EXPONENTIAL,
                15,
                TimeUnit.MINUTES
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "daily_health_test",
            androidx.work.ExistingWorkPolicy.KEEP,
            workRequest
        )

        Timber.d("Daily health test work enqueued")
    }
}
