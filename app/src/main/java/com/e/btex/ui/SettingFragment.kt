package com.e.btex.ui

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.*
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.ACTION_BOND_STATE_CHANGED
import android.bluetooth.BluetoothDevice.ACTION_FOUND
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.e.btex.R
import com.e.btex.broadcastReceivers.*
import com.e.btex.connection.BTService
import com.e.btex.data.dto.Sensors
import com.e.btex.data.prefs.PreferenceStorage
import com.e.btex.databinding.FragmentSettingBinding
import com.e.btex.ui.common.DeviceStateListener
import com.e.btex.utils.AutoSubscribeReceiver
import com.e.btex.utils.extensions.setLockedScreen
import com.e.btex.utils.extensions.showInfoInLog
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import timber.log.Timber
import java.util.*
import kotlin.properties.Delegates


class SettingFragment : Fragment() {


    companion object {
        private const val REQUEST_BT = 11
    }

    private lateinit var binding: FragmentSettingBinding
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var deviceAdapter: DeviceAdapter
    private lateinit var pairedDeviceAdapter: DeviceAdapter
    private val deviceList: MutableList<BluetoothDevice> = mutableListOf()

    private var service: BTService? = null
    private var isConnected = false

    private var isBluetoothExist: Boolean = false

    private var isBTEnabled by Delegates.observable(false) { _, old, new ->
        binding.appBar.btSwitch.isChecked = new
    }

    private lateinit var prefStorage: PreferenceStorage
    private var targetDevice: BluetoothDevice? = null

    var isAutoTurn = false

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

    private var deviceStateReceiver by AutoSubscribeReceiver<DeviceStateReceiver>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        prefStorage = PreferenceStorage.getInstance(requireContext())
        targetDevice = getBluetoothDeviceByAddress(prefStorage.deviceAddress)

        isBluetoothExist = getDefaultAdapter()?.let {
            bluetoothAdapter = it
            true
        } ?: false

        onDeviceDiscoveredReceiver = BluetoothDeviceReceiver()
        onStateChangedReceiver = BluetoothStateReceiver()
        onScanModeChangedReceiver = BluetoothScanModeReceiver()
        onBondStateReceiver = BluetoothBondStateReceiver()
        deviceStateReceiver = DeviceStateReceiver()

        if(isBtStateValid)
            initBlueToothService()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentSettingBinding.inflate(inflater, container, false).apply {
            targetDevice = this@SettingFragment.targetDevice
        }

        isBTEnabled = bluetoothAdapter.isEnabled

        binding.appBar.btSwitch.setOnCheckedChangeListener{ _ , _ ->
            if(!isAutoTurn) {
                enableDisableBT()
            } else{
                isAutoTurn = false
            }
        }

        binding.scanning.buttonDiscovery.setOnClickListener {
            dicoverDevice()
        }

        binding.buttonUpdatePaired.setOnClickListener {
            updatePairedDevices()
            binding.pairedDeviceRecyclerView.apply {
                isGone  = !isGone
            }
        }


        deviceAdapter = DeviceAdapter {
            if(isBtStateValid) {
                requireActivity().toast(it?.name ?: "")
                bluetoothAdapter.cancelDiscovery()
                it.createBond()
            }
        }


        pairedDeviceAdapter = DeviceAdapter {
            if(isBtStateValid) {
               // bluetoothConnection?.startClient(it)
                prefStorage.deviceAddress = it.address
                service?.startClient(it)
            }
        }

        binding.targetDeviceContainer.setOnClickListener {
            if(isBtStateValid) {
                targetDevice?.let {
                    service?.startClient(it)
                }
            }
        }

        binding.deviceRecyclerView.adapter = deviceAdapter
        binding.pairedDeviceRecyclerView.adapter = pairedDeviceAdapter

        updatePairedDevices()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appBar.toolBar.apply {
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }

