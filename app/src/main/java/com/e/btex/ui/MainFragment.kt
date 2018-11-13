package com.e.btex.ui

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.*
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.ACTION_BOND_STATE_CHANGED
import android.bluetooth.BluetoothDevice.ACTION_FOUND
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.e.btex.R
import com.e.btex.broadcastReceivers.BluetoothBondStateReceiver
import com.e.btex.broadcastReceivers.BluetoothDeviceReceiver
import com.e.btex.broadcastReceivers.BluetoothScanModeReceiver
import com.e.btex.broadcastReceivers.BluetoothStateReceiver
import com.e.btex.databinding.FragmentMainBinding
import com.e.btex.utils.AutoSubscribeReceiver
import com.e.btex.utils.extensions.showInfoInLog
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import timber.log.Timber
import com.e.btex.connection.BluetoothConnectionService
import com.e.btex.data.StatusResponse
import java.nio.charset.Charset


class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var deviceAdapter: DeviceAdapter
    private lateinit var pairedDeviceAdapter: DeviceAdapter
    private val deviceList: MutableList<BluetoothDevice> = mutableListOf()

    private var bluetoothConnection: BluetoothConnectionService? = null

    private var isBluetoothExist: Boolean = false

    val isBtStateValid: Boolean
        get() {
            if (!isBluetoothExist) {
                requireActivity().longToast(R.string.bt_not_capabilities)
                return false
            }
            if (!bluetoothAdapter.isEnabled) {
                requireActivity().toast(R.string.bt_not_active)
                return false
            }

            return true
        }

    private var onStateChangedReceiver
            by AutoSubscribeReceiver<BluetoothStateReceiver>(ACTION_STATE_CHANGED)
    private var onScanModeChangedReceiver
            by AutoSubscribeReceiver<BluetoothScanModeReceiver>(ACTION_SCAN_MODE_CHANGED)
    private var onBondStateReceiver
            by AutoSubscribeReceiver<BluetoothBondStateReceiver>(ACTION_BOND_STATE_CHANGED)
    private var onDeviceDiscoveredReceiver
            by AutoSubscribeReceiver<BluetoothDeviceReceiver>(
                    ACTION_FOUND,
                    ACTION_DISCOVERY_STARTED,
                    ACTION_DISCOVERY_FINISHED)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isBluetoothExist = getDefaultAdapter()?.let {
            bluetoothAdapter = it
            true
        } ?: false

        onDeviceDiscoveredReceiver = BluetoothDeviceReceiver()
        onStateChangedReceiver = BluetoothStateReceiver()
        onScanModeChangedReceiver = BluetoothScanModeReceiver()
        onBondStateReceiver = BluetoothBondStateReceiver()


        if(isBtStateValid)
            ininBlueToothService()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)

        binding.buttonOnOff.setOnClickListener {
            enableDisableBT()
        }

        binding.buttonVisibility.setOnClickListener {
            showBluetoothVisibleDialog()
        }

        binding.scanning.buttonDiscovery.setOnClickListener {
            dicoverDevice()
        }

        binding.buttonUpdatePaired.setOnClickListener {
            updatePairedDevices()
        }

        binding.buttonSend.setOnClickListener {
            val bytes =  binding.messageText.getText().toString().toByteArray(Charset.defaultCharset())
            bluetoothConnection?.write(bytes)
            binding.messageText.setText("")
        }

        deviceAdapter = DeviceAdapter {
            requireActivity().toast(it?.name?:"")
            bluetoothAdapter.cancelDiscovery()
            it.createBond()
        }


        pairedDeviceAdapter = DeviceAdapter {
            //requireActivity().toast(it.name)

            bluetoothConnection?.startClient(it)
        }

        binding.deviceRecyclerView.adapter = deviceAdapter
        binding.pairedDeviceRecyclerView.adapter = pairedDeviceAdapter

        return binding.root
    }

    private lateinit var Bdevice: BluetoothDevice

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onStateChangedReceiver.setOnStateChangedListener(object : BluetoothStateReceiver.OnStateChangedListener {
            override fun onStateOff() {
                Timber.i("onStateOff")
            }

            override fun onStateOn() {
                Timber.i("onStateOn")
                if(bluetoothConnection == null)
                    ininBlueToothService()
            }

            override fun onStateTurningOff() {
                Timber.i("onStateTurningOff")
            }

            override fun onStateTurningOn() {
                Timber.i("onStateTurningOn")
            }

        })
        onScanModeChangedReceiver.setOnVisibilityChangedListener(object : BluetoothScanModeReceiver.OnScanModeChangedListener {
            override fun onScanModeConnectableDiscoverable() {
                Timber.i("onScanModeConnectableDiscoverable")
            }

            override fun onScanModeConnectable() {
                Timber.i("onScanModeConnectable")
            }

            override fun onScanModeNone() {
                Timber.i("onScanModeNone")
            }

            override fun onStateConnecting() {
                Timber.i("onStateConnecting")
            }

            override fun onStateConnected() {
                Timber.i("onStateConnected")
            }

        })
        onDeviceDiscoveredReceiver.setOnDeviceReceivedListener(object : BluetoothDeviceReceiver.OnDeviceReceivedListener {
            override fun onStartDiscovery() {
                Timber.i("onStartDiscovery")
                binding.isScaning = true
            }

            override fun onStopDiscovery() {
                Timber.i("onStopDiscovery")
                binding.isScaning = false
            }

            override fun onDeviceReceived(device: BluetoothDevice) {
                Timber.i("onDeviceReceived:")
                device.showInfoInLog()
                deviceList.add(device)
                deviceAdapter.submitList(deviceList.toList())
            }

        })
        onBondStateReceiver.setOnBondStateListener(object : BluetoothBondStateReceiver.OnBondStateChangedListener {
            override fun onBondBonded(device: BluetoothDevice) {
                Timber.i("onBondBonded")
            }

            override fun onBondBonding(device: BluetoothDevice) {
                Timber.i("onBondBonding")
            }

            override fun onBondNone(device: BluetoothDevice) {
                Timber.i("onBondNone")
            }

        })

    }

    fun ininBlueToothService() {
        bluetoothConnection = BluetoothConnectionService(requireContext(), bluetoothAdapter, Handler()).apply {
            setOnDataRecieveListener { buffer, bytes ->

                val statusResponse = StatusResponse(buffer)
                Timber.i("Status response: $statusResponse")

            }
            start()
        }
    }

    private fun updatePairedDevices() {
        pairedDeviceAdapter.submitList(bluetoothAdapter.bondedDevices.toList())
    }

    private fun enableDisableBT() {
        if (!bluetoothAdapter.isEnabled) {
            showBluetoothEnableDialog()
        } else {
            bluetoothAdapter.disable()
        }


    }

    private fun showBluetoothEnableDialog() {
        val enableBtIntent = Intent(ACTION_REQUEST_ENABLE)
        startActivity(enableBtIntent)

    }

    private fun showBluetoothVisibleDialog() {
        val visibilityBtIntent = Intent(ACTION_REQUEST_DISCOVERABLE)
        visibilityBtIntent.putExtra(EXTRA_DISCOVERABLE_DURATION, 120)
        startActivity(visibilityBtIntent)


    }

    private fun dicoverDevice() {
        if (!isBtStateValid) {
            return
        }

        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }
        deviceList.clear()
        deviceAdapter.clear()
        bluetoothAdapter.startDiscovery()
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothAdapter.cancelDiscovery()
        bluetoothAdapter.disable()
    }

}


