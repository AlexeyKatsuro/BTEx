package com.e.btex.data.dto

data class Sensors(var temperature: Float = 0.0f,
                   var humidity: Float = 0.0f,
                   var co2: Float = 0.0f,
                   var  pm1: Float = 0.0f,
                   var pm10: Float = 0.0f,
                   var pm25: Float = 0.0f,
                   var tvoc: Float = 0.0f
)