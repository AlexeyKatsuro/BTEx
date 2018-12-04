package com.e.btex.connection

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.e.btex.ui.common.DeviceStateListener
import timber.log.Timber
import java.io.IOException
import java.util.*

class ConnectThread(private val device: BluetoothDevice,
                    private val uuid: UUID,
                    private val bluetoothAdapter: BluetoothAdapter,
                    private val listner: DeviceStateListener?,
                    private val onConneted: (BluetoothSocket) -> Unit) : Thread() {

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
        Timber.d("ConnectThread: started.")
        listner?.onStartConnecting()
        // Get a BluetoothSocket for a connection with the
        // given BluetoothDevice
        try {
            Timber.d("ConnectThread: Trying to create InsecureRfcommSocket using UUID: $uuid")
            socket = createBluetoothSocket(device) ?: throw  IOException("socket is NULL")
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
            listner?.onCreateConnection(device)
            Timber.d("run: ConnectThread connected.")
            onConneted(socket)
        } catch (e: IOException) {
            // Close the socket
            Timber.e(e, "run: ConnectThread connecting is Failed")
            cancel()
            listner?.onFailedConnecting()
            Timber.d("run: ConnectThread: Could not connect to UUID: $uuid")
        }
    }


    fun cancel() {
        try {
            Timber.d("cancel: Closing Client Socket.")
            socket.close()

        } catch (e: IOException) {
            Timber.e(e, "cancel: close() of socket in Connectthread failed. ${e.message}")
        }

    }
}