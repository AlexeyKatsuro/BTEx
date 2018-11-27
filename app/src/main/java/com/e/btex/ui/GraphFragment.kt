package com.e.btex.ui

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
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.androidplot.xy.*
import com.e.btex.R
import com.e.btex.connection.MyService
import com.e.btex.data.dto.Sensor
import com.e.btex.data.dto.Sensors
import com.e.btex.databinding.FragmentGraphBinding
import com.e.btex.utils.extensions.getName
import com.e.btex.utils.plot.DynamicXYDataSource
import com.e.btex.utils.plot.SensorSeries
import timber.log.Timber
import java.text.DecimalFormat


class GraphFragment : Fragment() {


    private lateinit var binding: FragmentGraphBinding


    private lateinit var plot: XYPlot
    private lateinit var data: DynamicXYDataSource

    private lateinit var sensorAdapter: SensorAdapter

    var isConnected = false

    private var service: MyService? = null

    private lateinit var series: SensorSeries

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {


        binding = FragmentGraphBinding.inflate(inflater, container, false)
        binding.appBar.toolBar.inflateMenu(R.menu.main_menu)


        sensorAdapter = SensorAdapter()

        binding.sensorRecyclerView.adapter = sensorAdapter


        plot = binding.plot

        plot.graph.getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).format = DecimalFormat("0")
        plot.graph.getLineLabelStyle(XYGraphWidget.Edge.LEFT).format = DecimalFormat("###.##")

        data = DynamicXYDataSource(Handler())

        data.setOnUpdateCallback {
            sensorAdapter.submitList(it.getSensorList())
            series.addSensorVal(it)
            plot.redraw()
        }

        series = SensorSeries(Sensor.Temperature::class)

        val formatter1 = LineAndPointFormatter(requireActivity(), R.xml.line_point_formatter_with_labels)

        plot.addSeries(series, formatter1)

        // thin out domain tick labels so they dont overlap each other:
        plot.domainStepMode = StepMode.INCREMENT_BY_FIT
        //plot.domainStepValue = 1.0

        plot.rangeStepMode = StepMode.INCREMENT_BY_FIT
        //plot.rangeStepValue = 1.0
//
        //plot.setRangeBoundaries(0, 10, BoundaryMode.AUTO)
        //plot.setDomainBoundaries(0, 100, BoundaryMode.AUTO)

        // create a dash effect for domain and range grid lines:
//        val dashFx = DashPathEffect(
//                floatArrayOf(PixelUtils.dpToPix(3f), PixelUtils.dpToPix(3f)), 0f)
//        plot.graph.domainGridLinePaint.pathEffect = dashFx
//        plot.graph.rangeGridLinePaint.pathEffect = dashFx


        return binding.root
    }

    override fun onResume() {
        // kick off the data generating thread:
       // Thread(data).start()
        super.onResume()
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(requireContext(), MyService::class.java)
        requireActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unbind from the service
        if (isConnected) {
            requireActivity().unbindService(connection)
            isConnected = false
        }
    }

    override fun onPause() {
        data.stopThread()
        super.onPause()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.appBar.toolBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_setting -> {
                    findNavController().navigate(R.id.showSettingFragment)
                    true
                }

                R.id.action_add -> {
                    data.update()
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
                    0 -> Sensor.Temperature::class
                    1 -> Sensor.Humidity::class
                    2 -> Sensor.Co2::class
                    3 -> Sensor.Pm1::class
                    4 -> Sensor.Pm25::class
                    5 -> Sensor.Pm10::class
                    6 -> Sensor.Tvoc::class
                    else ->{
                        val exception = Exception("Invalid sensor type in position: $position")
                        Timber.e(exception)
                        throw exception
                    }
                }
                series.setSensorClass(type)
                series.name = type.getName(resources)
                plot.redraw()
            }
        }
    }

    private val connection = object : ServiceConnection {

        override fun onServiceDisconnected(name: ComponentName?) {
            isConnected = false
        }

        override fun onServiceConnected(name: ComponentName?, iBinder: IBinder) {

            service = (iBinder as MyService.LocalBinder).service
            isConnected = true

        }

    }

}