package com.ovulation.health

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.room.Room
import com.ovulation.health.data.db.OvulationDatabase
import timber.log.Timber

class OvulationHealthApp : Application() {

    companion object {
        lateinit var database: OvulationDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Initialize Room Database
        database = Room.databaseBuilder(
            applicationContext,
            OvulationDatabase::class.java,
            "ovulation_health.db"
        ).build()

        // Create Notification Channels
        createNotificationChannels()

        Timber.d("OvulationHealthApp initialized successfully")
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Daily test reminder channel
            val dailyTestChannel = NotificationChannel(
                CHANNEL_DAILY_TEST,
                "Daily Health Tests",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for scheduled daily health tests"
            }

            // Ovulation detection channel
            val ovulationChannel = NotificationChannel(
                CHANNEL_OVULATION,
                "Ovulation Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications when ovulation is detected or predicted"
            }

            // Window of implantation channel
            val implantationChannel = NotificationChannel(
                CHANNEL_IMPLANTATION,
                "Implantation Window",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications about the window of implantation"
            }

            // Data sync channel
            val syncChannel = NotificationChannel(
                CHANNEL_DATA_SYNC,
                "Data Synchronization",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for data synchronization with server"
            }

            notificationManager.createNotificationChannels(
                listOf(dailyTestChannel, ovulationChannel, implantationChannel, syncChannel)
            )
        }
    }

    companion object {
        const val CHANNEL_DAILY_TEST = "channel_daily_test"
        const val CHANNEL_OVULATION = "channel_ovulation"
        const val CHANNEL_IMPLANTATION = "channel_implantation"
        const val CHANNEL_DATA_SYNC = "channel_data_sync"
    }
}
