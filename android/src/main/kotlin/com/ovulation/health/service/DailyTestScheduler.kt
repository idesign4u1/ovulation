package com.ovulation.health.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.ovulation.health.receiver.DailyTestReceiver
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Scheduler for daily health tests
 * Ensures tests run at consistent times each day
 */
class DailyTestScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Schedule daily tests at a specific time
     */
    fun scheduleDailyTests(
        hour: Int = 8,  // 8 AM default
        minute: Int = 0
    ) {
        val intent = Intent(context, DailyTestReceiver::class.java).apply {
            action = "com.ovulation.DAILY_TEST"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            DAILY_TEST_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Calculate next trigger time
        val nextTrigger = getNextTriggerTime(hour, minute)
        val triggerAtMillis = nextTrigger
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // For Android 12+, use setExactAndAllowWhileIdle
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                // For older devices
                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            }

        } catch (e: SecurityException) {
            // Handle case where SCHEDULE_EXACT_ALARM permission is not granted
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    /**
     * Cancel scheduled daily tests
     */
    fun cancelDailyTests() {
        val intent = Intent(context, DailyTestReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            DAILY_TEST_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

    private fun getNextTriggerTime(hour: Int, minute: Int): LocalDateTime {
        val now = LocalDateTime.now()
        var nextTrigger = now.withHour(hour).withMinute(minute).withSecond(0)

        // If the scheduled time has already passed today, schedule for tomorrow
        if (nextTrigger.isBefore(now)) {
            nextTrigger = nextTrigger.plusDays(1)
        }

        return nextTrigger
    }

    companion object {
        private const val DAILY_TEST_REQUEST_CODE = 1001
    }
}
