package com.ovulation.health.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ovulation.health.service.DailyTestScheduler
import com.ovulation.health.ui.screen.*
import com.ovulation.health.ui.theme.OvulationHealthTheme
import timber.log.Timber

class MainActivity : ComponentActivity() {

    private lateinit var dailyTestScheduler: DailyTestScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize daily test scheduler
        dailyTestScheduler = DailyTestScheduler(this)
        dailyTestScheduler.scheduleDailyTests(hour = 8, minute = 0)

        Timber.d("Daily tests scheduled for 8:00 AM")

        setContent {
            OvulationHealthTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "dashboard"
                    ) {
                        composable("dashboard") {
                            DashboardScreen(navController)
                        }
                        composable("predictions") {
                            PredictionsScreen(navController)
                        }
                        composable("history") {
                            HistoryScreen(navController)
                        }
                        composable("settings") {
                            SettingsScreen(navController)
                        }
                        composable("reports") {
                            ReportsScreen(navController)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up resources
        dailyTestScheduler.cancelDailyTests()
    }
}
