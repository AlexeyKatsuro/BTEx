package com.e.btex.connection

import android.bluetooth.BluetoothAdapter
import android.content.Context
import java.util.*
import com.e.btex.R
import android.bluetooth.BluetoothSocket
import android.bluetooth.BluetoothServerSocket
import timber.log.Timber
import java.io.IOException
import android.bluetooth.BluetoothDevice
import android.app.ProgressDialog
import android.os.Handler
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset






class BluetoothConnectionService(private val context: Context,private val bluetoothAdapter: BluetoothAdapter, private val handler: Handler) {

    private val appName = context.getString(R.string.app_name)
    private val uuidInsecure = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")

    private var mInsecureAcceptThread: AcceptThread? = null

    private var mConnectThread: ConnectThread? = null
    private var mmDevice: BluetoothDevice? = null
    private var deviceUUID: UUID? = null
    var mProgressDialog: ProgressDialog? = null
    private var mConnectedThread: ConnectedThread? = null

    private var inputCallback: ((ByteArray, Int) -> Unit)? = null

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private inner class AcceptThread : Thread() {

        // The local server socket
        private val mmServerSocket: BluetoothServerSocket?

        init {
            var tmp: BluetoothServerSocket? = null

            // Create a new listening server socket
            try {
                tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, uuidInsecure)

                Timber.i("AcceptThread: Setting up Server using: $uuidInsecure")
            } catch (e: IOException) {
                Timber.e(e,"AcceptThread: IOException: ${e.message}")
            }

            mmServerSocket = tmp
        }



        override fun run() {
            Timber.i("run: AcceptThread Running.")

            var socket: BluetoothSocket? = null

            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                Timber.i("run: RFCOM server socket start.....")

                socket = mmServerSocket!!.accept()

                Timber.i("run: RFCOM server socket accepted connection.")

            } catch (e: IOException) {
                Timber.e(e, ("AcceptThread: IOException: ${e.message}"))
            }

            //talk about this is in the 3rd
            if (socket != null) {
                connected(socket)
            }

            Timber.i("END mAcceptThread ")
        }

        fun cancel() {
            Timber.i("cancel: Canceling AcceptThread.")
            try {
                mmServerSocket!!.close()
            } catch (e: IOException) {
                Timber.e(e, "cancel: Close of AcceptThread ServerSocket failed.  + ${e.message}")
            }

        }

    }

    private inner class ConnectThread(device: BluetoothDevice, uuid: UUID) : Thread() {
        private var mmSocket: BluetoothSocket? = null

        init {
            Timber.i("ConnectThread: started.")
            mmDevice = device
            deviceUUID = uuid
        }

        override fun run() {
            var tmp: BluetoothSocket? = null
            Timber.i("RUN mConnectThread ")

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                Timber.i("ConnectThread: Trying to create InsecureRfcommSocket using UUID: $uuidInsecure")
                tmp = mmDevice!!.createRfcommSocketToServiceRecord(deviceUUID)
            } catch (e: IOException) {
                Timber.e(e, "ConnectThread: Could not create InsecureRfcommSocket ${e.message}")
            }

            mmSocket = tmp

            // Always cancel discovery because it will slow down a connection
            bluetoothAdapter.cancelDiscovery()

            // Make a connection to the BluetoothSocket

            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket!!.connect()

                Timber.i("run: ConnectThread connected.")
            } catch (e: IOException) {
                // Close the socket
                try {
                    mmSocket!!.close()
                    Timber.i("run: Closed Socket.")
                } catch (e1: IOException) {
                    Timber.e(e, "mConnectThread: run: Unable to close connection in socket ${e1.message}")
                }

                Timber.i("run: ConnectThread: Could not connect to UUID: $uuidInsecure")
            }


            connected(mmSocket)
        }

        fun cancel() {
            try {
                Timber.i("cancel: Closing Client Socket.")
                mmSocket!!.close()
            } catch (e: IOException) {
                Timber.e(e, "cancel: close() of mmSocket in Connectthread failed. ${e.message}")
            }

        }
    }

    fun setOnDataRecieveListener(callback: (ByteArray, Int) -> Unit){
        inputCallback = callback
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    @Synchronized
    fun start() {
        Timber.i("start")

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = AcceptThread()
            mInsecureAcceptThread!!.start()
        }
    }

    /**
     * AcceptThread starts and sits waiting for a connection.
     * Then ConnectThread starts and attempts to make a connection with the other devices AcceptThread.
     */

    fun startClient(device: BluetoothDevice, uuid: UUID) {
        Timber.i("startClient: Started.")

        //initprogress dialog
        mProgressDialog = ProgressDialog.show(context, "Connecting Bluetooth", "Please Wait...", true)

        mConnectThread = ConnectThread(device, uuid)
        mConnectThread!!.start()
    }


    private fun connected(mmSocket: BluetoothSocket?) {
        Timber.i("connected: Starting.")

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = ConnectedThread(mmSocket)
        mConnectedThread!!.start()
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread.write
     */
    fun write(out: ByteArray) {
        // Synchronize a copy of the ConnectedThread
        Timber.i("write: Write Called.")
        //perform the write
        mConnectedThread!!.write(out)
    }



    /**
     * Finally the ConnectedThread which is responsible for maintaining the BTConnection, Sending the data, and
     * receiving incoming data through input/output streams respectively.
     */
    private inner class ConnectedThread(private val mmSocket: BluetoothSocket?) : Thread() {
        private var mmInStream: InputStream?
        private var mmOutStream: OutputStream?

        init {
            Timber.i("ConnectedThread: Starting.")
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            //dismiss the progressdialog when connection is established
            try {
                mProgressDialog!!.dismiss()
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }


            try {
                tmpIn = mmSocket!!.inputStream
                tmpOut = mmSocket!!.outputStream
            } catch (e: IOException) {
                e.printStackTrace()
            }

            mmInStream = tmpIn
            mmOutStream = tmpOut
        }

        override fun run() {
            val buffer = ByteArray(1024)  // buffer store for the stream

            var bytes: Int // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                // Read from the InputStream
                try {
                    bytes = mmInStream!!.read(buffer)
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
                mmOutStream!!.write(bytes)
            } catch (e: IOException) {
                Timber.e(e, "write: Error writing to output stream. ${e.message}")
            }

        }

        /* Call this from the main activity to shutdown the connection */
        fun cancel() {
            try {
                mmSocket!!.close()
            } catch (e: IOException) {
            }

        }
    }

}