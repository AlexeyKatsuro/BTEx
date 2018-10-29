/*
 * Copyright 2018 LWO, LLC
 */

package com.e.btex.utils

import android.content.BroadcastReceiver
import android.content.IntentFilter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import timber.log.Timber
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * A lazy property that gets cleaned up when the fragment is destroyed.
 *
 * Accessing this variable in a destroyed fragment will throw NPE.
 */
class AutoRegisterReceiver<T : BroadcastReceiver>(val fragment: Fragment, val filterAction: String) : ReadWriteProperty<Fragment, T> {
    private var _value: T? = null

    init {
        fragment.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                _value?.let {
                    fragment.requireActivity().unregisterReceiver(it)
                }
            }
        })
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        return _value ?: throw IllegalStateException(
                "should never call auto-cleared-value get when it might not be available"
        )
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T) {
        _value = value
        val intentFilter = IntentFilter(filterAction)
        fragment.requireActivity().registerReceiver(_value, intentFilter)
    }
}

/**
 * Creates an [AutoSubscribeReceiver] associated with this fragment.
 */
fun <T : BroadcastReceiver> Fragment.AutoSubscribeReceiver(filterAction: String) = AutoRegisterReceiver<T>(this,filterAction)
