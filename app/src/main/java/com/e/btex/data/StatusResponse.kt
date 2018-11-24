package com.e.btex.data

import com.e.btex.utils.extensions.toInt
import com.e.btex.utils.extensions.toPositiveShort
import com.e.btex.utils.extensions.toShort

data class StatusResponse(val byteArray: ByteArray,

                          val signature: Short = byteArray.sliceArray(0..1).toShort(),
                          val commandCode: Short = byteArray[2].toPositiveShort(),
                          val status: Short = byteArray[3].toPositiveShort(),
                          val battery: Short = byteArray[4].toPositiveShort(),
                          val lastLoggedRecord: Int = byteArray.sliceArray(5..8).toInt(),
                          val temperature: Short = byteArray.sliceArray(9..10).toShort(),
                          val humidity: Short = byteArray.sliceArray(11..12).toShort(),
                          val co2: Short = byteArray.sliceArray(13..14).toShort(),
                          val pm1: Short = byteArray.sliceArray(15..16).toShort(),
                          val pm25: Short = byteArray.sliceArray(17..18).toShort(),
                          val pm10: Short = byteArray.sliceArray(19..20).toShort(),
                          val tvoc: Short = byteArray.sliceArray(21..22).toShort()
)
