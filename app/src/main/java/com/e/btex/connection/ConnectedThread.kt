package com.e.btex.connection

import android.bluetooth.BluetoothSocket
import android.os.Handler
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset

/**
 * Finally the ConnectedThread which is responsible for maintaining the BTConnection, Sending the data, and
 * receiving incoming data through input/output streams respectively.
 */
class ConnectedThread(private val socket: BluetoothSocket,
                      private val handler: Handler,
                      private val inputCallback: ((ByteArray, Int) -> Unit)? ) : Thread() {

    private val inputStream = socket.inputStream
    private var outStream = socket.outputStream

    override fun run() {

        Timber.i("ConnectedThread: Starting.")
        val buffer = ByteArray(1024)  // buffer store for the stream

        var bytes: Int // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs
        while (true) {
            // Read from the InputStream
            try {
                bytes = inputStream!!.read(buffer)
                val incomingMessage = String(buffer, 0, bytes)
                Timber.i("InputStream: $incomingMessage")
                handler.post {
                    inputCallback?.invoke(buffer,bytes)
                }
            } catch (e: IOException) {
                Timber.e(e, "write: Error reading Input Stream.  + ${e.message}")
                break
            }

        }
    }

    //Call this from the main activity to send data to the remote device
    fun write(bytes: ByteArray) {
        val text = String(bytes, Charset.defaultCharset())
        Timber.i("write: Writing to outputstream: $text")
        try {
            outStream!!.write(bytes)
        } catch (e: IOException) {
            Timber.e(e, "write: Error writing to output stream. ${e.message}")
        }

    }

    /* Call this from the main activity to shutdown the connection */
    fun cancel() {
        try {
            socket.close()
        } catch (e: IOException) {
        }

    }
}