package com.e.btex.broadcastReceivers

import android.bluetooth.BluetoothAdapter.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber

class BluetoothScanModeReceiver : BroadcastReceiver(){

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_SCAN_MODE_CHANGED) {
            val mode = intent.getIntExtra(EXTRA_SCAN_MODE, ERROR)
            when (mode) {
                //Device is in Discoverable Mode
                SCAN_MODE_CONNECTABLE_DISCOVERABLE -> onScanModeChangedListener?.onScanModeConnectableDiscoverable()
                //Device not in discoverable mode
                SCAN_MODE_CONNECTABLE -> onScanModeChangedListener?.onScanModeConnectable()
                SCAN_MODE_NONE -> onScanModeChangedListener?.onScanModeNone()
                STATE_CONNECTING -> onScanModeChangedListener?.onStateConnecting()
                STATE_CONNECTED -> onScanModeChangedListener?.onStateConnected()
                else -> Timber.e("Unknown Scan Mode")
            }
        }
    }

    interface OnScanModeChangedListener{
        fun onScanModeConnectableDiscoverable()
        fun onScanModeConnectable()
        fun onScanModeNone()
        fun onStateConnecting()
        fun onStateConnected()
    }

    private var onScanModeChangedListener: OnScanModeChangedListener? = null

    fun setOnVisibilityChangedListener(listener: OnScanModeChangedListener){
        onScanModeChangedListener = listener
    }
}