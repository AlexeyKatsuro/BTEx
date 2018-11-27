package com.e.btex.connection

import android.bluetooth.BluetoothSocket
import android.os.Handler
import com.e.btex.ui.common.BtConnectionListener
import timber.log.Timber
import java.io.IOException

/**
 * Finally the ConnectedThread which is responsible for maintaining the BTConnection, Sending the data, and
 * receiving incoming data through input/output streams respectively.
 */
class ConnectedThread(private val socket: BluetoothSocket,
                      private val handler: Handler,
                      private val listner: BtConnectionListener?) : Thread() {

    private var isRunnig = false
    private val inputStream = socket.inputStream
    private var outStream = socket.outputStream

    override fun run() {

        Timber.d("ConnectedThread: Starting.")
        isRunnig = true
        handler.post {
            listner?.onCreateConnection()
        }

        val buffer = ByteArray(1024)  // buffer store for the stream

        var bytes: Int // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs

        var byteReceived = 0
        val byteList = mutableListOf<Byte>()
        var isData = false

        while (isRunnig) {
            // Read from the InputStream
            try {
                bytes = inputStream!!.read(buffer)
                Timber.d("Read from the InputStream: $bytes Bytes")


                if(buffer.sliceArray(0 until 2).contentEquals(byteArrayOf(222.toByte(),175.toByte()))){
                    Timber.d("Read Status signature")
                    isData = true
                    byteReceived = 0
                    byteList.clear()
                }

                if (isData){
                    Timber.d("Read Status Data: ${bytes} Bytes")
                    byteList.addAll(buffer.slice(0 until bytes))
                     byteReceived+=bytes

                    if (byteReceived == 23){
                        Timber.d("All status data was received: ${byteList.size} Bytes")
                        handler.post {
                            listner?.onReceiveData(byteList.toByteArray(),byteReceived)
                        }
                        isData = false
                    }
                }
//                Thread.sleep(5000)
//                handler.post {
//                    listner?.onReceiveData(byteList.toByteArray(), byteReceived)
//                }


            } catch (e: IOException) {
                Timber.e(e, "write: Error reading Input Stream.  + ${e.message}")
                cancel()
                handler.post {
                    listner?.onDestroyConnection()
                }
                break
            }

        }
    }


    /* Call this from the main activity to shutdown the connection */
    fun cancel() {
        isRunnig = false
        try {
            socket.close()
        } catch (e: IOException) {
        }

    }
}