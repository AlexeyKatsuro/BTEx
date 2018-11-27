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
import android.os.Handler
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.e.btex.R
import com.e.btex.broadcastReceivers.BluetoothBondStateReceiver
import com.e.btex.broadcastReceivers.BluetoothDeviceReceiver
import com.e.btex.broadcastReceivers.BluetoothScanModeReceiver
import com.e.btex.broadcastReceivers.BluetoothStateReceiver
import com.e.btex.connection.BluetoothConnectionService
import com.e.btex.connection.MyService
import com.e.btex.data.StatusResponse
import com.e.btex.databinding.FragmentSettingBinding
import com.e.btex.ui.common.BtConnectionListener
import com.e.btex.utils.AutoSubscribeReceiver
import com.e.btex.utils.extensions.setLockedScreen
import com.e.btex.utils.extensions.showInfoInLog
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import timber.log.Timber
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

    //private var bluetoothConnection: BluetoothConnectionService? = null
    private var service: MyService? = null

    private var isBluetoothExist: Boolean = false

    private var isBTEnabled by Delegates.observable(false) { _, old, new ->
        binding.appBar.btSwitch.isChecked = new
    }

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        isBluetoothExist = getDefaultAdapter()?.let {
            bluetoothAdapter = it
            true
        } ?: false

        onDeviceDiscoveredReceiver = BluetoothDeviceReceiver()
        onStateChangedReceiver = BluetoothStateReceiver()
        onScanModeChangedReceiver = BluetoothScanModeReceiver()
        onBondStateReceiver = BluetoothBondStateReceiver()


        if(isBtStateValid)
            initBlueToothService()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSettingBinding.inflate(inflater, container, false)

        isBTEnabled = bluetoothAdapter.isEnabled

        binding.appBar.btSwitch.setOnCheckedChangeListener{ _ , _ ->
            if(!isAutoTurn) {
                enableDisableBT()
            } else{
                isAutoTurn = false
            }
        }

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
                service?.startClient(it)
            }
        }

        binding.deviceRecyclerView.adapter = deviceAdapter
        binding.pairedDeviceRecyclerView.adapter = pairedDeviceAdapter

        updatePairedDevices()

        return binding.root
    }

    private lateinit var Bdevice: BluetoothDevice

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appBar.toolBar.apply {
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }

        onStateChangedReceiver.setOnStateChangedListener(object : BluetoothStateReceiver.OnStateChangedListener {
            override fun onStateOff() {
                Timber.i("onStateOff")
                binding.appBar.btSwitch.isEnabled = true
                isAutoTurn = true
                isBTEnabled = false
            }

            override fun onStateOn() {
                Timber.i("onStateOn")
                binding.appBar.btSwitch.isEnabled = true
                isAutoTurn = true
                isBTEnabled = true

                if(service == null)
                    initBlueToothService()
            }

            override fun onStateTurningOff() {
                Timber.i("onStateTurningOff")
                binding.appBar.btSwitch.isEnabled = false
            }

            override fun onStateTurningOn() {
                Timber.i("onStateTurningOn")
                binding.appBar.btSwitch.isEnabled = false
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


    fun initBlueToothService() {

        val intent = Intent(requireContext(), MyService::class.java)
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
        }

        override fun onServiceConnected(name: ComponentName?, iBinder: IBinder) {

            service = (iBinder as MyService.LocalBinder).service
            service?.bluetoothConnectionService = BluetoothConnectionService(requireContext(), bluetoothAdapter, Handler()).apply {

                setBTConnectionListener(object : BtConnectionListener{
                    override fun onStartConnecting() {
                        setLockedScreen(true)
                        binding.isConnecting = true
                        binding.executePendingBindings()
                    }

                    override fun onFailedConnecting() {
                        setLockedScreen(false)
                        binding.isConnecting = false
                        binding.executePendingBindings()
                        Toast.makeText(requireContext(),"Connection failed",Toast.LENGTH_SHORT).show()                }

                    override fun onCreateConnection() {
                        setLockedScreen(false)
                        binding.isConnecting = false
                        binding.executePendingBindings()
                        Toast.makeText(requireContext(),"Connected",Toast.LENGTH_SHORT).show()

                    }

                    override fun onDestroyConnection() {
                        Toast.makeText(requireContext(),"Disconnected",Toast.LENGTH_SHORT).show()
                    }

                    override fun onReceiveData(bytes: ByteArray, size: Int) {
                        val statusResponse = StatusResponse(bytes)
                        Timber.i("Status response: $statusResponse")
                    }

                })
                //start()
            }

        }

    }

}


