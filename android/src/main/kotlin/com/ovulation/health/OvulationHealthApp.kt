package com.ovulation.health

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.room.Room
import com.ovulation.health.auth.AuthManager
import com.ovulation.health.data.db.OvulationDatabase
import timber.log.Timber

class OvulationHealthApp : Application() {

    companion object {
        lateinit var database: OvulationDatabase
            private set
        lateinit var authManager: AuthManager
            private set
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())

        database = Room.databaseBuilder(
            applicationContext,
            OvulationDatabase::class.java,
            "ovulation_health.db"
        )
            .fallbackToDestructiveMigration()   // replace with proper migrations before prod
            .build()

        authManager = AuthManager(applicationContext, database)

        createNotificationChannels()
        Timber.d("OvulationHealthApp initialized")
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannels(
                listOf(
                    NotificationChannel(CHANNEL_DAILY_TEST,   "Daily Health Tests",    NotificationManager.IMPORTANCE_HIGH),
                    NotificationChannel(CHANNEL_OVULATION,    "Ovulation Alerts",      NotificationManager.IMPORTANCE_HIGH),
                    NotificationChannel(CHANNEL_IMPLANTATION, "Implantation Window",   NotificationManager.IMPORTANCE_HIGH),
                    NotificationChannel(CHANNEL_DATA_SYNC,    "Data Synchronization",  NotificationManager.IMPORTANCE_LOW)
                )
            )
        }
    }

    companion object {
        const val CHANNEL_DAILY_TEST   = "channel_daily_test"
        const val CHANNEL_OVULATION    = "channel_ovulation"
        const val CHANNEL_IMPLANTATION = "channel_implantation"
        const val CHANNEL_DATA_SYNC    = "channel_data_sync"
    }
}
