package com.e.btex.broadcastReceivers

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BluetoothBondStateReceiver:  BroadcastReceiver(){

    override fun onReceive(context: Context, intent: Intent) {
        if(intent.action == ACTION_BOND_STATE_CHANGED){
            val device: BluetoothDevice = intent.getParcelableExtra(EXTRA_DEVICE)

            when(device.bondState){
                BOND_BONDED -> onBondStateChangedListener?.onBondBonded(device)
                BOND_BONDING -> onBondStateChangedListener?.onBondBonding(device)
                BOND_NONE -> onBondStateChangedListener?.onBondNone(device)
                else -> Unit
            }

            //onBondStateChangedListener?.invoke(device)
        }
    }

    interface OnBondStateChangedListener{
        fun onBondBonded(device: BluetoothDevice)
        fun onBondBonding(device: BluetoothDevice)
        fun onBondNone(device: BluetoothDevice)

    }

    private var onBondStateChangedListener: OnBondStateChangedListener? = null

    fun setOnBondStateListener(listener: OnBondStateChangedListener){
        onBondStateChangedListener = listener
    }
}