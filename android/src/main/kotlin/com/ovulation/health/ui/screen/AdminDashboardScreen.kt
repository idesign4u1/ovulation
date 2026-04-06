package com.ovulation.health.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ovulation.health.OvulationHealthApp
import com.ovulation.health.auth.AuthManager
import com.ovulation.health.data.model.OvulationPrediction
import com.ovulation.health.data.model.User
import kotlinx.coroutines.flow.collect
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Admin-only dashboard.
 *
 * Shows:
 * - List of subjects with their latest prediction score
 * - High-confidence alerts (composite ≥ 0.85)
 * - Quick navigation to per-subject charts
 */
@Composable
fun AdminDashboardScreen(
    authManager: AuthManager,
    onLogout: () -> Unit
) {
    val admin = authManager.currentUser.collectAsState().value ?: return
    val db = OvulationHealthApp.database

    var subjects by remember { mutableStateOf<List<User>>(emptyList()) }
    var latestPredictions by remember { mutableStateOf<Map<String, OvulationPrediction>>(emptyMap()) }
    var selectedSubjectId by remember { mutableStateOf<String?>(null) }

    // Load subjects assigned to this admin
    LaunchedEffect(admin.id) {
        db.userDao().getSubjectsForAdmin(admin.id).collect { list ->
            subjects = list
            // Load latest prediction for each
            val preds = mutableMapOf<String, OvulationPrediction>()
            list.forEach { subject ->
                db.ovulationPredictionDao().getLatestForSubject(subject.id)
                    ?.let { preds[subject.id] = it }
            }
            latestPredictions = preds
        }
    }

    if (selectedSubjectId != null) {
        SubjectDetailScreen(
            subjectId = selectedSubjectId!!,
            onBack = { selectedSubjectId = null }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Admin Dashboard", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Welcome, ${admin.displayName}", fontSize = 13.sp, color = Color.Gray)
            }
            Row {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Notifications, "Alerts")
                }
                IconButton(onClick = onLogout) {
                    Icon(Icons.Default.Logout, "Logout")
                }
            }
        }

        Divider()

        // Alert banner for high-confidence events
        val alertSubjects = subjects.filter { s ->
            latestPredictions[s.id]?.compositeScore?.let { it >= 0.85f } == true
        }
        if (alertSubjects.isNotEmpty()) {
            AlertBanner(count = alertSubjects.size)
            Spacer(Modifier.height(8.dp))
        }

        // Stats summary row
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatChip(modifier = Modifier.weight(1f), label = "Subjects", value = subjects.size.toString())
            StatChip(modifier = Modifier.weight(1f), label = "Alerts", value = alertSubjects.size.toString(), urgent = alertSubjects.isNotEmpty())
            StatChip(modifier = Modifier.weight(1f), label = "Active today",
                value = subjects.count { s ->
                    latestPredictions[s.id]?.predictionDate?.isAfter(LocalDateTime.now().minusHours(24)) == true
                }.toString()
            )
        }

        Spacer(Modifier.height(8.dp))
        Text("Subjects", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(Modifier.height(4.dp))

        if (subjects.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.GroupAdd, null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    Text("No subjects assigned yet", color = Color.Gray)
                    Text("Share your Admin ID: ${admin.id.take(8)}…", fontSize = 12.sp, color = Color.Gray)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(subjects) { subject ->
                    val pred = latestPredictions[subject.id]
                    SubjectCard(
                        subject = subject,
                        latestPrediction = pred,
                        onClick = { selectedSubjectId = subject.id }
                    )
                }
            }
        }
    }
}

// ---- Subject detail (per-subject charts & data) ----

@Composable
fun SubjectDetailScreen(subjectId: String, onBack: () -> Unit) {
    val db = OvulationHealthApp.database
    var subject by remember { mutableStateOf<User?>(null) }
    var prediction by remember { mutableStateOf<OvulationPrediction?>(null) }

    LaunchedEffect(subjectId) {
        subject = db.userDao().getById(subjectId)
        prediction = db.ovulationPredictionDao().getLatestForSubject(subjectId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
            Text(subject?.displayName ?: "Subject", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Divider()
        Spacer(Modifier.height(12.dp))

        prediction?.let { pred ->
            // Score breakdown card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Latest Prediction", fontWeight = FontWeight.Bold)
                    Text(
                        "Ovulation: ${pred.estimatedOvulationDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}",
                        fontSize = 13.sp, color = Color.Gray
                    )
                    Spacer(Modifier.height(12.dp))

                    ScoreRow("Composite", pred.compositeScore)
                    ScoreRow("Ferning",   pred.ferningScore,   Color(0xFF4CAF50))
                    ScoreRow("Voice",     pred.voiceScore,     Color(0xFF9C27B0))
                    ScoreRow("Temp",      pred.temperatureScore, Color(0xFFFF9800))
                    ScoreRow("Cardiac",   pred.cardiacScore,   Color(0xFFE91E63))

                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Status: ${pred.status}  •  Confidence: ${(pred.compositeScore * 100).toInt()}%",
                        fontSize = 12.sp,
                        color = if (pred.compositeScore >= 0.85f) Color(0xFF4CAF50) else Color.Gray
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("Notes", fontWeight = FontWeight.SemiBold)
            Text(pred.notes, fontSize = 12.sp, color = Color.Gray)
        } ?: run {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No predictions yet for this subject", color = Color.Gray)
            }
        }
    }
}

// ---- Reusable components ----

@Composable
private fun AlertBanner(count: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF5252).copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.NotificationsActive, null, tint = Color(0xFFFF5252))
            Text(
                "$count subject(s) show high-confidence ovulation signal (≥ 85%)",
                fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFFF5252)
            )
        }
    }
}

@Composable
private fun StatChip(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    urgent: Boolean = false
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (urgent) Color(0xFFFF5252).copy(0.1f)
                             else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                color = if (urgent) Color(0xFFFF5252) else MaterialTheme.colorScheme.primary)
            Text(label, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun SubjectCard(
    subject: User,
    latestPrediction: OvulationPrediction?,
    onClick: () -> Unit
) {
    val score = latestPrediction?.compositeScore ?: 0f
    val scoreColor = when {
        score >= 0.85f -> Color(0xFF4CAF50)
        score >= 0.70f -> Color(0xFFFF9800)
        score > 0f     -> Color(0xFF2196F3)
        else           -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    subject.displayName.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(subject.displayName, fontWeight = FontWeight.SemiBold)
                Text(subject.email, fontSize = 12.sp, color = Color.Gray)
                if (latestPrediction != null) {
                    Text(
                        "Ovulation: ${latestPrediction.estimatedOvulationDate
                            .format(DateTimeFormatter.ofPattern("MMM d"))}",
                        fontSize = 11.sp, color = Color.Gray
                    )
                }
            }

            // Score badge
            if (score > 0f) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(scoreColor.copy(0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${(score * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor
                    )
                }
            }

            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
        }
    }
}

@Composable
private fun ScoreRow(label: String, value: Float, color: Color = MaterialTheme.colorScheme.primary) {
    Column(modifier = Modifier.padding(vertical = 3.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 12.sp)
            Text("${(value * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
        LinearProgressIndicator(
            progress = value,
            modifier = Modifier.fillMaxWidth().height(5.dp),
            color = color
        )
    }
}
