package com.e.btex.connection

import android.app.Service
import android.bluetooth.BluetoothAdapter.getDefaultAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.os.bundleOf
import com.e.btex.data.dto.Sensors
import com.e.btex.ui.common.DeviceStateListener
import timber.log.Timber
import java.util.*

class BTService : Service() {

    companion object {
        const val ACTION_START_CONNECTING = "com.e.btex.connection.bt.service.action.START_CONNECTING"
        const val ACTION_FAILED_CONNECTING = "com.e.btex.connection.bt.service.action.FAILED_CONNECTING"
        const val ACTION_CREATE_CONNECTION = "com.e.btex.connection.bt.service.action.CREATE_CONNECTION"
        const val ACTION_DESTROY_CONNECTION = "com.e.btex.connection.bt.service.action.DESTROY_CONNECTION"
        const val ACTION_RECEIVE_DATA = "com.e.btex.connection.bt.service.action.RECEIVE_DATA"
        const val EXTRA_DATA = "com.e.btex.connection.bt.service.extra.DATA"
        const val EXTRA_DATA_SIZE = "com.e.btex.connection.bt.service.extra.DATA_SIZE"
    }

    // Binder given to clients
    private val mBinder = LocalBinder()

    private lateinit var bluetoothConnectionService: BluetoothConnectionService

    // Random number generator

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        val service: BTService
            get() = this@BTService
    }

    override fun onBind(intent: Intent): IBinder {
        Timber.d("onBind")
        return mBinder
    }


    override fun onDestroy() {
        super.onDestroy()
        Timber.d("onDestroy")
        bluetoothConnectionService.cancel()
    }


    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate")

        bluetoothConnectionService = BluetoothConnectionService(getDefaultAdapter()).apply {

            setBTConnectionListener(object : DeviceStateListener {
                override fun onStartConnecting() = sendBroadcast(Intent(ACTION_START_CONNECTING))

                override fun onFailedConnecting() = sendBroadcast(Intent(ACTION_FAILED_CONNECTING))

                override fun onCreateConnection() = sendBroadcast(Intent(ACTION_CREATE_CONNECTION))

                override fun onDestroyConnection() = sendBroadcast(Intent(ACTION_DESTROY_CONNECTION))

                override fun onReceiveData(bytes: ByteArray, size: Int) {
                    val intent = Intent(ACTION_RECEIVE_DATA).apply {
                        putExtra(EXTRA_DATA_SIZE,size)
                        putExtra(EXTRA_DATA,bytes)
                    }
                    sendBroadcast(intent)
                }

            })
            //start()
        }

    }

    fun startClient(device: BluetoothDevice) {
        bluetoothConnectionService.startClient(device)
    }


}
