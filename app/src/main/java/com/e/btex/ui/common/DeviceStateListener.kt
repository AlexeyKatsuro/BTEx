package com.e.btex.ui.common

import android.bluetooth.BluetoothDevice

interface DeviceStateListener{

    fun onStartConnecting()
    fun onFailedConnecting()

    fun onCreateConnection(device: BluetoothDevice)
    fun onDestroyConnection()

    fun onReceiveData(bytes: ByteArray,size: Int)


}