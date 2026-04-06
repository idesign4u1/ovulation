package com.ovulation.health.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ovulation.health.OvulationHealthApp
import com.ovulation.health.data.model.*
import com.ovulation.health.ml.FerningAnalyzer
import com.ovulation.health.ml.VoiceAnalyzer
import com.ovulation.health.ml.PredictionEngine
import com.ovulation.health.sensor.SensorDataCollector
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.time.LocalDateTime
import kotlin.coroutines.resume

/**
 * Worker that performs daily health tests
 * Collects ferning, voice, temperature, and cardiac data
 */
class DailyHealthTestWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val database = OvulationHealthApp.database
    private val sensorCollector = SensorDataCollector(context)
    private val voiceAnalyzer = VoiceAnalyzer(context)
    private val ferningAnalyzer = FerningAnalyzer(context)
    private val predictionEngine = PredictionEngine()

    override suspend fun doWork(): Result {
        return try {
            Timber.d("Starting daily health test")

            // 1. Collect sensor data (temperature and cardiac)
            collectSensorData()

            // 2. Record voice sample
            recordVoiceSample()

            // 3. Capture saliva ferning image
            captureFerningImage()

            // 4. Calculate predictions
            calculatePredictions()

            // 5. Generate report for admin
            generateAdminReport()

            Timber.d("Daily health test completed successfully")
            Result.success()

        } catch (e: Exception) {
            Timber.e(e, "Daily health test failed")
            Result.retry()
        }
    }

    private suspend fun collectSensorData() {
        suspendCancellableCoroutine<Unit> { continuation ->
            sensorCollector.addListener(object : SensorDataCollector.DataCollectionListener {
                override fun onCardiacDataCollected(data: CardiacData) {
                    database.cardiacDataDao().insert(data)
                    Timber.d("Cardiac data saved: HR=${data.heartRate}, HRV=${data.heartRateVariability}")
                }

                override fun onTemperatureCollected(data: TemperatureData) {
                    database.temperatureDataDao().insert(data)
                    Timber.d("Temperature data saved: ${data.temperature}°C")
                    continuation.resume(Unit)
                }

                override fun onError(error: String) {
                    Timber.e("Sensor collection error: $error")
                    continuation.resume(Unit)
                }
            })

            // Start sensor monitoring
            sensorCollector.startHeartRateMonitoring(durationSeconds = 60)
            sensorCollector.collectBodyTemperature()
        }
    }

    private suspend fun recordVoiceSample() {
        suspendCancellableCoroutine<Unit> { continuation ->
            voiceAnalyzer.addListener(object : VoiceAnalyzer.VoiceAnalysisListener {
                override fun onAnalysisComplete(analysis: VoiceAnalysis) {
                    database.voiceAnalysisDao().insert(analysis)
                    Timber.d("Voice analysis saved: F0=${analysis.fundamentalFrequency}Hz, Shift=${analysis.frequencyShift}Hz")
                    continuation.resume(Unit)
                }

                override fun onError(error: String) {
                    Timber.e("Voice analysis error: $error")
                    continuation.resume(Unit)
                }
            })

            // Start voice recording (10 seconds)
            voiceAnalyzer.startRecording(durationSeconds = 10)
        }
    }

    private suspend fun captureFerningImage() {
        suspendCancellableCoroutine<Unit> { continuation ->
            ferningAnalyzer.addListener(object : FerningAnalyzer.FerningAnalysisListener {
                override fun onAnalysisComplete(analysis: FerningAnalysis) {
                    database.ferningAnalysisDao().insert(analysis)
                    Timber.d("Ferning analysis saved: Pattern=${analysis.ferningPattern}, Confidence=${analysis.confidence}")
                    continuation.resume(Unit)
                }

                override fun onProgress(progress: Int) {
                    Timber.d("Ferning analysis progress: $progress%")
                }

                override fun onError(error: String) {
                    Timber.e("Ferning analysis error: $error")
                    continuation.resume(Unit)
                }
            })

            // In real implementation, would capture image from camera
            // For now, this is a placeholder
            Timber.d("Capturing ferning image from camera...")
        }
    }

    private suspend fun calculatePredictions() {
        try {
            val now = LocalDateTime.now()
            val cycleStartDate = now.minusDays(28) // Get last 28 days of data

            // Retrieve all collected data
            val ferningData = database.ferningAnalysisDao()
                .getAnalysisInRange(cycleStartDate, now)
                .collect { list ->
                    val voiceData = database.voiceAnalysisDao()
                        .getAnalysisInRange(cycleStartDate, now)
                        .collect { voices ->
                            val tempData = database.temperatureDataDao()
                                .getDataInRange(cycleStartDate, now)
                                .collect { temps ->
                                    val cardiacData = database.cardiacDataDao()
                                        .getDataInRange(cycleStartDate, now)
                                        .collect { cardiacList ->
                                            // Calculate predictions
                                            val ovulationPrediction = predictionEngine.predictOvulation(
                                                ferningData = list,
                                                voiceData = voices,
                                                temperatureData = temps,
                                                cardiacData = cardiacList
                                            )

                                            database.ovulationPredictionDao().insert(ovulationPrediction)
                                            Timber.d("Ovulation prediction saved: ${ovulationPrediction.estimatedOvulationDate}")

                                            // Predict implantation window
                                            val implantationWindow = predictionEngine.predictImplantationWindow(
                                                ovulationPrediction = ovulationPrediction,
                                                temperatureData = temps
                                            )

                                            database.implantationWindowDao().insert(implantationWindow)
                                            Timber.d("Implantation window predicted: ${implantationWindow.estimatedStartDate} - ${implantationWindow.estimatedEndDate}")
                                        }
                                }
                        }
                }

        } catch (e: Exception) {
            Timber.e(e, "Error calculating predictions")
        }
    }

    private suspend fun generateAdminReport() {
        try {
            val now = LocalDateTime.now()
            val reportStartDate = now.minusDays(7) // Last 7 days

            // Get latest prediction
            val latestPrediction = database.ovulationPredictionDao().getLatestPrediction()

            if (latestPrediction != null) {
                val reportData = buildReportData(latestPrediction)
                val graphData = buildGraphData(reportStartDate, now)

                val report = AdminReport(
                    reportDate = now,
                    startDate = reportStartDate,
                    endDate = now,
                    userId = "default_user", // Would be replaced with actual user ID
                    reportData = reportData,
                    graphData = graphData,
                    synced = false
                )

                database.adminReportDao().insert(report)
                Timber.d("Admin report generated and saved")
            }

        } catch (e: Exception) {
            Timber.e(e, "Error generating admin report")
        }
    }

    private fun buildReportData(prediction: OvulationPrediction): String {
        return """
            {
                "prediction_date": "${prediction.predictionDate}",
                "estimated_ovulation": "${prediction.estimatedOvulationDate}",
                "confidence": ${prediction.confidence},
                "ferning_score": ${prediction.ferningScore},
                "voice_score": ${prediction.voiceScore},
                "temperature_score": ${prediction.temperatureScore},
                "cardiac_score": ${prediction.cardiacScore},
                "status": "${prediction.status}"
            }
        """.trimIndent()
    }

    private fun buildGraphData(startDate: LocalDateTime, endDate: LocalDateTime): String {
        // In real implementation, would compile actual data into graph-ready format
        return """
            {
                "temperature_chart": [],
                "heart_rate_chart": [],
                "ferning_progression": [],
                "voice_frequency_chart": []
            }
        """.trimIndent()
    }

    // Extension function to collect from Flow
    private suspend inline fun <T> kotlinx.coroutines.flow.Flow<T>.collect(action: suspend (T) -> Unit) {
        kotlinx.coroutines.flow.collect(this, action)
    }
}
