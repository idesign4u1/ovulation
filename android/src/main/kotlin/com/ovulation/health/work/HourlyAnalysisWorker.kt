package com.ovulation.health.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ovulation.health.OvulationHealthApp
import com.ovulation.health.data.model.AdminReport
import com.ovulation.health.data.model.DailyHealthSummary
import com.ovulation.health.data.model.OvulationPrediction
import com.ovulation.health.ml.PredictionEngine
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.LocalDateTime

/**
 * Runs every hour (enqueued by ContinuousMonitoringService).
 * Re-runs the prediction engine on the last 24 h of passive data
 * for the currently logged-in subject.
 */
class HourlyAnalysisWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val db = OvulationHealthApp.database
    private val authManager = OvulationHealthApp.authManager
    private val predictionEngine = PredictionEngine()

    override suspend fun doWork(): Result {
        val subject = authManager.currentUser.value
        if (subject == null) {
            Timber.d("HourlyAnalysisWorker: no logged-in user, skipping")
            return Result.success()
        }
        val subjectId = subject.id
        Timber.d("HourlyAnalysisWorker started for subject $subjectId")

        return try {
            val now = LocalDateTime.now()
            val since = now.minusHours(24)

            val ferningList  = db.ferningAnalysisDao().getForSubject(subjectId, since, now).first()
            val voiceList    = db.voiceAnalysisDao().getForSubject(subjectId, since, now).first()
            val tempList     = db.temperatureDataDao().getForSubject(subjectId, since, now).first()
            val cardiacList  = db.cardiacDataDao().getForSubject(subjectId, since, now).first()

            Timber.d("Last 24 h → ferning=${ferningList.size}, voice=${voiceList.size}, temp=${tempList.size}, cardiac=${cardiacList.size}")

            if (voiceList.isEmpty() && tempList.isEmpty() && cardiacList.isEmpty()) {
                return Result.success()
            }

            val prediction = predictionEngine.predictOvulation(
                ferningData = ferningList, voiceData = voiceList,
                temperatureData = tempList, cardiacData = cardiacList
            ).copy(subjectId = subjectId)
            db.ovulationPredictionDao().insert(prediction)

            val implWindow = predictionEngine
                .predictImplantationWindow(prediction, tempList)
                .copy(subjectId = subjectId)
            db.implantationWindowDao().insert(implWindow)

            val cycleDay = estimateCycleDay(subject, now)
            db.dailyHealthSummaryDao().insert(
                DailyHealthSummary(
                    subjectId = subjectId,
                    date = now,
                    allTestsCompleted = cardiacList.isNotEmpty() && voiceList.isNotEmpty(),
                    ferningAnalysisId = ferningList.lastOrNull()?.id,
                    voiceAnalysisId   = voiceList.lastOrNull()?.id,
                    temperatureDataId = tempList.lastOrNull()?.id,
                    cardiacDataId     = cardiacList.lastOrNull()?.id,
                    cycleDay = cycleDay,
                    menstrualPhase = estimatePhase(cycleDay)
                )
            )

            if (prediction.compositeScore >= 0.85f) enqueueAdminAlert(prediction, subjectId)

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "HourlyAnalysisWorker failed")
            Result.retry()
        }
    }

    private fun estimateCycleDay(user: com.ovulation.health.data.model.User, now: LocalDateTime): Int {
        val start = user.lastPeriodStart ?: return ((now.dayOfYear % 28) + 1)
        val days = java.time.temporal.ChronoUnit.DAYS.between(start.toLocalDate(), now.toLocalDate()).toInt() + 1
        return days.coerceIn(1, 35)
    }

    private fun estimatePhase(day: Int) = when (day) {
        in 1..5   -> DailyHealthSummary.MenstrualPhase.MENSTRUATION
        in 6..13  -> DailyHealthSummary.MenstrualPhase.FOLLICULAR
        in 14..16 -> DailyHealthSummary.MenstrualPhase.OVULATION
        else      -> DailyHealthSummary.MenstrualPhase.LUTEAL
    }

    private suspend fun enqueueAdminAlert(prediction: OvulationPrediction, subjectId: String) {
        val adminId = OvulationHealthApp.authManager.currentUser.value?.assignedAdminId ?: return
        val report = AdminReport(
            subjectId  = subjectId,
            adminId    = adminId,
            reportDate = LocalDateTime.now(),
            startDate  = LocalDateTime.now().minusDays(1),
            endDate    = LocalDateTime.now(),
            reportData = buildAlertJson(prediction),
            graphData  = "{}"
        )
        db.adminReportDao().insert(report)
        Timber.d("Admin alert enqueued: composite=${prediction.compositeScore}")
    }

    private fun buildAlertJson(p: OvulationPrediction) = """
        {"alert_type":"OVULATION_IMMINENT","composite_score":${p.compositeScore},
         "estimated_ovulation":"${p.estimatedOvulationDate}","status":"${p.status}"}
    """.trimIndent()
}
