package com.e.btex.broadcastReceivers

import android.content.BroadcastReceiver
import android.content.IntentFilter

abstract class SpecificBroadcastReceiver: BroadcastReceiver() {
    abstract fun getFilterActions(): IntentFilter
}