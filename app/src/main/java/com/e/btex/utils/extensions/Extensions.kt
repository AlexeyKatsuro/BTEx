package com.e.btex.utils.extensions

import android.app.Activity
import android.content.res.Resources
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.e.btex.R
import com.e.btex.data.SensorsType
import timber.log.Timber
import java.util.*
import kotlin.reflect.KClass

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


fun SensorsType.getName(res: Resources)
    = when (this) {
    SensorsType.temperature -> res.getString(R.string.temperature)
    SensorsType.humidity -> res.getString(R.string.humidity)
    SensorsType.co2-> res.getString(R.string.co2)
    SensorsType.pm1-> res.getString(R.string.pm1)
    SensorsType.pm25 -> res.getString(R.string.pm25)
    SensorsType.pm10 -> res.getString(R.string.pm10)
    SensorsType.tvoc -> res.getString(R.string.tvoc)
}

