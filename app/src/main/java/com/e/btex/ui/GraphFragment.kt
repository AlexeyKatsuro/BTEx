package com.e.btex.ui

import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.androidplot.util.PixelUtils
import com.androidplot.xy.*
import com.e.btex.R
import com.e.btex.data.dto.Sensors
import com.e.btex.databinding.FragmentGraphBinding
import com.e.btex.utils.plot.DynamicSeries
import com.e.btex.utils.plot.DynamicXYDataSource
import com.e.btex.utils.plot.PlotUpdater
import java.text.DecimalFormat

class GraphFragment : Fragment() {


    private lateinit var binding: FragmentGraphBinding

    private val sensors = Sensors()

    private lateinit var plot: XYPlot
    private lateinit var plotUpdater: PlotUpdater
    private lateinit var data: DynamicXYDataSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {


        binding = FragmentGraphBinding.inflate(inflater, container, false).apply {
            sensors = this@GraphFragment.sensors
        }
        binding.appBar.toolBar.inflateMenu(R.menu.main_menu)

        plot = binding.plot
        plotUpdater = PlotUpdater(plot)

        plot.graph.getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).format = DecimalFormat("0")
        plot.graph.getLineLabelStyle(XYGraphWidget.Edge.LEFT).format = DecimalFormat("###.##")

        data = DynamicXYDataSource()
        val sine1Series = DynamicSeries(data, 0, "Sine 1")

        val formatter1 = LineAndPointFormatter(
                Color.rgb(0, 200, 0), null, null, null)
        formatter1.linePaint.strokeJoin = Paint.Join.ROUND
        formatter1.linePaint.strokeWidth = 5f

        plot.addSeries(sine1Series, formatter1)
        data.addObserver(plotUpdater)
        // thin out domain tick labels so they dont overlap each other:
        plot.domainStepMode = StepMode.INCREMENT_BY_FIT
        plot.domainStepValue = 1.0

        plot.rangeStepMode = StepMode.INCREMENT_BY_VAL
        plot.rangeStepValue = 1.0

        plot.setRangeBoundaries(0, 10, BoundaryMode.FIXED)
        plot.setDomainBoundaries(-10, 100, BoundaryMode.AUTO)

        // create a dash effect for domain and range grid lines:
        val dashFx = DashPathEffect(
                floatArrayOf(PixelUtils.dpToPix(3f), PixelUtils.dpToPix(3f)), 0f)
        plot.graph.domainGridLinePaint.pathEffect = dashFx
        plot.graph.rangeGridLinePaint.pathEffect = dashFx



        return binding.root
    }

    override fun onResume() {
        // kick off the data generating thread:
        Thread(data).start()
        super.onResume()
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
                else -> false
            }
        }

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>) = Unit

            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                (parent.adapter.getItem(position) as? String)?.let {
                    plot.rangeTitle.text = it
                    plot.redraw()
                }
            }

        }


    }

}