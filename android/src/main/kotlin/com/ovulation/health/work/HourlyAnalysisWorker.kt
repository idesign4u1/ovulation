package com.ovulation.health.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ovulation.health.OvulationHealthApp
import com.ovulation.health.data.model.DailyHealthSummary
import com.ovulation.health.data.model.OvulationPrediction
import com.ovulation.health.ml.PredictionEngine
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.LocalDateTime

/**
 * Runs every hour (enqueued by ContinuousMonitoringService).
 *
 * - Pulls the last 24 h of passively collected readings from the DB.
 * - Re-runs the prediction engine so predictions reflect the very
 *   latest touch/voice/camera data rather than just the morning snapshot.
 * - Triggers admin alerts if the composite score crosses key thresholds.
 */
class HourlyAnalysisWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val db = OvulationHealthApp.database
    private val predictionEngine = PredictionEngine()

    override suspend fun doWork(): Result {
        Timber.d("HourlyAnalysisWorker started")

        return try {
            val now = LocalDateTime.now()
            val since = now.minusHours(24)

            // ---- Collect latest sensor data ----
            val ferningList  = db.ferningAnalysisDao().getAnalysisInRange(since, now).first()
            val voiceList    = db.voiceAnalysisDao().getAnalysisInRange(since, now).first()
            val tempList     = db.temperatureDataDao().getDataInRange(since, now).first()
            val cardiacList  = db.cardiacDataDao().getDataInRange(since, now).first()

            Timber.d(
                "Data in last 24 h → ferning=${ferningList.size}, " +
                    "voice=${voiceList.size}, temp=${tempList.size}, cardiac=${cardiacList.size}"
            )

            if (voiceList.isEmpty() && tempList.isEmpty() && cardiacList.isEmpty()) {
                Timber.d("No passive data yet – nothing to analyse")
                return Result.success()
            }

            // ---- Run prediction engine ----
            val prediction = predictionEngine.predictOvulation(
                ferningData     = ferningList,
                voiceData       = voiceList,
                temperatureData = tempList,
                cardiacData     = cardiacList
            )
            db.ovulationPredictionDao().insert(prediction)
            Timber.d("Hourly prediction: score=${"%.2f".format(prediction.compositeScore)} " +
                "→ ${prediction.estimatedOvulationDate}")

            // ---- Implantation window ----
            val implWindow = predictionEngine.predictImplantationWindow(prediction, tempList)
            db.implantationWindowDao().insert(implWindow)

            // ---- Daily summary (upsert by date) ----
            val cycleDay = estimateCycleDay(now)
            val summary = DailyHealthSummary(
                date               = now,
                allTestsCompleted  = cardiacList.isNotEmpty() && voiceList.isNotEmpty(),
                ferningAnalysisId  = ferningList.lastOrNull()?.id,
                voiceAnalysisId    = voiceList.lastOrNull()?.id,
                temperatureDataId  = tempList.lastOrNull()?.id,
                cardiacDataId      = cardiacList.lastOrNull()?.id,
                cycleDay           = cycleDay,
                menstrualPhase     = estimatePhase(cycleDay)
            )
            db.dailyHealthSummaryDao().insert(summary)

            // ---- Alert admin if high-confidence ovulation ----
            if (prediction.compositeScore >= 0.85f) {
                enqueueAdminAlert(prediction)
            }

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "HourlyAnalysisWorker failed")
            Result.retry()
        }
    }

    private fun estimateCycleDay(now: LocalDateTime): Int {
        // In a real implementation, look up the last period start date from DB.
        return ((now.dayOfYear % 28) + 1).coerceIn(1, 35)
    }

    private fun estimatePhase(day: Int) = when (day) {
        in 1..5   -> DailyHealthSummary.MenstrualPhase.MENSTRUATION
        in 6..13  -> DailyHealthSummary.MenstrualPhase.FOLLICULAR
        in 14..16 -> DailyHealthSummary.MenstrualPhase.OVULATION
        else      -> DailyHealthSummary.MenstrualPhase.LUTEAL
    }

    private suspend fun enqueueAdminAlert(prediction: OvulationPrediction) {
        // Delegate to existing DataSyncWorker which already knows how to
        // POST alerts to the admin API.
        val report = com.ovulation.health.data.model.AdminReport(
            reportDate = LocalDateTime.now(),
            startDate  = LocalDateTime.now().minusDays(1),
            endDate    = LocalDateTime.now(),
            userId     = "default_user",
            reportData = buildAlertJson(prediction),
            graphData  = "{}"
        )
        db.adminReportDao().insert(report)
        Timber.d("Admin alert enqueued for high-confidence ovulation prediction")
    }

    private fun buildAlertJson(p: OvulationPrediction) = """
        {
          "alert_type": "OVULATION_IMMINENT",
          "composite_score": ${p.compositeScore},
          "estimated_ovulation": "${p.estimatedOvulationDate}",
          "ferning_score": ${p.ferningScore},
          "voice_score": ${p.voiceScore},
          "temperature_score": ${p.temperatureScore},
          "cardiac_score": ${p.cardiacScore},
          "status": "${p.status}"
        }
    """.trimIndent()
}
