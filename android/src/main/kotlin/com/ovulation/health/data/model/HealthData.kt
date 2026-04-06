package com.ovulation.health.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

// ---------------------------------------------------------------------------
// Every health entity carries a subjectId so the admin can query
// data per-subject and the app never mixes readings across users.
// ---------------------------------------------------------------------------

@Entity(
    tableName = "ferning_analysis",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["subjectId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("subjectId")]
)
data class FerningAnalysis(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val subjectId: String,
    val testDate: LocalDateTime,
    val imageUri: String,
    val ferningPattern: FerningPattern,
    val confidence: Float,
    val notes: String = ""
) {
    enum class FerningPattern { NONE, PARTIAL, FULL }
}

@Entity(
    tableName = "voice_analysis",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["subjectId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("subjectId")]
)
data class VoiceAnalysis(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val subjectId: String,
    val testDate: LocalDateTime,
    val audioUri: String,
    val fundamentalFrequency: Float,
    val frequencyShift: Float,
    val shimmer: Float,
    val jitter: Float,
    val notes: String = ""
)

@Entity(
    tableName = "temperature_data",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["subjectId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("subjectId")]
)
data class TemperatureData(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val subjectId: String,
    val testDate: LocalDateTime,
    val temperature: Float,
    val measurementMethod: String,
    val accuracy: Float,
    val notes: String = ""
)

@Entity(
    tableName = "cardiac_data",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["subjectId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("subjectId")]
)
data class CardiacData(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val subjectId: String,
    val testDate: LocalDateTime,
    val heartRate: Int,
    val heartRateVariability: Float,
    val oxygenSaturation: Float = 0f,
    val notes: String = ""
)

@Entity(
    tableName = "ovulation_predictions",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["subjectId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("subjectId")]
)
data class OvulationPrediction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val subjectId: String,
    val predictionDate: LocalDateTime,
    val estimatedOvulationDate: LocalDateTime,
    val confidence: Float,
    val ferningScore: Float,
    val voiceScore: Float,
    val temperatureScore: Float,
    val cardiacScore: Float,
    val compositeScore: Float,
    val status: PredictionStatus,
    val notes: String = ""
) {
    enum class PredictionStatus { PREDICTED, CONFIRMED, PASSED }
}

@Entity(
    tableName = "implantation_windows",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["subjectId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("subjectId")]
)
data class ImplantationWindow(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val subjectId: String,
    val predictionDate: LocalDateTime,
    val estimatedStartDate: LocalDateTime,
    val estimatedEndDate: LocalDateTime,
    val confidence: Float,
    val biomarkers: String,
    val status: WindowStatus,
    val notes: String = ""
) {
    enum class WindowStatus { PREDICTED, ACTIVE, PASSED }
}

@Entity(
    tableName = "daily_health_summary",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["subjectId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("subjectId")]
)
data class DailyHealthSummary(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val subjectId: String,
    val date: LocalDateTime,
    val allTestsCompleted: Boolean,
    val ferningAnalysisId: Int? = null,
    val voiceAnalysisId: Int? = null,
    val temperatureDataId: Int? = null,
    val cardiacDataId: Int? = null,
    val cycleDay: Int,
    val menstrualPhase: MenstrualPhase,
    val notes: String = ""
) {
    enum class MenstrualPhase { MENSTRUATION, FOLLICULAR, OVULATION, LUTEAL }
}

@Entity(tableName = "admin_reports")
data class AdminReport(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val subjectId: String,           // the subject this report is about
    val adminId: String,             // the admin who owns this report
    val reportDate: LocalDateTime,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val reportData: String,
    val graphData: String,
    val synced: Boolean = false,
    val syncedDate: LocalDateTime? = null
)
