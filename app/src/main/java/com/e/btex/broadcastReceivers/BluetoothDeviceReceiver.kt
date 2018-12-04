package com.e.btex.broadcastReceivers

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BluetoothDeviceReceiver: BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent) {

        if(intent.action == BluetoothDevice.ACTION_FOUND){
            val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            onDeviceReceivedListener?.onDeviceReceived(device)
        }

        if (intent.action == BluetoothAdapter.ACTION_DISCOVERY_STARTED){
            onDeviceReceivedListener?.onStartDiscovery()
        }

        if(intent.action == BluetoothAdapter.ACTION_DISCOVERY_FINISHED){
            onDeviceReceivedListener?.onStopDiscovery()
        }
    }

    interface OnDeviceReceivedListener {
        fun onDeviceReceived(device: BluetoothDevice)
        fun onStartDiscovery()
        fun onStopDiscovery()
    }

    private var onDeviceReceivedListener: OnDeviceReceivedListener? = null

    fun setOnDeviceReceivedListener(callBack: OnDeviceReceivedListener){
        onDeviceReceivedListener= callBack
    }

}