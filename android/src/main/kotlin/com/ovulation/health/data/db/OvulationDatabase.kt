package com.ovulation.health.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ovulation.health.data.model.*

@Database(
    entities = [
        User::class,
        AuthSession::class,
        FerningAnalysis::class,
        VoiceAnalysis::class,
        TemperatureData::class,
        CardiacData::class,
        OvulationPrediction::class,
        ImplantationWindow::class,
        DailyHealthSummary::class,
        AdminReport::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(DateTimeConverters::class, EnumConverters::class)
abstract class OvulationDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun authSessionDao(): AuthSessionDao
    abstract fun ferningAnalysisDao(): FerningAnalysisDao
    abstract fun voiceAnalysisDao(): VoiceAnalysisDao
    abstract fun temperatureDataDao(): TemperatureDataDao
    abstract fun cardiacDataDao(): CardiacDataDao
    abstract fun ovulationPredictionDao(): OvulationPredictionDao
    abstract fun implantationWindowDao(): ImplantationWindowDao
    abstract fun dailyHealthSummaryDao(): DailyHealthSummaryDao
    abstract fun adminReportDao(): AdminReportDao
}
