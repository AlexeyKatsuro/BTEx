package com.e.btex.utils.plot

import android.content.Context
import com.e.btex.data.SensorsType
import com.e.btex.data.dto.Sensors
import com.e.btex.utils.extensions.getName

class SensorDataSource(val context: Context) : XYDataSource {

    var sensorsType: SensorsType = SensorsType.temperature

    val sensordsData = mutableMapOf<SensorsType, MutableList<Number>>()

    var maxSize = 10

    init {
        sensordsData
    }

    private val currentValues: List<Number>
        get() = sensordsData[sensorsType]?: emptyList()

    override fun getItemCount(seriesIndex: Int) = currentValues.size

    override fun getX(seriesIndex: Int, index: Int) = index

    override fun getY(seriesIndex: Int, index: Int) = currentValues[index]

    override fun getTitle(seriesIndex: Int) = sensorsType.getName(context.resources)


    fun addData(sensors: Sensors) {
        sensordsData.addToList(sensors.getSensorMap())
        restrictSize(maxSize)
    }

    private fun restrictSize(size: Int) {
        sensordsData.keys.forEach { key ->
            sensordsData[key]!!.apply {
                while (size > size)
                    removeAt(0)
            }
        }
    }

    private fun <T, V> MutableMap<T, MutableList<V>>.addToList(map: Map<T, V>) {
        map.keys.forEach { key ->
            if (this.containsKey(key)) {
                this[key]!!.add(map[key]!!)
            } else {
                this[key] = mutableListOf(map[key]!!)
            }
        }
    }
}

