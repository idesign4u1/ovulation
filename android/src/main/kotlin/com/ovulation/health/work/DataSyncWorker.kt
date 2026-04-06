package com.ovulation.health.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ovulation.health.OvulationHealthApp
import com.ovulation.health.network.OvulationHealthApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.time.LocalDateTime

/**
 * Worker that syncs unsynced health data with admin server
 */
class DataSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val database = OvulationHealthApp.database
    private val apiService = createApiService()

    override suspend fun doWork(): Result {
        return try {
            Timber.d("Starting data sync worker")

            // Get unsync reports from database
            val unsyncedReports = database.adminReportDao().getUnsynced()

            Timber.d("Found ${unsyncedReports.size} unsynced reports")

            for (report in unsyncedReports) {
                try {
                    // Upload report to server
                    val response = apiService.uploadHealthMetrics(report)

                    if (response.isSuccessful) {
                        // Mark report as synced
                        val syncedReport = report.copy(
                            synced = true,
                            syncedDate = LocalDateTime.now()
                        )
                        database.adminReportDao().update(syncedReport)

                        Timber.d("Report synced successfully: ${report.id}")

                        // Send success alert
                        sendSuccessAlert(report)

                    } else {
                        Timber.e("Failed to upload report: ${response.code()}")
                        return@try Result.retry()
                    }

                } catch (e: Exception) {
                    Timber.e(e, "Error syncing report ${report.id}")
                    return@try Result.retry()
                }
            }

            Timber.d("Data sync completed successfully")
            Result.success()

        } catch (e: Exception) {
            Timber.e(e, "Data sync failed")
            Result.retry()
        }
    }

    private suspend fun sendSuccessAlert(report: com.ovulation.health.data.model.AdminReport) {
        try {
            // Send notification to admin
            val alertRequest = com.ovulation.health.network.AlertRequest(
                userId = report.subjectId,
                alertType = "DATA_SYNC_SUCCESS",
                severity = "INFO",
                message = "Health data synced successfully",
                data = mapOf(
                    "report_id" to report.id,
                    "sync_date" to LocalDateTime.now().toString()
                )
            )

            val response = apiService.sendAlert(alertRequest)
            if (response.isSuccessful) {
                Timber.d("Success alert sent to server")
            }

        } catch (e: Exception) {
            Timber.e(e, "Failed to send success alert")
        }
    }

    private fun createApiService(): OvulationHealthApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(OvulationHealthApiService::class.java)
    }

    companion object {
        // Replace with actual server URL
        private const val API_BASE_URL = "https://health-api.example.com/"
    }
}
