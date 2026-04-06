package com.ovulation.health.data.db

import androidx.room.TypeConverter
import com.ovulation.health.data.model.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DateTimeConverters {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @TypeConverter fun fromLocalDateTime(v: LocalDateTime?): String? = v?.format(formatter)
    @TypeConverter fun toLocalDateTime(v: String?): LocalDateTime? = v?.let { LocalDateTime.parse(it, formatter) }
}

class EnumConverters {
    @TypeConverter fun fromFerning(v: FerningAnalysis.FerningPattern?): String? = v?.name
    @TypeConverter fun toFerning(v: String?): FerningAnalysis.FerningPattern? = v?.let { FerningAnalysis.FerningPattern.valueOf(it) }

    @TypeConverter fun fromPredStatus(v: OvulationPrediction.PredictionStatus?): String? = v?.name
    @TypeConverter fun toPredStatus(v: String?): OvulationPrediction.PredictionStatus? = v?.let { OvulationPrediction.PredictionStatus.valueOf(it) }

    @TypeConverter fun fromWinStatus(v: ImplantationWindow.WindowStatus?): String? = v?.name
    @TypeConverter fun toWinStatus(v: String?): ImplantationWindow.WindowStatus? = v?.let { ImplantationWindow.WindowStatus.valueOf(it) }

    @TypeConverter fun fromPhase(v: DailyHealthSummary.MenstrualPhase?): String? = v?.name
    @TypeConverter fun toPhase(v: String?): DailyHealthSummary.MenstrualPhase? = v?.let { DailyHealthSummary.MenstrualPhase.valueOf(it) }

    @TypeConverter fun fromRole(v: UserRole?): String? = v?.name
    @TypeConverter fun toRole(v: String?): UserRole? = v?.let { UserRole.valueOf(it) }
}
