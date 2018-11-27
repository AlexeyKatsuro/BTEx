package com.e.btex.connection

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import timber.log.Timber
import java.io.IOException
import java.util.*


/**
 * This thread runs while listening for incoming connections. It behaves
 * like a server-side client. It runs until a connection is accepted
 * (or until cancelled).
 */
class AcceptThread(appName: String,
                   uuidInsecure: UUID,
                   bluetoothAdapter: BluetoothAdapter,
                   private val onConneted: (BluetoothSocket)->Unit) : Thread() {

    // The local server socket
    private val mmServerSocket: BluetoothServerSocket?

    init {
        var tmp: BluetoothServerSocket? = null

        // Create a new listening server socket
        try {
            tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, uuidInsecure)

            Timber.d("AcceptThread: Setting up Server using: $uuidInsecure")
        } catch (e: IOException) {
            Timber.e(e,"AcceptThread: IOException: ${e.message}")
        }

        mmServerSocket = tmp
    }



    override fun run() {
        Timber.d("run: AcceptThread Running.")

        var socket: BluetoothSocket? = null

        try {
            // This is a blocking call and will only return on a
            // successful connection or an exception
            Timber.d("run: RFCOM server socket start.....")

            socket = mmServerSocket!!.accept()

            Timber.d("run: RFCOM server socket accepted connection.")

        } catch (e: IOException) {
            Timber.e(e, ("AcceptThread: IOException: ${e.message}"))
        }

        //talk about this is in the 3rd
        if (socket != null) {
            onConneted.invoke(socket)
        }

        Timber.d("END mAcceptThread ")
    }

    fun cancel() {
        Timber.d("cancel: Canceling AcceptThread.")
        try {
            mmServerSocket!!.close()
        } catch (e: IOException) {
            Timber.e(e, "cancel: Close of AcceptThread ServerSocket failed.  + ${e.message}")
        }

    }

}