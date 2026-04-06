package com.ovulation.health.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ovulation.health.OvulationHealthApp
import com.ovulation.health.data.model.AdminReport
import timber.log.Timber
import java.time.LocalDateTime

/**
 * Kept for backward-compat with the morning alarm trigger.
 * The main logic has moved to HourlyAnalysisWorker; this just
 * enqueues it immediately so the daily alarm still works.
 */
class DailyHealthTestWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Timber.d("DailyHealthTestWorker – delegating to HourlyAnalysisWorker")
        val req = androidx.work.OneTimeWorkRequestBuilder<HourlyAnalysisWorker>().build()
        androidx.work.WorkManager.getInstance(applicationContext).enqueue(req)
        return Result.success()
    }
}
