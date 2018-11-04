package com.e.btex.connection

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import timber.log.Timber
import java.io.IOException
import java.util.*

class ConnectThread(private val device: BluetoothDevice,
                    private  val uuid: UUID,
                    private val bluetoothAdapter: BluetoothAdapter,
                    private val onConneted: (BluetoothSocket)->Unit) : Thread() {

    private lateinit var socket: BluetoothSocket


    override fun run() {
        Timber.i("ConnectThread: started.")

        // Get a BluetoothSocket for a connection with the
        // given BluetoothDevice
        try {
            Timber.i("ConnectThread: Trying to create InsecureRfcommSocket using UUID: $uuid")
            socket = device.createRfcommSocketToServiceRecord(uuid)?: throw  IOException("socket is NULL")
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
        } catch (e: IOException) {
            // Close the socket
            try {
                socket.close()
                Timber.i("run: Closed Socket.")
            } catch (e1: IOException) {
                Timber.e(e, "mConnectThread: run: Unable to close connection in socket ${e1.message}")
            }

            Timber.i("run: ConnectThread: Could not connect to UUID: $uuid")
        }


        onConneted(socket)
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