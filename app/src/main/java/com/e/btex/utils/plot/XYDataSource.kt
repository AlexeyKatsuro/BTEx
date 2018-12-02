package com.e.btex.utils.plot

import android.os.Handler
import com.androidplot.xy.XYSeries
import com.e.btex.data.dto.Sensors
import java.util.*

interface XYDataSource{

    fun getItemCount(seriesIndex: Int): Int

    fun getX(seriesIndex: Int, index: Int): Number

    fun getY(seriesIndex: Int, index: Int): Number

    fun getTitle(seriesIndex: Int): String
}