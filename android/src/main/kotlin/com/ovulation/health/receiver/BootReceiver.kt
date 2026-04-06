package com.ovulation.health.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ovulation.health.service.ContinuousMonitoringService
import timber.log.Timber

/**
 * Restarts the ContinuousMonitoringService after the device reboots.
 *
 * Without this the foreground service would stop at every reboot and
 * passive data collection would silently cease.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in listOf(
                Intent.ACTION_BOOT_COMPLETED,
                "android.intent.action.QUICKBOOT_POWERON"
            )
        ) return

        Timber.d("Boot completed – restarting ContinuousMonitoringService")
        ContinuousMonitoringService.start(context)
    }
}
