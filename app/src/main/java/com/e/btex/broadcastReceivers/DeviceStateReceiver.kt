package com.e.btex.broadcastReceivers

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.e.btex.connection.BTService
import com.e.btex.ui.common.DeviceStateListener

class DeviceStateReceiver: SpecificBroadcastReceiver() {


    override fun getFilterActions() = IntentFilter().apply {
        addAction(BTService.ACTION_START_CONNECTING)
        addAction(BTService.ACTION_CREATE_CONNECTION)
        addAction(BTService.ACTION_FAILED_CONNECTING)
        addAction(BTService.ACTION_DESTROY_CONNECTION)
        addAction(BTService.ACTION_RECEIVE_DATA)
    }

    private var mListener: DeviceStateListener? = null

    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action){
            BTService.ACTION_START_CONNECTING -> mListener?.onStartConnecting()
            BTService.ACTION_CREATE_CONNECTION -> mListener?.onCreateConnection()
            BTService.ACTION_FAILED_CONNECTING -> mListener?.onFailedConnecting()
            BTService.ACTION_DESTROY_CONNECTION -> mListener?.onDestroyConnection()
            BTService.ACTION_RECEIVE_DATA -> {
                val size = intent.getIntExtra(BTService.EXTRA_DATA_SIZE,0)
                val data = intent.getByteArrayExtra(BTService.EXTRA_DATA)
                mListener?.onReceiveData(data,size)
            }
        }
    }

    fun setBtConnectionListener(listener: DeviceStateListener){
        this.mListener = listener
    }
}