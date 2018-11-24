package com.e.btex.connection

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Handler
import com.e.btex.ui.common.BtConnectionListener
import timber.log.Timber
import java.io.IOException
import java.util.*

class ConnectThread(private val device: BluetoothDevice,
                    private  val uuid: UUID,
                    private val bluetoothAdapter: BluetoothAdapter,
                    private val handler: Handler,
                    private val listner: BtConnectionListener?,
                    private val onConneted: (BluetoothSocket)->Unit) : Thread() {

    private lateinit var socket: BluetoothSocket


    @Throws(IOException::class)
    private fun createBluetoothSocket(device: BluetoothDevice): BluetoothSocket? {
        try {
            val m = device.javaClass.getMethod("createInsecureRfcommSocketToServiceRecord", UUID::class.java)
            return m.invoke(device, uuid) as BluetoothSocket
        } catch (e: Exception) {
            Timber.e(e, "Could not create Insecure RFComm Connection ${e.message}")
        }

        return device.createRfcommSocketToServiceRecord(uuid)
    }

    override fun run() {
        Timber.i("ConnectThread: started.")
        handler.post {
            listner?.onStartConnecting()
        }
        // Get a BluetoothSocket for a connection with the
        // given BluetoothDevice
        try {
            Timber.i("ConnectThread: Trying to create InsecureRfcommSocket using UUID: $uuid")
            socket = createBluetoothSocket(device)?: throw  IOException("socket is NULL")
        } catch (e: IOException) {
            Timber.e(e, "ConnectThread: Could not create InsecureRfcommSocket ${e.message}")
        }

        // Always cancel discovery because it will slow down a connection
        bluetoothAdapter.cancelDiscovery()

        // Make a connection to the BluetoothSocket

        try {
            // This is a blocking call and will only return on a
            // successful connection or an exception
            socket.connect()
            Timber.i("run: ConnectThread connected.")
            onConneted(socket)
        } catch (e: IOException) {
            // Close the socket
            Timber.e(e, "run: ConnectThread connecting is Failed")
            cancel()
            handler.post {
                listner?.onFailedConnecting()
            }

            Timber.i("run: ConnectThread: Could not connect to UUID: $uuid")
        }
    }


    fun cancel() {
        try {
            Timber.i("cancel: Closing Client Socket.")
            socket.close()

        } catch (e: IOException) {
            Timber.e(e, "cancel: close() of socket in Connectthread failed. ${e.message}")
        }

    }
}