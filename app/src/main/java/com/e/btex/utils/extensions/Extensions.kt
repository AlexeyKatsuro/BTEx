package com.e.btex.utils.extensions

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.*
import android.bluetooth.BluetoothDevice
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import timber.log.Timber
import java.util.*


val BluetoothAdapter.stateString: String
    get() = state.toBluetoothState

val Int.toBluetoothState: String
    get() {
        return return when (this) {
            STATE_OFF -> "STATE_OFF"
            STATE_ON -> "STATE_OFF"
            STATE_TURNING_ON -> "STATE_OFF"
            STATE_TURNING_OFF -> "STATE_TURNING_OFF"
            else -> "UNKNOWN_STATE"
        }
    }

val Int.toBluetoothScanMode: String
get() {
    return when (this) {
        //Device is in Discoverable Mode
        SCAN_MODE_CONNECTABLE_DISCOVERABLE ->"SCAN_MODE_CONNECTABLE_DISCOVERABLE: Discoverability Enabled."
        //Device not in discoverable mode
        SCAN_MODE_CONNECTABLE -> "SCAN_MODE_CONNECTABLE: Discoverability Disabled. Able to receive connections."
        SCAN_MODE_NONE -> "SCAN_MODE_NONE"
        STATE_CONNECTING -> "STATE_CONNECTING"
        STATE_CONNECTED -> "STATE_CONNECTING"
        else -> "UNKNOWN_SCAN_MODE"
    }
}

fun BluetoothDevice.showInfoInLog() {
    Timber.i("name = $name, address = $address")
}

fun IntRange.random() =
        Random().nextInt((endInclusive + 1) - start) +  start

val Fragment.actionBar
    get() = (requireActivity() as AppCompatActivity).supportActionBar!!