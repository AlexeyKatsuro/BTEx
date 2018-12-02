package com.e.btex.data.dto

import com.e.btex.data.SensorsType


data class Sensors(val temperature: Number,
                   val humidity: Number,
                   val co2: Number,
                   val pm1: Number,
                   val pm10: Number,
                   val pm25: Number,
                   val tvoc: Number) {
    fun getSensorMap(): Map<SensorsType, Number> {
        return mapOf(
                SensorsType.temperature to temperature,
                SensorsType.humidity to humidity,
                SensorsType.co2 to co2,
                SensorsType.pm1 to pm1,
                SensorsType.pm10 to pm10,
                SensorsType.pm25 to pm25,
                SensorsType.tvoc to tvoc
        )
    }
}