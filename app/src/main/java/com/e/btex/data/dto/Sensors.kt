package com.e.btex.data.dto

import android.content.res.Resources

class Sensors(temperature: Number,
                    humidity: Number,
                    co2: Number,
                    pm1: Number,
                    pm10: Number,
                    pm25: Number,
                    tvoc: Number
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

    override fun toString(): String {
        return "Sensors(temperature=$temperature, humidity=$humidity, co2=$co2, pm1=$pm1, pm10=$pm10, pm25=$pm25, tvoc=$tvoc)"
    }


}