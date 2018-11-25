package com.e.btex.utils.plot

import java.util.*

class DynamicXYDataSource: Runnable {
    // encapsulates management of the observers watching this datasource for update events:

    var startTime = Date().time

    inner class MyObservable : Observable() {

       fun change(){
           setChanged()
       }
    }


    var list: MutableList<Int> = ArrayList()

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

                list.add(Random().nextInt(10))
                if (list.size > 100) {
                    list.clear()
                }

                notifier.change()
                notifier.notifyObservers("")


            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }

    fun getItemCount(series: Int): Int {
        return list.size
    }

    fun getX(series: Int, index: Int): Number {
        return index
    }

    fun getY(series: Int, index: Int): Number {
        return list[index]
    }

    fun addObserver(observer: Observer) {
        notifier.addObserver(observer)
    }

    fun removeObserver(observer: Observer) {
        notifier.deleteObserver(observer)
    }
}