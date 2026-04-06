package com.ovulation.health.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ovulation.health.OvulationHealthApp
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────
// Predictions
// ─────────────────────────────────────────────

@Composable
fun PredictionsScreen(navController: NavHostController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        TopBar("Predictions", navController)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                PredictionCard(
                    title = "Ovulation Prediction",
                    date = "April 9, 2026",
                    confidence = 0.92f,
                    details = listOf("Ferning: 95%", "Voice: 82%", "Temperature: 88%", "Cardiac: 75%")
                )
            }

            item {
                PredictionCard(
                    title = "Implantation Window",
                    date = "April 14–18, 2026",
                    confidence = 0.88f,
                    details = listOf("Optimal: April 15–17", "Endometrial: Ready")
                )
            }

            item { Text("Confidence Metrics", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 12.dp)) }
            item { MetricBar("Ferning Analysis", 0.95f) }
            item { MetricBar("Voice Frequency",  0.81f) }
            item { MetricBar("Body Temperature", 0.88f) }
            item { MetricBar("Cardiac",          0.75f) }
        }
    }
}

// ─────────────────────────────────────────────
// History
// ─────────────────────────────────────────────

@Composable
fun HistoryScreen(navController: NavHostController) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TopBar("History", navController)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(30) { index ->
                HistoryItem(
                    date = "April ${6 - index % 6}, 2026",
                    testsCompleted = 4,
                    phase = listOf("Follicular", "Ovulation", "Luteal", "Menstruation")[index % 4]
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// Settings
// ─────────────────────────────────────────────

@Composable
fun SettingsScreen(navController: NavHostController) {
    val authManager = OvulationHealthApp.authManager
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TopBar("Settings", navController)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { SettingItem("Daily Test Time",  "8:00 AM",             Icons.Default.Schedule) }
            item { SettingItem("Cycle Length",     "28 days",             Icons.Default.CalendarToday) }
            item { SettingItem("Notifications",    "Enabled",             Icons.Default.Notifications) }
            item { SettingItem("Admin Email",      "admin@example.com",   Icons.Default.Email) }
            item { SettingItem("Data Backup",      "Last synced: Today",  Icons.Default.CloudUpload) }

            item {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { scope.launch { authManager.logout() } },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Logout, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Logout")
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// Reports
// ─────────────────────────────────────────────

@Composable
fun ReportsScreen(navController: NavHostController) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TopBar("Reports", navController)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(6) { index ->
                ReportItem(month = "April", year = "2026", dataPoints = 120 - index * 10, predictions = 4)
            }
        }
    }
}

// ─────────────────────────────────────────────
// Shared composable components
// ─────────────────────────────────────────────

@Composable
fun TopBar(title: String, navController: NavHostController) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.Default.ArrowBack, "Back")
        }
    }
    Divider()
    Spacer(Modifier.height(8.dp))
}

@Composable
fun PredictionCard(title: String, date: String, confidence: Float, details: List<String>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(date, fontSize = 13.sp, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(progress = confidence, modifier = Modifier.fillMaxWidth())
            Text("${(confidence * 100).toInt()}% confidence", fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            details.forEach { Text("• $it", fontSize = 12.sp) }
        }
    }
}

@Composable
fun MetricBar(label: String, value: Float) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 12.sp)
            Text("${(value * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(progress = value, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
    }
}

@Composable
fun HistoryItem(date: String, testsCompleted: Int, phase: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(date, fontWeight = FontWeight.Bold)
                Text("$testsCompleted tests completed", fontSize = 12.sp, color = Color.Gray)
            }
            Text(phase, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun SettingItem(title: String, value: String, icon: ImageVector) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, null, modifier = Modifier.size(22.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Bold)
                    Text(value, fontSize = 12.sp, color = Color.Gray)
                }
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
        }
    }
}

@Composable
fun ReportItem(month: String, year: String, dataPoints: Int, predictions: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("$month $year", fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("$dataPoints data points", fontSize = 12.sp)
                Text("$predictions predictions", fontSize = 12.sp)
            }
        }
    }
}
