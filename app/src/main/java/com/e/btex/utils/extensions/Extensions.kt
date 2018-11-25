package com.e.btex.utils.extensions

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.*
import android.bluetooth.BluetoothDevice
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import timber.log.Timber
import java.util.*

fun IntRange.random() =
        Random().nextInt((endInclusive + 1) - start) + start

val Fragment.actionBar
    get() = (requireActivity() as AppCompatActivity).supportActionBar!!

fun Activity.setLockedScreen(isLocked: Boolean) {
    if (isLocked)
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    else
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
}

fun Fragment.setLockedScreen(isLocked: Boolean) = requireActivity().setLockedScreen(isLocked)

inline fun <T : ViewDataBinding> T.executeAfter(block: T.() -> Unit) {
    block()
    executePendingBindings()
}

