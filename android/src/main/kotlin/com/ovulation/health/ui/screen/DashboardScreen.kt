package com.ovulation.health.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ovulation.health.OvulationHealthApp
import kotlinx.coroutines.flow.collect

@Composable
fun DashboardScreen(navController: NavHostController) {

    val db = OvulationHealthApp.database

    // Pulse animation for "live" indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.3f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "pulse_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // ---- Header ----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Health Dashboard", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text("Passive monitoring active", fontSize = 12.sp, color = Color.Gray)
            }
            // Live indicator dot
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .scale(pulseScale)
                    .background(Color(0xFF4CAF50), CircleShape)
            )
        }

        Divider()
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // ---- Monitoring source cards ----
            item {
                Text("Passive Sensors", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SensorCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.CameraAlt,
                        label = "PPG",
                        description = "Touch camera\nto measure HR"
                    )
                    SensorCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Phone,
                        label = "Voice",
                        description = "Auto-analysed\nduring calls"
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SensorCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.TouchApp,
                        label = "Temp",
                        description = "Measured on\nevery screen touch"
                    )
                    SensorCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Science,
                        label = "Ferning",
                        description = "Tap to capture\nsaliva sample"
                    )
                }
            }

            // ---- Latest readings ----
            item {
                Text(
                    "Latest Readings",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item { LatestReadingRow("Heart Rate", "72 BPM", "↑ +2 from baseline", color = Color(0xFFE91E63)) }
            item { LatestReadingRow("HRV", "42 ms", "↓ –2.5% vs baseline", color = Color(0xFFE91E63)) }
            item { LatestReadingRow("Temperature", "36.9 °C", "↑ +0.4 from baseline", color = Color(0xFFFF9800)) }
            item { LatestReadingRow("Voice F0", "218 Hz", "↑ +8 Hz from baseline", color = Color(0xFF9C27B0)) }
            item { LatestReadingRow("Ferning", "Full pattern", "High estrogen", color = Color(0xFF4CAF50)) }

            // ---- Prediction summary ----
            item {
                Text(
                    "Prediction",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                PredictionBanner(
                    ovulationIn = 2,
                    compositeScore = 0.87f,
                    onDetailsClick = { navController.navigate("predictions") }
                )
            }

            // ---- Action buttons ----
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { navController.navigate("predictions") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.BarChart, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Charts")
                    }
                    Button(
                        onClick = { navController.navigate("reports") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2))
                    ) {
                        Icon(Icons.Default.Send, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Send Report")
                    }
                }
            }
        }
    }
}

// ---- Reusable Components ----

@Composable
private fun SensorCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    description: String
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(28.dp))
            Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(description, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun LatestReadingRow(label: String, value: String, trend: String, color: Color) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(trend, fontSize = 11.sp, color = Color.Gray)
            }
            Text(value, fontWeight = FontWeight.Bold, color = color, fontSize = 15.sp)
        }
    }
}

@Composable
private fun PredictionBanner(
    ovulationIn: Int,
    compositeScore: Float,
    onDetailsClick: () -> Unit
) {
    val pct = (compositeScore * 100).toInt()
    val bannerColor = when {
        compositeScore >= 0.85f -> Color(0xFF4CAF50)
        compositeScore >= 0.70f -> Color(0xFFFF9800)
        else -> Color(0xFF2196F3)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bannerColor.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Ovulation in ~$ovulationIn days",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = bannerColor
                )
                Text("$pct%", fontWeight = FontWeight.Bold, color = bannerColor)
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = compositeScore,
                modifier = Modifier.fillMaxWidth(),
                color = bannerColor
            )
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onDetailsClick) {
                Text("View detailed charts →")
            }
        }
    }
}
