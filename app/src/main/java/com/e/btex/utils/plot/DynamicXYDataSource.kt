package com.e.btex.utils.plot

import android.os.Handler
import com.e.btex.data.dto.Sensors
import java.util.*

class DynamicXYDataSource(val handler: Handler) : Runnable {
    // encapsulates management of the observers watching this datasource for update events:

    var startTime = Date().time

    private var onUpdateCallback: ((Sensors) -> Unit)? = null

    inner class MyObservable : Observable() {

        fun change() {
            setChanged()
        }
    }

    var title: String = "Temperature"


    var list: MutableList<Sensors> = ArrayList()

    private var notifier = MyObservable()
    private var keepRunning = false


    fun stopThread() {
        keepRunning = false
    }

    //@Override
    override fun run() {
        try {
            keepRunning = true
            while (keepRunning) {

                Thread.sleep(500) // decrease or remove to speed up the refresh rate.
                handler.post {
                    update()
                }


            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }

    fun update() {


        val  sensors = createMockSensors()

        list.add(sensors)
        if (list.size > 20) {
            list.removeAt(0)
        }

        onUpdateCallback?.invoke(sensors)

    }

    fun setOnUpdateCallback(callback: (Sensors)->Unit){
        onUpdateCallback = callback
    }

    private fun createMockSensors() = Sensors(
            Random().nextInt(35).toFloat(),
            Random().nextInt(1000).toFloat(),
            Random().nextInt(325).toFloat(),
            Random().nextInt(100).toFloat(),
            Random().nextInt(10).toFloat(),
            Random().nextInt(50).toFloat(),
            Random().nextInt(5).toFloat())

    fun getItemCount(series: Int): Int {
        return list.size
    }

    fun getX(series: Int, index: Int): Number {
        return index
    }

    fun getY(series: Int, index: Int): Number {
        return list[index].temperature.value
    }

    fun getTitle(series: Int) = title

    fun addObserver(observer: Observer) {
        notifier.addObserver(observer)
    }

    fun removeObserver(observer: Observer) {
        notifier.deleteObserver(observer)
    }
}