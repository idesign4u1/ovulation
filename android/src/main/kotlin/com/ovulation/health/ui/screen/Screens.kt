package com.ovulation.health.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineData

@Composable
fun DashboardScreen(navController: NavHostController) {
    var showTestDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Health Dashboard",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("Daily monitoring & predictions", color = Color.Gray)
            }

            Icon(
                Icons.Default.Home,
                contentDescription = "Home",
                modifier = Modifier.size(32.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Summary Cards
            item {
                SummaryCard(
                    title = "Today's Status",
                    icon = Icons.Default.CheckCircle,
                    status = "All tests completed",
                    color = Color(0xFF4CAF50)
                )
            }

            item {
                SummaryCard(
                    title = "Ovulation Prediction",
                    icon = Icons.Default.Info,
                    status = "Predicted in 3 days",
                    color = Color(0xFF2196F3)
                )
            }

            item {
                SummaryCard(
                    title = "Implantation Window",
                    icon = Icons.Default.FavoriteBorder,
                    status = "7-11 days from now",
                    color = Color(0xFFFF9800)
                )
            }

            // Action Buttons
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showTestDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Test")
                    }

                    Button(
                        onClick = { navController.navigate("predictions") },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        )
                    ) {
                        Icon(Icons.Default.BarChart, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Predictions")
                    }
                }
            }

            // Recent Data
            item {
                Text(
                    "Recent Data",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item {
                DataItem(
                    label = "Temperature",
                    value = "36.8°C",
                    trend = "↑ 0.3°C from baseline"
                )
            }

            item {
                DataItem(
                    label = "Heart Rate",
                    value = "72 BPM",
                    trend = "↑ 2 BPM from baseline"
                )
            }

            item {
                DataItem(
                    label = "Voice Frequency",
                    value = "215 Hz",
                    trend = "↑ 5 Hz from baseline"
                )
            }

            item {
                DataItem(
                    label = "Ferning Pattern",
                    value = "Full Pattern",
                    trend = "High estrogen indicator"
                )
            }
        }
    }

    // Test Dialog
    if (showTestDialog) {
        TestDialog(
            onDismiss = { showTestDialog = false },
            onStartTest = {
                showTestDialog = false
                // Start daily test
            }
        )
    }
}

@Composable
fun PredictionsScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Predictions",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.Close, contentDescription = "Back")
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                PredictionCard(
                    title = "Ovulation Prediction",
                    date = "April 9, 2026",
                    confidence = 0.92f,
                    details = listOf(
                        "Ferning: 95%",
                        "Voice: 82%",
                        "Temperature: 88%",
                        "Cardiac: 75%"
                    )
                )
            }

            item {
                PredictionCard(
                    title = "Implantation Window",
                    date = "April 14-18, 2026",
                    confidence = 0.88f,
                    details = listOf(
                        "Optimal: April 15-17",
                        "Endometrial: Ready",
                        "Biomarkers: Positive"
                    )
                )
            }

            item {
                Text(
                    "Confidence Metrics",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item {
                MetricBar("Ferning Analysis", 0.95f)
            }

            item {
                MetricBar("Voice Frequency", 0.81f)
            }

            item {
                MetricBar("Body Temperature", 0.88f)
            }

            item {
                MetricBar("Cardiac Indicators", 0.75f)
            }
        }
    }
}

@Composable
fun HistoryScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "History",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.Close, contentDescription = "Back")
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(30) { index ->
                val day = 6 - index
                HistoryItem(
                    date = "April $day, 2026",
                    testsCompleted = 4,
                    phase = "Follicular"
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Settings",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.Close, contentDescription = "Back")
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SettingItem(
                    title = "Daily Test Time",
                    value = "8:00 AM",
                    icon = Icons.Default.Schedule
                )
            }

            item {
                SettingItem(
                    title = "Cycle Length",
                    value = "28 days",
                    icon = Icons.Default.CalendarToday
                )
            }

            item {
                SettingItem(
                    title = "Notifications",
                    value = "Enabled",
                    icon = Icons.Default.Notifications
                )
            }

            item {
                SettingItem(
                    title = "Admin Email",
                    value = "admin@example.com",
                    icon = Icons.Default.Email
                )
            }

            item {
                SettingItem(
                    title = "Data Backup",
                    value = "Last synced: Today",
                    icon = Icons.Default.CloudUpload
                )
            }
        }
    }
}

@Composable
fun ReportsScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Reports",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.Close, contentDescription = "Back")
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(12) { index ->
                ReportItem(
                    month = "April",
                    year = "2026",
                    dataPoints = 120,
                    predictions = 4
                )
            }
        }
    }
}

// Component Composables

@Composable
fun SummaryCard(
    title: String,
    icon: androidx.compose.material.icons.materialIcon,
    status: String,
    color: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(status, fontSize = 12.sp, color = Color.Gray)
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.2f), shape = MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun DataItem(label: String, value: String, trend: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(label, fontWeight = FontWeight.Bold)
                Text(trend, fontSize = 12.sp, color = Color.Gray)
            }
            Text(value, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PredictionCard(
    title: String,
    date: String,
    confidence: Float,
    details: List<String>
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(date, fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = confidence,
                modifier = Modifier.fillMaxWidth()
            )
            Text("${(confidence * 100).toInt()}% confidence", fontSize = 12.sp)

            Spacer(modifier = Modifier.height(12.dp))

            details.forEach { detail ->
                Text("• $detail", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun MetricBar(label: String, value: Float) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 12.sp)
            Text("${(value * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = value,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )
    }
}

@Composable
fun HistoryItem(date: String, testsCompleted: Int, phase: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(date, fontWeight = FontWeight.Bold)
                Text("$testsCompleted tests completed", fontSize = 12.sp, color = Color.Gray)
            }
            Text(phase, fontSize = 12.sp, color = Color(0xFF2196F3))
        }
    }
}

@Composable
fun SettingItem(title: String, value: String, icon: androidx.compose.material.icons.materialIcon) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Bold)
                    Text(value, fontSize = 12.sp, color = Color.Gray)
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}

@Composable
fun ReportItem(month: String, year: String, dataPoints: Int, predictions: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("$month $year", fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("$dataPoints data points", fontSize = 12.sp)
                Text("$predictions predictions", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun TestDialog(
    onDismiss: () -> Unit,
    onStartTest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Start Health Test") },
        text = { Text("This will take approximately 10-15 minutes. Ensure you have:\n• Lighting for camera\n• Microphone available\n• Clean surface for saliva sample") },
        confirmButton = {
            Button(onClick = onStartTest) {
                Text("Start")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
