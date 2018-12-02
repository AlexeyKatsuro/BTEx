package com.e.btex.utils.plot

import com.androidplot.xy.XYSeries

class DynamicSeries(private val dataSource: XYDataSource, private val seriesIndex: Int) : XYSeries {



    override fun getTitle() = dataSource.getTitle(seriesIndex)

    override fun size() = dataSource.getItemCount(seriesIndex)

    override fun getX(index: Int) = dataSource.getX(seriesIndex, index)

    override fun getY(index: Int) = dataSource.getY(seriesIndex, index)
}