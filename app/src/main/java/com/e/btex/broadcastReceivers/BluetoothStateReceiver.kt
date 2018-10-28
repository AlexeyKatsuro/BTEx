package com.e.btex.broadcastReceivers

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.*
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import timber.log.Timber

class BluetoothStateReceiver: BroadcastReceiver(){

    override fun onReceive(context: Context, intent: Intent) {
        if(intent.action == ACTION_STATE_CHANGED){
            val state: Int = intent.getIntExtra(EXTRA_STATE, ERROR)
            when (state) {
                STATE_OFF -> onStateChangedListener?.onStateOff()
                STATE_ON -> onStateChangedListener?.onStateOn()
                STATE_TURNING_ON -> onStateChangedListener?.onStateTurningOn()
                STATE_TURNING_OFF -> onStateChangedListener?.onStateTurningOff()
                else -> Timber.e("Unknown State")
            }
        }
    }

    interface OnStateChangedListener{
        fun onStateOff()
        fun onStateOn()
        fun onStateTurningOff()
        fun onStateTurningOn()
    }

    private var onStateChangedListener: OnStateChangedListener? = null

    fun setOnStateChangedListener(listener: OnStateChangedListener){
        onStateChangedListener = listener
    }
}