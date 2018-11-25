package com.e.btex.data

enum class Signatures(val bytes: ByteArray) {

    Status(byteArrayOf(222.toByte(),175.toByte()))
}