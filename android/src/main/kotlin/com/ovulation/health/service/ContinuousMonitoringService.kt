package com.ovulation.health.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.ovulation.health.OvulationHealthApp
import com.ovulation.health.R
import com.ovulation.health.sensor.CameraPPGDetector
import com.ovulation.health.sensor.PassiveVoiceMonitor
import com.ovulation.health.sensor.TouchTemperatureAnalyzer
import com.ovulation.health.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Foreground service that runs continuously and passively collects
 * physiological data throughout the day.
 *
 * Triggers:
 * - Touch on rear camera area → PPG measurement
 * - Active microphone (call/voice) → Voice frequency analysis
 * - Any screen touch → Temperature reading
 * - Hourly scheduled check → Full data snapshot
 */
class ContinuousMonitoringService : Service() {

    inner class LocalBinder : Binder() {
        fun getService() = this@ContinuousMonitoringService
    }

    private val binder = LocalBinder()

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val database get() = OvulationHealthApp.database

    lateinit var cameraPPGDetector: CameraPPGDetector
        private set
    lateinit var passiveVoiceMonitor: PassiveVoiceMonitor
        private set
    lateinit var touchTemperatureAnalyzer: TouchTemperatureAnalyzer
        private set

    private var hourlyJob: Job? = null

    override fun onCreate() {
        super.onCreate()

        cameraPPGDetector = CameraPPGDetector(this, database)
        passiveVoiceMonitor = PassiveVoiceMonitor(this, database)
        touchTemperatureAnalyzer = TouchTemperatureAnalyzer(this, database)

        startForeground(NOTIF_ID, buildForegroundNotification())
        Timber.d("ContinuousMonitoringService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startMonitoring()
            ACTION_STOP -> stopMonitoring()
            ACTION_CAMERA_TOUCHED -> cameraPPGDetector.startMeasurement()
            ACTION_CALL_STARTED -> passiveVoiceMonitor.startAnalysis()
            ACTION_CALL_ENDED -> passiveVoiceMonitor.stopAnalysis()
            ACTION_SCREEN_TOUCHED -> touchTemperatureAnalyzer.sample()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        scope.cancel()
        cameraPPGDetector.release()
        passiveVoiceMonitor.release()
        touchTemperatureAnalyzer.release()
        super.onDestroy()
    }

    private fun startMonitoring() {
        Timber.d("Starting continuous monitoring")
        startHourlySnapshots()
        touchTemperatureAnalyzer.startListening()
    }

    private fun stopMonitoring() {
        hourlyJob?.cancel()
        touchTemperatureAnalyzer.stopListening()
        Timber.d("Continuous monitoring stopped")
    }

    /**
     * Every hour: pull the latest readings already stored by passive
     * triggers and run the prediction engine on them.
     */
    private fun startHourlySnapshots() {
        hourlyJob?.cancel()
        hourlyJob = scope.launch {
            while (isActive) {
                delay(HOURLY_INTERVAL_MS)
                try {
                    Timber.d("Hourly snapshot triggered")
                    runHourlyAnalysis()
                } catch (e: Exception) {
                    Timber.e(e, "Hourly snapshot failed")
                }
            }
        }
    }

    private suspend fun runHourlyAnalysis() {
        // Delegate heavy work to the existing worker
        val workRequest = androidx.work.OneTimeWorkRequestBuilder<
            com.ovulation.health.work.HourlyAnalysisWorker>().build()
        androidx.work.WorkManager.getInstance(this)
            .enqueue(workRequest)
    }

    // ----- Notification -----

    private fun buildForegroundNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, OvulationHealthApp.CHANNEL_DATA_SYNC)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("Health Monitoring Active")
            .setContentText("Passively collecting physiological data")
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    companion object {
        const val NOTIF_ID = 9001
        const val HOURLY_INTERVAL_MS = 60 * 60 * 1000L   // 1 hour

        const val ACTION_START = "com.ovulation.START_MONITORING"
        const val ACTION_STOP = "com.ovulation.STOP_MONITORING"
        const val ACTION_CAMERA_TOUCHED = "com.ovulation.CAMERA_TOUCHED"
        const val ACTION_CALL_STARTED = "com.ovulation.CALL_STARTED"
        const val ACTION_CALL_ENDED = "com.ovulation.CALL_ENDED"
        const val ACTION_SCREEN_TOUCHED = "com.ovulation.SCREEN_TOUCHED"

        fun start(context: Context) {
            val intent = Intent(context, ContinuousMonitoringService::class.java)
                .apply { action = ACTION_START }
            context.startForegroundService(intent)
        }

        fun notifyCameraTouched(context: Context) {
            context.startService(Intent(context, ContinuousMonitoringService::class.java)
                .apply { action = ACTION_CAMERA_TOUCHED })
        }

        fun notifyCallStarted(context: Context) {
            context.startService(Intent(context, ContinuousMonitoringService::class.java)
                .apply { action = ACTION_CALL_STARTED })
        }

        fun notifyCallEnded(context: Context) {
            context.startService(Intent(context, ContinuousMonitoringService::class.java)
                .apply { action = ACTION_CALL_ENDED })
        }

        fun notifyScreenTouched(context: Context) {
            context.startService(Intent(context, ContinuousMonitoringService::class.java)
                .apply { action = ACTION_SCREEN_TOUCHED })
        }
    }
}
