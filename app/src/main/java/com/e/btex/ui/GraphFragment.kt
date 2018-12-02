package com.e.btex.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.DashPathEffect
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.androidplot.util.PixelUtils
import com.androidplot.xy.*
import com.e.btex.R
import com.e.btex.broadcastReceivers.DeviceStateReceiver
import com.e.btex.connection.BTService
import com.e.btex.data.SensorsType
import com.e.btex.data.dto.Sensors
import com.e.btex.databinding.FragmentGraphBinding
import com.e.btex.ui.common.DeviceStateListener
import com.e.btex.utils.AutoSubscribeReceiver
import com.e.btex.utils.extensions.executeAfter
import com.e.btex.utils.extensions.getName
import com.e.btex.utils.plot.DynamicSeries
import com.e.btex.utils.plot.SensorDataSource
import timber.log.Timber
import java.text.DecimalFormat
import java.util.*


class GraphFragment : Fragment() {


    private lateinit var binding: FragmentGraphBinding


    private lateinit var plot: XYPlot


    private var deviceStateReceiver by AutoSubscribeReceiver<DeviceStateReceiver>()

    var isConnected = false

    private var mService: BTService? = null

    private lateinit var dataSource: SensorDataSource
    private lateinit var series: DynamicSeries


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBlueToothService()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {


        binding = FragmentGraphBinding.inflate(inflater, container, false)
        binding.appBar.toolBar.inflateMenu(R.menu.main_menu)

        deviceStateReceiver = DeviceStateReceiver()




        plot = binding.plot

        plot.graph.getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).format = DecimalFormat("0")
        plot.graph.getLineLabelStyle(XYGraphWidget.Edge.LEFT).format = DecimalFormat("###.##")

        dataSource = SensorDataSource(requireContext())
        series = DynamicSeries(dataSource, 0)

        val formatter1 = LineAndPointFormatter(requireActivity(), R.xml.line_point_formatter_with_labels)

        plot.addSeries(series, formatter1)

        // thin out domain tick labels so they dont overlap each other:
        plot.domainStepMode = StepMode.INCREMENT_BY_FIT
        plot.domainStepValue = 1000.0

        plot.rangeStepMode = StepMode.INCREMENT_BY_FIT
        //plot.rangeStepValue = 1.0
//
        plot.setRangeBoundaries(0, 10, BoundaryMode.AUTO)
        plot.setDomainBoundaries(0, 100, BoundaryMode.AUTO)

        // create a dash effect for domain and range grid lines:
        val dashFx = DashPathEffect(
                floatArrayOf(PixelUtils.dpToPix(3f), PixelUtils.dpToPix(3f)), 0f)
        plot.graph.domainGridLinePaint.pathEffect = dashFx
        plot.graph.rangeGridLinePaint.pathEffect = dashFx


        return binding.root
    }


    override fun onDestroy() {
        super.onDestroy()
        // Unbind from the mService
        if (isConnected) {
            requireActivity().unbindService(connection)
            isConnected = false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.appBar.toolBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_setting -> {
                    findNavController().navigate(R.id.showSettingFragment)
                    true
                }
                else -> false
            }
        }

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>) = Unit

            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                (parent.adapter.getItem(position) as? String)?.let {
                    plot.rangeTitle.text = it

                }
                val type = when (position) {
                    0 -> SensorsType.temperature
                    1 -> SensorsType.humidity
                    2 -> SensorsType.co2
                    3 -> SensorsType.pm1
                    4 -> SensorsType.pm10
                    5 -> SensorsType.pm25
                    6 -> SensorsType.tvoc
                    else ->{
                        val exception = Exception("Invalid sensor type in position: $position")
                        Timber.e(exception)
                        throw exception
                    }
                }
                dataSource.sensorsType = type
                plot.redraw()
            }
        }

        deviceStateReceiver.setBtConnectionListener(object : DeviceStateListener {
            override fun onStartConnecting() = Unit

            override fun onFailedConnecting() = Unit

            override fun onCreateConnection() = Unit

            override fun onDestroyConnection() {
                Toast.makeText(requireContext(), "Disconnected", Toast.LENGTH_SHORT).show()
            }

            override fun onReceiveData(bytes: ByteArray, size: Int) {
                Timber.d("onReceiveData")
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

                binding.executeAfter {
                    this.sensors = sensors
                }
                dataSource.addData(sensors)
                plot.redraw()
            }
        })
    }

    fun initBlueToothService() {
        val intent = Intent(requireContext(), BTService::class.java)
        requireActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private val connection = object : ServiceConnection {

        override fun onServiceDisconnected(name: ComponentName?) {
            isConnected = false
        }

        override fun onServiceConnected(name: ComponentName?, iBinder: IBinder) {

            mService = (iBinder as BTService.LocalBinder).service
            isConnected = true

        }

    }

}