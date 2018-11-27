package com.e.btex.data.dto

sealed class Sensor(val  value: Float){

    class Temperature(value: Float): Sensor(value)
    class Humidity(value: Float): Sensor(value)
    class Co2(value: Float): Sensor(value)
    class Pm1(value: Float): Sensor(value)
    class Pm10(value: Float): Sensor(value)
    class Pm25(value: Float): Sensor(value)
    class Tvoc(value: Float): Sensor(value)


}