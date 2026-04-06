package com.ovulation.health.data.db

import androidx.room.*
import com.ovulation.health.data.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface FerningAnalysisDao {
    @Insert
    suspend fun insert(analysis: FerningAnalysis): Long

    @Update
    suspend fun update(analysis: FerningAnalysis)

    @Delete
    suspend fun delete(analysis: FerningAnalysis)

    @Query("SELECT * FROM ferning_analysis WHERE testDate BETWEEN :startDate AND :endDate ORDER BY testDate DESC")
    fun getAnalysisInRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<FerningAnalysis>>

    @Query("SELECT * FROM ferning_analysis WHERE id = :id")
    suspend fun getById(id: Int): FerningAnalysis?

    @Query("SELECT * FROM ferning_analysis ORDER BY testDate DESC LIMIT 1")
    suspend fun getLatest(): FerningAnalysis?
}

@Dao
interface VoiceAnalysisDao {
    @Insert
    suspend fun insert(analysis: VoiceAnalysis): Long

    @Update
    suspend fun update(analysis: VoiceAnalysis)

    @Delete
    suspend fun delete(analysis: VoiceAnalysis)

    @Query("SELECT * FROM voice_analysis WHERE testDate BETWEEN :startDate AND :endDate ORDER BY testDate DESC")
    fun getAnalysisInRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<VoiceAnalysis>>

    @Query("SELECT * FROM voice_analysis WHERE id = :id")
    suspend fun getById(id: Int): VoiceAnalysis?

    @Query("SELECT * FROM voice_analysis ORDER BY testDate DESC LIMIT 1")
    suspend fun getLatest(): VoiceAnalysis?
}

@Dao
interface TemperatureDataDao {
    @Insert
    suspend fun insert(data: TemperatureData): Long

    @Update
    suspend fun update(data: TemperatureData)

    @Delete
    suspend fun delete(data: TemperatureData)

    @Query("SELECT * FROM temperature_data WHERE testDate BETWEEN :startDate AND :endDate ORDER BY testDate DESC")
    fun getDataInRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<TemperatureData>>

    @Query("SELECT * FROM temperature_data WHERE id = :id")
    suspend fun getById(id: Int): TemperatureData?

    @Query("SELECT * FROM temperature_data ORDER BY testDate DESC LIMIT 1")
    suspend fun getLatest(): TemperatureData?
}

@Dao
interface CardiacDataDao {
    @Insert
    suspend fun insert(data: CardiacData): Long

    @Update
    suspend fun update(data: CardiacData)

    @Delete
    suspend fun delete(data: CardiacData)

    @Query("SELECT * FROM cardiac_data WHERE testDate BETWEEN :startDate AND :endDate ORDER BY testDate DESC")
    fun getDataInRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<CardiacData>>

    @Query("SELECT * FROM cardiac_data WHERE id = :id")
    suspend fun getById(id: Int): CardiacData?

    @Query("SELECT * FROM cardiac_data ORDER BY testDate DESC LIMIT 1")
    suspend fun getLatest(): CardiacData?
}

@Dao
interface OvulationPredictionDao {
    @Insert
    suspend fun insert(prediction: OvulationPrediction): Long

    @Update
    suspend fun update(prediction: OvulationPrediction)

    @Delete
    suspend fun delete(prediction: OvulationPrediction)

    @Query("SELECT * FROM ovulation_predictions WHERE predictionDate BETWEEN :startDate AND :endDate ORDER BY predictionDate DESC")
    fun getPredictionsInRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<OvulationPrediction>>

    @Query("SELECT * FROM ovulation_predictions ORDER BY predictionDate DESC LIMIT 1")
    suspend fun getLatestPrediction(): OvulationPrediction?

    @Query("SELECT * FROM ovulation_predictions WHERE status = :status ORDER BY predictionDate DESC")
    fun getPredictionsByStatus(status: OvulationPrediction.PredictionStatus): Flow<List<OvulationPrediction>>
}

@Dao
interface ImplantationWindowDao {
    @Insert
    suspend fun insert(window: ImplantationWindow): Long

    @Update
    suspend fun update(window: ImplantationWindow)

    @Delete
    suspend fun delete(window: ImplantationWindow)

    @Query("SELECT * FROM implantation_windows WHERE predictionDate BETWEEN :startDate AND :endDate ORDER BY predictionDate DESC")
    fun getWindowsInRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<ImplantationWindow>>

    @Query("SELECT * FROM implantation_windows ORDER BY predictionDate DESC LIMIT 1")
    suspend fun getLatestWindow(): ImplantationWindow?

    @Query("SELECT * FROM implantation_windows WHERE status = :status ORDER BY predictionDate DESC")
    fun getWindowsByStatus(status: ImplantationWindow.WindowStatus): Flow<List<ImplantationWindow>>
}

@Dao
interface DailyHealthSummaryDao {
    @Insert
    suspend fun insert(summary: DailyHealthSummary): Long

    @Update
    suspend fun update(summary: DailyHealthSummary)

    @Delete
    suspend fun delete(summary: DailyHealthSummary)

    @Query("SELECT * FROM daily_health_summary WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getSummariesInRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<DailyHealthSummary>>

    @Query("SELECT * FROM daily_health_summary ORDER BY date DESC LIMIT 1")
    suspend fun getLatestSummary(): DailyHealthSummary?
}

@Dao
interface AdminReportDao {
    @Insert
    suspend fun insert(report: AdminReport): Long

    @Update
    suspend fun update(report: AdminReport)

    @Delete
    suspend fun delete(report: AdminReport)

    @Query("SELECT * FROM admin_reports WHERE reportDate BETWEEN :startDate AND :endDate ORDER BY reportDate DESC")
    fun getReportsInRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<AdminReport>>

    @Query("SELECT * FROM admin_reports WHERE synced = 0 ORDER BY reportDate ASC")
    suspend fun getUnsyncedReports(): List<AdminReport>

    @Query("SELECT * FROM admin_reports ORDER BY reportDate DESC LIMIT 1")
    suspend fun getLatestReport(): AdminReport?
}
