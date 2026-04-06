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

    @Query("SELECT * FROM ferning_analysis WHERE subjectId = :subjectId AND testDate BETWEEN :from AND :to ORDER BY testDate DESC")
    fun getForSubject(subjectId: String, from: LocalDateTime, to: LocalDateTime): Flow<List<FerningAnalysis>>

    @Query("SELECT * FROM ferning_analysis WHERE subjectId = :subjectId ORDER BY testDate DESC LIMIT 1")
    suspend fun getLatestForSubject(subjectId: String): FerningAnalysis?
}

@Dao
interface VoiceAnalysisDao {
    @Insert
    suspend fun insert(analysis: VoiceAnalysis): Long

    @Update
    suspend fun update(analysis: VoiceAnalysis)

    @Query("SELECT * FROM voice_analysis WHERE subjectId = :subjectId AND testDate BETWEEN :from AND :to ORDER BY testDate DESC")
    fun getForSubject(subjectId: String, from: LocalDateTime, to: LocalDateTime): Flow<List<VoiceAnalysis>>

    @Query("SELECT * FROM voice_analysis WHERE subjectId = :subjectId ORDER BY testDate DESC LIMIT 1")
    suspend fun getLatestForSubject(subjectId: String): VoiceAnalysis?
}

@Dao
interface TemperatureDataDao {
    @Insert
    suspend fun insert(data: TemperatureData): Long

    @Update
    suspend fun update(data: TemperatureData)

    @Query("SELECT * FROM temperature_data WHERE subjectId = :subjectId AND testDate BETWEEN :from AND :to ORDER BY testDate DESC")
    fun getForSubject(subjectId: String, from: LocalDateTime, to: LocalDateTime): Flow<List<TemperatureData>>

    @Query("SELECT * FROM temperature_data WHERE subjectId = :subjectId ORDER BY testDate DESC LIMIT 1")
    suspend fun getLatestForSubject(subjectId: String): TemperatureData?
}

@Dao
interface CardiacDataDao {
    @Insert
    suspend fun insert(data: CardiacData): Long

    @Update
    suspend fun update(data: CardiacData)

    @Query("SELECT * FROM cardiac_data WHERE subjectId = :subjectId AND testDate BETWEEN :from AND :to ORDER BY testDate DESC")
    fun getForSubject(subjectId: String, from: LocalDateTime, to: LocalDateTime): Flow<List<CardiacData>>

    @Query("SELECT * FROM cardiac_data WHERE subjectId = :subjectId ORDER BY testDate DESC LIMIT 1")
    suspend fun getLatestForSubject(subjectId: String): CardiacData?
}

@Dao
interface OvulationPredictionDao {
    @Insert
    suspend fun insert(prediction: OvulationPrediction): Long

    @Update
    suspend fun update(prediction: OvulationPrediction)

    @Query("SELECT * FROM ovulation_predictions WHERE subjectId = :subjectId AND predictionDate BETWEEN :from AND :to ORDER BY predictionDate DESC")
    fun getForSubject(subjectId: String, from: LocalDateTime, to: LocalDateTime): Flow<List<OvulationPrediction>>

    @Query("SELECT * FROM ovulation_predictions WHERE subjectId = :subjectId ORDER BY predictionDate DESC LIMIT 1")
    suspend fun getLatestForSubject(subjectId: String): OvulationPrediction?

    // Admin: get latest prediction for every subject in a list
    @Query("SELECT * FROM ovulation_predictions WHERE subjectId IN (:subjectIds) ORDER BY predictionDate DESC")
    fun getLatestForSubjects(subjectIds: List<String>): Flow<List<OvulationPrediction>>
}

@Dao
interface ImplantationWindowDao {
    @Insert
    suspend fun insert(window: ImplantationWindow): Long

    @Update
    suspend fun update(window: ImplantationWindow)

    @Query("SELECT * FROM implantation_windows WHERE subjectId = :subjectId AND predictionDate BETWEEN :from AND :to ORDER BY predictionDate DESC")
    fun getForSubject(subjectId: String, from: LocalDateTime, to: LocalDateTime): Flow<List<ImplantationWindow>>

    @Query("SELECT * FROM implantation_windows WHERE subjectId = :subjectId ORDER BY predictionDate DESC LIMIT 1")
    suspend fun getLatestForSubject(subjectId: String): ImplantationWindow?
}

@Dao
interface DailyHealthSummaryDao {
    @Insert
    suspend fun insert(summary: DailyHealthSummary): Long

    @Update
    suspend fun update(summary: DailyHealthSummary)

    @Query("SELECT * FROM daily_health_summary WHERE subjectId = :subjectId AND date BETWEEN :from AND :to ORDER BY date DESC")
    fun getForSubject(subjectId: String, from: LocalDateTime, to: LocalDateTime): Flow<List<DailyHealthSummary>>

    @Query("SELECT * FROM daily_health_summary WHERE subjectId = :subjectId ORDER BY date DESC LIMIT 1")
    suspend fun getLatestForSubject(subjectId: String): DailyHealthSummary?
}

@Dao
interface AdminReportDao {
    @Insert
    suspend fun insert(report: AdminReport): Long

    @Update
    suspend fun update(report: AdminReport)

    // Admin: all reports for subjects under their management
    @Query("SELECT * FROM admin_reports WHERE adminId = :adminId ORDER BY reportDate DESC")
    fun getForAdmin(adminId: String): Flow<List<AdminReport>>

    // Admin: reports for one specific subject
    @Query("SELECT * FROM admin_reports WHERE adminId = :adminId AND subjectId = :subjectId ORDER BY reportDate DESC")
    fun getForSubjectUnderAdmin(adminId: String, subjectId: String): Flow<List<AdminReport>>

    @Query("SELECT * FROM admin_reports WHERE synced = 0 ORDER BY reportDate ASC")
    suspend fun getUnsynced(): List<AdminReport>

    @Query("SELECT * FROM admin_reports WHERE adminId = :adminId ORDER BY reportDate DESC LIMIT 1")
    suspend fun getLatestForAdmin(adminId: String): AdminReport?
}
