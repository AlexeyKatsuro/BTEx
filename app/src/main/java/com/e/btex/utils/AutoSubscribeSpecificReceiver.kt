/*
 * Copyright 2018 LWO, LLC
 */

package com.e.btex.utils

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.e.btex.broadcastReceivers.SpecificBroadcastReceiver
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * A lazy property that gets cleaned up when the fragment is destroyed.
 *
 * Accessing this variable in a destroyed fragment will throw NPE.
 */
class AutoSubscribeSpecificReceiver<T : SpecificBroadcastReceiver>(val fragment: Fragment) : ReadWriteProperty<Fragment, T> {
    private var _value: T? = null

    init {
        fragment.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                unregisterReceiver()
            }
        })
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        return _value ?: throw IllegalStateException(
                "should never call auto-cleared-value get when it might not be available"
        )
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T) {
        unregisterReceiver()
        _value = value

        fragment.requireActivity().registerReceiver(_value, _value?.getFilterActions())
    }

    private fun unregisterReceiver() {
        _value?.let {
            fragment.requireActivity().unregisterReceiver(it)
        }
    }
}

/**
 * Creates an [AutoSubscribeReceiver] associated with this fragment.
 */
fun <T : SpecificBroadcastReceiver> Fragment.AutoSubscribeReceiver()
        = AutoSubscribeSpecificReceiver<T>(this)
