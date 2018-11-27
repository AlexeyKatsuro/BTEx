package com.e.btex.data.dto

sealed class Sensor(val  value: Number) {

    class Temperature(value: Number) : Sensor(value)
    class Humidity(value: Number) : Sensor(value)
    class Co2(value: Number) : Sensor(value)
    class Pm1(value: Number) : Sensor(value)
    class Pm10(value: Number) : Sensor(value)
    class Pm25(value: Number) : Sensor(value)
    class Tvoc(value: Number) : Sensor(value)

    override fun toString(): String {
        return "Sensor(${this::class.java.simpleName} value=$value)"
    }


}