        deviceStateReceiver.setBtConnectionListener(object : DeviceStateListener {
            override fun onStartConnecting() {
                setLockedScreen(true)
                binding.isConnecting = true
                binding.executePendingBindings()
            }

            override fun onFailedConnecting() {
                setLockedScreen(false)
                binding.isConnecting = false
                binding.executePendingBindings()
                Toast.makeText(requireContext(), "Connection failed", Toast.LENGTH_SHORT).show()
            }

            override fun onCreateConnection() {
                setLockedScreen(false)
                binding.isConnecting = false
                binding.executePendingBindings()
                Toast.makeText(requireContext(), "Connected", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()

            }

            override fun onDestroyConnection() {
                Toast.makeText(requireContext(), "Disconnected", Toast.LENGTH_SHORT).show()
            }

            override fun onReceiveData(bytes: ByteArray, size: Int) {
//                val statusResponse = StatusResponse(bytes)
//                Timber.d("Status response: $statusResponse")
//
//                val sensors = Sensors(
//                        temperature = statusResponse.temperature,
//                        humidity = statusResponse.humidity,
//                        co2 = statusResponse.co2,
//                        pm1 = statusResponse.pm1,
//                        pm25 = statusResponse.pm25,
//                        pm10 = statusResponse.pm10,
//                        tvoc = statusResponse.tvoc)
                val sensors = Sensors(
                        Random().nextInt(35).toFloat(),
                        Random().nextInt(1000).toFloat(),
                        Random().nextInt(325).toFloat(),
                        Random().nextInt(100).toFloat(),
                        Random().nextInt(10).toFloat(),
                        Random().nextInt(50).toFloat(),
                        Random().nextInt(5).toFloat())
                Timber.d("Sensors data: $sensors")
            }
        })

        onStateChangedReceiver.setOnStateChangedListener(object : BluetoothStateReceiver.OnStateChangedListener {
            override fun onStateOff() {
                Timber.d("onStateOff")
                binding.appBar.btSwitch.isEnabled = true
                isAutoTurn = true
                isBTEnabled = false
            }

            override fun onStateOn() {
                Timber.d("onStateOn")
                binding.appBar.btSwitch.isEnabled = true
                isAutoTurn = true
                isBTEnabled = true

                if(service == null)
                    initBlueToothService()
            }

            override fun onStateTurningOff() {
                Timber.d("onStateTurningOff")
                binding.appBar.btSwitch.isEnabled = false
            }

            override fun onStateTurningOn() {
                Timber.d("onStateTurningOn")
                binding.appBar.btSwitch.isEnabled = false
            }

        })
        onScanModeChangedReceiver.setOnVisibilityChangedListener(object : BluetoothScanModeReceiver.OnScanModeChangedListener {
            override fun onScanModeConnectableDiscoverable() {
                Timber.d("onScanModeConnectableDiscoverable")
            }

            override fun onScanModeConnectable() {
                Timber.d("onScanModeConnectable")
            }

            override fun onScanModeNone() {
                Timber.d("onScanModeNone")
            }

            override fun onStateConnecting() {
                Timber.d("onStateConnecting")
            }

            override fun onStateConnected() {
                Timber.d("onStateConnected")
            }

        })
        onDeviceDiscoveredReceiver.setOnDeviceReceivedListener(object : BluetoothDeviceReceiver.OnDeviceReceivedListener {
            override fun onStartDiscovery() {
                Timber.d("onStartDiscovery")
                binding.isScaning = true
            }

            override fun onStopDiscovery() {
                Timber.d("onStopDiscovery")
                binding.isScaning = false
            }

            override fun onDeviceReceived(device: BluetoothDevice) {
                Timber.d("onDeviceReceived:")
                device.showInfoInLog()
                deviceList.add(device)
                deviceAdapter.submitList(deviceList.toList())
            }

        })
        onBondStateReceiver.setOnBondStateListener(object : BluetoothBondStateReceiver.OnBondStateChangedListener {
            override fun onBondBonded(device: BluetoothDevice) {
                Timber.d("onBondBonded")
                updatePairedDevices()
            }

            override fun onBondBonding(device: BluetoothDevice) {
                Timber.d("onBondBonding")
            }

            override fun onBondNone(device: BluetoothDevice) {
                Timber.d("onBondNone")
            }

        })
    }

    private fun getBluetoothDeviceByAddress(address: String?): BluetoothDevice? {
        return  try {
            BluetoothAdapter.getDefaultAdapter()?.let {
             if(it.isEnabled){
                 it.getRemoteDevice(address)
             } else
                 null
            }
        }catch (e: Exception) {
            Timber.e(e)
            null
        }

    }


    fun initBlueToothService() {
        val intent = Intent(requireContext(), BTService::class.java)
        requireActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun updatePairedDevices() {
        if (!isBtStateValid) {
            return
        }

        pairedDeviceAdapter.submitList(bluetoothAdapter.bondedDevices.toList())
    }

    private fun enableDisableBT() {
        if (!bluetoothAdapter.isEnabled) {
            binding.appBar.btSwitch.isEnabled = false
            showBluetoothEnableDialog()
        } else {
            bluetoothAdapter.disable()
        }


    }

    private fun showBluetoothEnableDialog() {
        val enableBtIntent = Intent(ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_BT)

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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == REQUEST_BT){
            if (resultCode!=Activity.RESULT_OK) {
                isAutoTurn = true
                isBTEnabled = false
                binding.appBar.btSwitch.isEnabled = true
            }
        }
    }

    private val connection = object : ServiceConnection {

        override fun onServiceDisconnected(name: ComponentName?) {
            isConnected = false
        }

        override fun onServiceConnected(name: ComponentName?, iBinder: IBinder) {
            service = (iBinder as BTService.LocalBinder).service
            isConnected = true
        }

    }

    override fun onDetach() {
        super.onDetach()
        if(isConnected) {
            requireActivity().unbindService(connection)
            isConnected = false
        }
    }

}


