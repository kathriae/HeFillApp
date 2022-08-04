package com.example.hefillapp

import android.os.Bundle
import android.widget.Button
import android.widget.Chronometer
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import java.time.Duration
import java.time.LocalTime
import kotlin.math.max
import kotlin.text.toDouble as toDouble

// TO - DO: KAAB: 04.08.2022
// - clean up variable names and remove unnecessary stuff
// set limits for plotting etc external and not in here
// refactor all names to be more descriptive (especially textview objects etc)
// change strings to come from strings resource file
// layout
// want to be able to go back and continue when coming back
// implement that one can go back and then resume the fill
// change x axis of plot to minutes (seconds is easier for debugging)
// stop button with dialog that asks if one wants to create a log file

class FillingActivity : AppCompatActivity(), dialog_fragment_enter_he_level.NewHeLevelAdded {

    // Widgets
    lateinit var lineGraphView: GraphView
    private var progressBar: ProgressBar? = null

    // Variables
    val series: LineGraphSeries<DataPoint> = LineGraphSeries()
    val seriesHorizontal: LineGraphSeries<DataPoint> = LineGraphSeries()
    val seriesExtrapolation: LineGraphSeries<DataPoint> = LineGraphSeries()
    var timeStart = LocalTime.now()
    lateinit var timeNow: LocalTime
    var elapsedSeconds: Double = 0.0
    lateinit var targetLevel: String
    private var progressPercent = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filling)

        // Get the Intent that started this activity and extract the string for target Level
        targetLevel = intent.getStringExtra("EXTRA_TARGET_LEVEL").toString()

        //Chronometer
        val meter = findViewById<Chronometer>(R.id.chronometer)

        //Progress Bar
        progressBar = findViewById<ProgressBar>(R.id.progress_Bar) as ProgressBar
        progressBar!!.max = targetLevel.toInt()
        progressBar!!.progress = progressPercent

        // Create empty linegraph
        lineGraphView = findViewById(R.id.idGraphView)
        lineGraphView.animate()
        lineGraphView.viewport.isScrollable = true
        lineGraphView.viewport.isScalable = true
        lineGraphView.viewport.setScalableY(true)
        lineGraphView.viewport.setScrollableY(true)
        // Set appearance of datapoints
        series.color = R.color.purple_200
        series.isDrawDataPoints = true
        series.dataPointsRadius = 10F
        series.thickness = 8
        // Set limits and axes
        lineGraphView.viewport.setMinX(0.0)
        lineGraphView.viewport.setMaxX(50.0)
        lineGraphView.viewport.setMinY(20.0)    //below this level the magnet is quenched anyway
        lineGraphView.viewport.setMaxY(100.0)
        lineGraphView.viewport.isYAxisBoundsManual = true
        lineGraphView.viewport.isXAxisBoundsManual = true
        lineGraphView.gridLabelRenderer.horizontalAxisTitle = "Time [s]"
        lineGraphView.gridLabelRenderer.verticalAxisTitle = "He Level [%]"

        // Add horizontal line at target level
        seriesHorizontal.resetData(arrayOf<DataPoint>(
            DataPoint(0.0, targetLevel?.toDouble()!!),
            DataPoint(50.0, targetLevel?.toDouble()!!)
        ))
        seriesHorizontal.color = R.color.purple_200
        lineGraphView.addSeries(seriesHorizontal)

        // Add extrapolation series
        seriesExtrapolation.color = R.color.purple_200
        lineGraphView.addSeries(seriesExtrapolation)

        // Add series to plot
        lineGraphView.addSeries(series)

        var clickCount = 0
        val buttonView = findViewById<Button>(R.id.button5).setOnClickListener {
            //check if button pressed the first time
            if(clickCount == 0) {
                timeStart = LocalTime.now()
                timeNow = timeStart
                elapsedSeconds = Duration.between(timeStart, timeNow).seconds.toDouble()

                // Start Chronometer to show elapsed time
                meter.start()
            }
            else {
                timeNow = LocalTime.now()
                elapsedSeconds = Duration.between(timeStart, timeNow).seconds.toDouble()
            }

            //open dialog when button is clicked
            var dialog = dialog_fragment_enter_he_level()
            dialog.show(supportFragmentManager, "customDialog")

            clickCount++
        }

    }

    override fun receiveHeLevel(HeLevel: Double) {

        // Add point to data series
        val newDataPoint = DataPoint(elapsedSeconds, HeLevel)
        series.appendData(newDataPoint, false, 1000)

        //reset the axis limits if necessary
        if (elapsedSeconds > 50) {
            lineGraphView.viewport.setMaxX(elapsedSeconds + 10.0)

            // Extend horizontal line for target Level
            seriesHorizontal.resetData(arrayOf<DataPoint>(
                DataPoint(0.0, targetLevel?.toDouble()!!),
                DataPoint(elapsedSeconds + 10.0, targetLevel?.toDouble()!!)
            ))
        }

        // Create lists with X and Y values
        val listX: ArrayList<Double> = ArrayList()
        val listY: ArrayList<Double> = ArrayList()
        for (element in series.getValues(0.0, elapsedSeconds)) {
            listX.add(element.x)
            listY.add(element.y)
        }

        // Compute filling rate
        var lastRate = 0.0
        var meanRate = 0.0
        val deltaX = DoubleArray(listX.size -1)
        var deltaTarget = 0.0
        var expTimeAtLastRate = 0.0
        var expTimeAtMeanRate = 0.0

        if (listX.size > 1){
            for (i in deltaX.indices) {
                lastRate = (listY[i + 1] - listY[i]) / (listX[i + 1] - listX[i])
                meanRate += lastRate
            }
            meanRate /= (listX.size - 1).toDouble()

            // Compute expected time
            deltaTarget = targetLevel?.toDouble()!! - listY.last()
            deltaTarget = max(0.0, deltaTarget)
            expTimeAtMeanRate = deltaTarget/meanRate
            expTimeAtLastRate = deltaTarget/lastRate

            // Update linear extrapolation
            seriesExtrapolation.resetData(arrayOf<DataPoint>(
                DataPoint(0.0, listY.first()),
                DataPoint((100.0-listY.first())/max(meanRate, 0.0), 100.0)
            ))

        }

        // Update Text view
        val textView = findViewById<TextView>(R.id.textView2).apply {
            text = "Average Rate: " + meanRate.toString() + " , Current Rate: " + lastRate.toString() +
                    ", diff to target: " + deltaTarget.toString() + " ,to go at mean:" + expTimeAtMeanRate.toString() +
                    " , to go at current: " + expTimeAtLastRate.toString()
        }

        // Update Progress Bar
        progressBar!!.progress = listY.last().toInt()

    }

}