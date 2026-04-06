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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ovulation.health.OvulationHealthApp
import com.ovulation.health.data.model.UserRole
import com.ovulation.health.sensor.CameraOverlayManager
import com.ovulation.health.service.ContinuousMonitoringService
import com.ovulation.health.ui.screen.*
import com.ovulation.health.ui.theme.OvulationHealthTheme
import kotlinx.coroutines.launch
import timber.log.Timber

class MainActivity : ComponentActivity() {

    private val authManager get() = OvulationHealthApp.authManager

    // ---- Service binding ----
    private var monitoringService: ContinuousMonitoringService? = null
    private var serviceBound = false
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            monitoringService = (binder as ContinuousMonitoringService.LocalBinder).getService()
            serviceBound = true
            cameraOverlay.install()
        }
        override fun onServiceDisconnected(name: ComponentName) {
            serviceBound = false; monitoringService = null
        }
    }

    private lateinit var cameraOverlay: CameraOverlayManager

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { startMonitoringService() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraOverlay = CameraOverlayManager(this)

        // Restore previous session before the first frame
        lifecycleScope.launch { authManager.restoreSession() }

        requestRequiredPermissions()

        setContent {
            OvulationHealthTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppRoot()
                }
            }
        }
    }

    @Composable
    private fun AppRoot() {
        val currentUser by authManager.currentUser.collectAsState()
        val navController = rememberNavController()

        when {
            // Not logged in → show auth screens
            currentUser == null -> {
                LoginScreen(
                    authManager = authManager,
                    onLoginSuccess = { isAdmin ->
                        // Start monitoring for all logged-in users
                        ContinuousMonitoringService.start(this)
                    }
                )
            }

            // Admin → admin dashboard
            currentUser!!.role == UserRole.ADMIN -> {
                AdminDashboardScreen(
                    authManager = authManager,
                    onLogout = { lifecycleScope.launch { authManager.logout() } }
                )
            }

            // Subject → subject dashboard + navigation
            else -> {
                NavHost(navController = navController, startDestination = "dashboard") {
                    composable("dashboard")   { DashboardScreen(navController) }
                    composable("predictions") { PredictionsScreen(navController) }
                    composable("history")     { HistoryScreen(navController) }
                    composable("settings")    { SettingsScreen(navController) }
                    composable("reports")     { ReportsScreen(navController) }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (authManager.isLoggedIn) {
            bindService(
                Intent(this, ContinuousMonitoringService::class.java),
                serviceConnection,
                Context.BIND_AUTO_CREATE
            )
        }
    }

    override fun onStop() {
        super.onStop()
        if (serviceBound) { unbindService(serviceConnection); serviceBound = false }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraOverlay.remove()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            monitoringService?.touchTemperatureAnalyzer?.sample()
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun startMonitoringService() {
        ContinuousMonitoringService.start(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService(android.os.PowerManager::class.java)
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:$packageName")))
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")))
        }
    }

    private fun requestRequiredPermissions() {
        val perms = buildList {
            add(Manifest.permission.CAMERA)
            add(Manifest.permission.RECORD_AUDIO)
            add(Manifest.permission.READ_PHONE_STATE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(perms.toTypedArray())
    }
}
