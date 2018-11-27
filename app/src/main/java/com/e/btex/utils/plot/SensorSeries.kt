package com.e.btex.utils.plot

import com.androidplot.xy.XYSeries
import com.e.btex.data.dto.Sensor
import com.e.btex.data.dto.Sensors
import kotlin.reflect.KClass

class SensorSeries(var sensorType: KClass<out Sensor>, private val sensorList: MutableList<List<Sensor>> = mutableListOf(), var maxSize: Int = 10, var name: String ="") : XYSeries {

    private val suggestList: List<Sensor>
        get() =  getSuggest()

    override fun getTitle() = name

    override fun size(): Int {
        return sensorList.size
    }

    override fun getX(index: Int): Number {
        return index
    }

    override fun getY(index: Int): Number {
        return suggestList[index].value
    }

    fun addSensorVal(sensor: Sensors) {
        if (sensorList.size > maxSize) {
            sensorList.removeAt(0)
        }
        sensorList.add(sensor.getSensorList())
    }


    fun clear() {
        sensorList.clear()
    }

    fun setSensorClass(clazz: KClass<out Sensor>) {
        sensorType = clazz
    }

    private fun getSuggest() = sensorList.flatten().filter {
        it::class == sensorType
    }


}