package com.e.btex.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class PreferenceStorage private constructor(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)


    var deviceAddress by StringPreference(prefs, PREFS_DEVICE_ADDRESS, null)

    companion object {

        @Volatile
        private var INSTANSE: PreferenceStorage? = null

        fun getInstance(context: Context): PreferenceStorage {
            return INSTANSE ?: synchronized(this) {
                INSTANSE ?: createStorage(context).also { INSTANSE = it }
            }

        }

        private fun createStorage(context: Context) = PreferenceStorage(context)


        private const val PREFS_NAME = "BTEx"
        private const val PREFS_DEVICE_ADDRESS = "mac_address"
    }


    class StringPreference(
            private val preferences: SharedPreferences,
            private val name: String,
            private val defaultValue: String?
    ) : ReadWriteProperty<Any, String?> {

        override fun getValue(thisRef: Any, property: KProperty<*>): String? {
            return preferences.getString(name, defaultValue)
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: String?) {
            preferences.edit { putString(name, value) }
        }
    }

}