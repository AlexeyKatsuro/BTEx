package com.e.btex.ui.common

interface BtConnectionListener{

    fun onStartConnecting()
    fun onFailedConnecting()

    fun onCreateConnection()
    fun onDestroyConnection()

    fun onReceiveData(bytes: ByteArray,size: Int)


}