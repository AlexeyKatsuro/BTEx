package com.e.btex.utils.plot

import com.androidplot.xy.XYSeries

class DynamicSeries(private val dataSource: DynamicXYDataSource, private val seriesIndex: Int, private val title: String) : XYSeries {

    override fun getTitle(): String {
        return dataSource.getTitle(seriesIndex).let {
            if(it.isEmpty())
                title
            else
                it
        }
    }

    override fun size(): Int {
        return dataSource.getItemCount(seriesIndex)
    }

    override fun getX(index: Int): Number {
        return dataSource.getX(seriesIndex, index)
    }

    override fun getY(index: Int): Number {

        return dataSource.getY(seriesIndex, index)
    }
}