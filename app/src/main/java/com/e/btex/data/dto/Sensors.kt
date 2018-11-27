package com.e.btex.data.dto

import android.content.res.Resources

class Sensors(temperature: Float = 0.0f,
                    humidity: Float = 0.0f,
                    co2: Float = 0.0f,
                    pm1: Float = 0.0f,
                    pm10: Float = 0.0f,
                    pm25: Float = 0.0f,
                    tvoc: Float = 0.0f,
              val res: Resources? =null
) {

    val temperature =  Sensor.Temperature(temperature)
    val humidity = Sensor.Humidity(humidity)
    val co2 = Sensor.Co2(co2)
    val pm1 = Sensor.Pm1(pm1)
    val pm10 =  Sensor.Pm10(pm10)
    val pm25 =  Sensor.Pm25(pm25)
    val tvoc =  Sensor.Tvoc(tvoc)

    fun getSensorList() = listOf(
            temperature,
            humidity,
            co2,
            pm1,
            pm10,
            pm25,
            tvoc)

}