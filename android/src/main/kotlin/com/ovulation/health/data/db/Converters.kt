package com.ovulation.health.data.db

import androidx.room.TypeConverter
import com.ovulation.health.data.model.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DateTimeConverters {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? {
        return value?.format(formatter)
    }

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it, formatter) }
    }
}

class EnumConverters {
    @TypeConverter
    fun fromFerningPattern(value: FerningAnalysis.FerningPattern?): String? {
        return value?.name
    }

    @TypeConverter
    fun toFerningPattern(value: String?): FerningAnalysis.FerningPattern? {
        return value?.let { FerningAnalysis.FerningPattern.valueOf(it) }
    }

    @TypeConverter
    fun fromPredictionStatus(value: OvulationPrediction.PredictionStatus?): String? {
        return value?.name
    }

    @TypeConverter
    fun toPredictionStatus(value: String?): OvulationPrediction.PredictionStatus? {
        return value?.let { OvulationPrediction.PredictionStatus.valueOf(it) }
    }

    @TypeConverter
    fun fromWindowStatus(value: ImplantationWindow.WindowStatus?): String? {
        return value?.name
    }

    @TypeConverter
    fun toWindowStatus(value: String?): ImplantationWindow.WindowStatus? {
        return value?.let { ImplantationWindow.WindowStatus.valueOf(it) }
    }

    @TypeConverter
    fun fromMenstrualPhase(value: DailyHealthSummary.MenstrualPhase?): String? {
        return value?.name
    }

    @TypeConverter
    fun toMenstrualPhase(value: String?): DailyHealthSummary.MenstrualPhase? {
        return value?.let { DailyHealthSummary.MenstrualPhase.valueOf(it) }
    }
}
