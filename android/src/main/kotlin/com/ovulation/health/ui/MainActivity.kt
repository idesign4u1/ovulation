package com.ovulation.health.ui

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ovulation.health.sensor.CameraOverlayManager
import com.ovulation.health.service.ContinuousMonitoringService
import com.ovulation.health.ui.screen.*
import com.ovulation.health.ui.theme.OvulationHealthTheme
import timber.log.Timber

class MainActivity : ComponentActivity() {

    // ---- Service binding ----
    private var monitoringService: ContinuousMonitoringService? = null
    private var serviceBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            monitoringService = (binder as ContinuousMonitoringService.LocalBinder).getService()
            serviceBound = true
            Timber.d("ContinuousMonitoringService bound")

            // Install the camera-area overlay now that the service is alive
            cameraOverlay.install()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            serviceBound = false
            monitoringService = null
        }
    }

    // ---- Camera overlay (detects finger on lens) ----
    private lateinit var cameraOverlay: CameraOverlayManager

    // ---- Permission launcher ----
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        results.forEach { (perm, granted) ->
            Timber.d("Permission $perm granted=$granted")
        }
        // Try to start service regardless; individual components handle missing perms gracefully
        startMonitoringService()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cameraOverlay = CameraOverlayManager(this)

        requestRequiredPermissions()

        setContent {
            OvulationHealthTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "dashboard") {
                        composable("dashboard")    { DashboardScreen(navController) }
                        composable("predictions")  { PredictionsScreen(navController) }
                        composable("history")      { HistoryScreen(navController) }
                        composable("settings")     { SettingsScreen(navController) }
                        composable("reports")      { ReportsScreen(navController) }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Bind to the running service
        val intent = Intent(this, ContinuousMonitoringService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        if (serviceBound) {
            unbindService(serviceConnection)
            serviceBound = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraOverlay.remove()
    }

    /**
     * Every screen touch is forwarded to the TouchTemperatureAnalyzer.
     * We do this at the Activity level so it works across all screens
     * without adding boilerplate to each Composable.
     */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            monitoringService?.touchTemperatureAnalyzer?.sample()
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun startMonitoringService() {
        ContinuousMonitoringService.start(this)

        // Ask the user to disable battery optimisation so the service
        // is not killed while the screen is off.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService(android.os.PowerManager::class.java)
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                startActivity(Intent(
                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:$packageName")
                ))
            }
        }

        // Ask for Draw-over-other-apps if not already granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            !Settings.canDrawOverlays(this)
        ) {
            startActivity(Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            ))
        }
    }

    private fun requestRequiredPermissions() {
        val perms = buildList {
            add(Manifest.permission.CAMERA)
            add(Manifest.permission.RECORD_AUDIO)
            add(Manifest.permission.READ_PHONE_STATE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
        permissionLauncher.launch(perms.toTypedArray())
    }
}
