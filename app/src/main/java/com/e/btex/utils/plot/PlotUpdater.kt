package com.e.btex.utils.plot

import com.androidplot.Plot
import java.util.*

class PlotUpdater(val plot: Plot<*, *, *, *, *>): Observer {

    override fun update(o: Observable, arg: Any) {
        plot.redraw()
    }
}