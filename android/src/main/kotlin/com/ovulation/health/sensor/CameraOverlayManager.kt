package com.ovulation.health.sensor

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.ovulation.health.service.ContinuousMonitoringService
import timber.log.Timber

/**
 * Creates an invisible system-level overlay that covers the rear-camera
 * area so any finger placement is detected and routed to the PPG detector.
 *
 * The overlay is transparent and non-interactive everywhere except the
 * small camera region (top-left corner on most phones). It does NOT
 * intercept touch events for the rest of the screen.
 *
 * Requires:
 *   <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
 *
 * Note: On Android 10+ the user must explicitly grant "Draw over other
 * apps" permission via Settings. The app should guide the user there.
 */
class CameraOverlayManager(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: View? = null

    /**
     * Width × height of the overlay hot-zone in dp (approximate camera area).
     * Adjust for target device form factors.
     */
    private val HOT_ZONE_DP = 80

    fun install() {
        if (overlayView != null) return   // already installed

        val density = context.resources.displayMetrics.density
        val sizePx = (HOT_ZONE_DP * density).toInt()

        val params = WindowManager.LayoutParams(
            sizePx, sizePx,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END   // camera is usually top-end on portrait
        }

        val view = View(context).apply {
            alpha = 0f   // fully transparent
            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    Timber.d("Camera area touched – starting PPG")
                    ContinuousMonitoringService.notifyCameraTouched(context)
                }
                false  // don't consume the touch
            }
        }

        try {
            windowManager.addView(view, params)
            overlayView = view
            Timber.d("Camera overlay installed")
        } catch (e: WindowManager.BadTokenException) {
            Timber.e(e, "SYSTEM_ALERT_WINDOW permission not granted")
        }
    }

    fun remove() {
        overlayView?.let {
            try { windowManager.removeView(it) } catch (_: Exception) {}
            overlayView = null
        }
    }
}
