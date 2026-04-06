package com.ovulation.health.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.ovulation.health.service.ContinuousMonitoringService
import timber.log.Timber

/**
 * Listens for phone call state changes and notifies the
 * ContinuousMonitoringService so it can start/stop passive
 * voice analysis accordingly.
 *
 * Requires:
 *   <uses-permission android:name="android.permission.READ_PHONE_STATE"/>
 */
class PhoneCallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        Timber.d("Phone state changed: $state")

        when (state) {
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                // Call in progress – start voice analysis
                Timber.d("Call started – triggering voice analysis")
                ContinuousMonitoringService.notifyCallStarted(context)
            }

            TelephonyManager.EXTRA_STATE_IDLE -> {
                // Call ended or no call
                Timber.d("Call ended – stopping voice analysis")
                ContinuousMonitoringService.notifyCallEnded(context)
            }

            TelephonyManager.EXTRA_STATE_RINGING -> {
                // Ringing – optionally prepare analysers
                Timber.d("Incoming call ringing")
            }
        }
    }
}
