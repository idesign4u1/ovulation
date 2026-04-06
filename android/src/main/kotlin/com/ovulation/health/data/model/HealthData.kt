package com.ovulation.health.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

// Saliva Ferning Analysis Data
@Entity(tableName = "ferning_analysis")
data class FerningAnalysis(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val testDate: LocalDateTime,
    val imageUri: String, // Path to the captured image
    val ferningPattern: FerningPattern, // NONE, PARTIAL, FULL
    val confidence: Float, // 0-1 confidence score
    val notes: String = ""
) {
    enum class FerningPattern {
        NONE,      // No ferning
        PARTIAL,   // Partial ferning pattern
        FULL       // Full ferning pattern (indicates high estrogen)
    }
}

// Voice Analysis Data
@Entity(tableName = "voice_analysis")
data class VoiceAnalysis(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val testDate: LocalDateTime,
    val audioUri: String, // Path to recorded audio
    val fundamentalFrequency: Float, // Hz
    val frequencyShift: Float, // Hz change from baseline
    val shimmer: Float, // Voice quality metric
    val jitter: Float, // Voice stability metric
    val notes: String = ""
)

// Body Temperature Data
@Entity(tableName = "temperature_data")
data class TemperatureData(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val testDate: LocalDateTime,
    val temperature: Float, // Celsius
    val measurementMethod: String, // BATTERY, TOUCH_SCREEN, etc.
    val accuracy: Float, // Estimated measurement accuracy
    val notes: String = ""
)

// Heart Rate and HRV Data
@Entity(tableName = "cardiac_data")
data class CardiacData(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val testDate: LocalDateTime,
    val heartRate: Int, // BPM
    val heartRateVariability: Float, // HRV in ms
    val oxygenSaturation: Float = 0f, // SpO2 percentage
    val notes: String = ""
)

// Ovulation Prediction Result
@Entity(tableName = "ovulation_predictions")
data class OvulationPrediction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val predictionDate: LocalDateTime,
    val estimatedOvulationDate: LocalDateTime,
    val confidence: Float, // 0-1 confidence score
    val ferningScore: Float,
    val voiceScore: Float,
    val temperatureScore: Float,
    val cardiacScore: Float,
    val compositeScore: Float, // Combined score
    val status: PredictionStatus, // PREDICTED, CONFIRMED, PASSED
    val notes: String = ""
) {
    enum class PredictionStatus {
        PREDICTED,  // Ovulation is predicted
        CONFIRMED,  // Ovulation is confirmed by multiple indicators
        PASSED      // The predicted window has passed
    }
}

// Window of Implantation Prediction
@Entity(tableName = "implantation_windows")
data class ImplantationWindow(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val predictionDate: LocalDateTime,
    val estimatedStartDate: LocalDateTime,
    val estimatedEndDate: LocalDateTime,
    val confidence: Float, // 0-1 confidence score
    val biomarkers: String, // JSON encoded biomarker data
    val status: WindowStatus,
    val notes: String = ""
) {
    enum class WindowStatus {
        PREDICTED,  // Window is predicted
        ACTIVE,     // Window is currently active
        PASSED      // Window has passed
    }
}

// Daily Health Summary
@Entity(tableName = "daily_health_summary")
data class DailyHealthSummary(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
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
    enum class MenstrualPhase {
        MENSTRUATION,
        FOLLICULAR,
        OVULATION,
        LUTEAL
    }
}

// Admin Report Data
@Entity(tableName = "admin_reports")
data class AdminReport(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val reportDate: LocalDateTime,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val userId: String,
    val reportData: String, // JSON encoded report data
    val graphData: String, // JSON encoded graph data
    val synced: Boolean = false,
    val syncedDate: LocalDateTime? = null
)
