package com.e.btex.connection

import android.bluetooth.BluetoothAdapter
import java.util.*
import android.bluetooth.BluetoothSocket
import timber.log.Timber
import android.bluetooth.BluetoothDevice
import com.e.btex.ui.common.DeviceStateListener
import kotlin.properties.Delegates


class BluetoothConnectionService(private val bluetoothAdapter: BluetoothAdapter) {

    private val appName = "BTEx"
    private val uuidInsecure = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")//UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")

    private var mInsecureAcceptThread: AcceptThread? = null

    private var mConnectThread: ConnectThread? by Delegates.observable<ConnectThread?>(null){
        _, old, new ->
        old?.cancel()
    }
    private var mConnectedThread: ConnectedThread? by Delegates.observable<ConnectedThread?>(null){
        _, old, new ->
        old?.cancel()
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    @Synchronized
    fun start() {
        Timber.d("start")

        // Cancel any thread attempting to make a connection
        mConnectThread = null

        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = AcceptThread(appName,uuidInsecure,bluetoothAdapter){
                connected(it)
            }
            mInsecureAcceptThread!!.start()
        }
    }

    /**
     * AcceptThread starts and sits waiting for a connection.
     * Then ConnectThread starts and attempts to make a connection with the other devices AcceptThread.
     */

    fun startClient(device: BluetoothDevice) {
        Timber.d("startClient: Started.")

        //initprogress dialog

        mConnectThread = ConnectThread(device, uuidInsecure, bluetoothAdapter, mDeviceStateListener){
            connected(it)
        }
        mConnectThread?.start()
    }


    private fun connected(socket: BluetoothSocket) {
        Timber.d("connected: Starting.")

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = ConnectedThread(socket, mDeviceStateListener)
        mConnectedThread?.start()
    }

    fun setBTConnectionListener(listner: DeviceStateListener){
        mDeviceStateListener = listner
    }

    var mDeviceStateListener: DeviceStateListener? = null

    fun cancel(){
        mInsecureAcceptThread?.cancel()
        mConnectThread?.cancel()
        mConnectedThread?.cancel()
    }
}