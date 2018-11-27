package com.e.btex.connection

import android.app.Service
import android.bluetooth.BluetoothAdapter.getDefaultAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import timber.log.Timber

class MyService : Service() {
    // Binder given to clients
    private val mBinder = LocalBinder()

    var bluetoothConnectionService: BluetoothConnectionService? = null

    // Random number generator

    private var callback: (()->Unit)? = null

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        val service: MyService
            get() = this@MyService
    }

    override fun onBind(intent: Intent): IBinder {
        Timber.d("onBind")
        return mBinder
    }


    fun setCallback(cb: ()->Unit){
        callback = cb
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("onDestroy")
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate")

    }

    fun startClient(device: BluetoothDevice) {
        bluetoothConnectionService?.startClient(device)
    }


}
