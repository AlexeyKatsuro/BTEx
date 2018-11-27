package com.e.btex.utils.extensions

import android.app.Activity
import android.content.res.Resources
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.e.btex.R
import com.e.btex.data.dto.Sensor
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


fun KClass<out Sensor>.getName(res: Resources)
    = when (this) {
    Sensor.Temperature::class -> res.getString(R.string.temperature)
    Sensor.Humidity::class -> res.getString(R.string.humidity)
    Sensor.Co2::class -> res.getString(R.string.co2)
    Sensor.Pm1::class -> res.getString(R.string.pm1)
    Sensor.Pm25::class -> res.getString(R.string.pm25)
    Sensor.Pm10::class -> res.getString(R.string.pm10)
    Sensor.Tvoc::class -> res.getString(R.string.tvoc)
    else ->{
        val exception = Exception("Invalid sensor class type : $this")
        Timber.e(exception)
        throw exception
    }
}


fun Sensor.getName(res: Resources)
        = when (this) {
        is Sensor.Temperature -> res.getString(R.string.temperature)
        is Sensor.Humidity -> res.getString(R.string.humidity)
        is Sensor.Co2 -> res.getString(R.string.co2)
        is Sensor.Pm1 -> res.getString(R.string.pm1)
        is Sensor.Pm25 -> res.getString(R.string.pm25)
        is Sensor.Pm10 -> res.getString(R.string.pm10)
        is Sensor.Tvoc -> res.getString(R.string.tvoc)
    }
fun Sensor.getString(res: Resources) = res.getString(R.string.sensor_val,getName(res),value)

