package com.example.hefillapp

import android.R.attr.data
import android.app.PendingIntent.getActivity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.hefillapp.com.example.hefillapp.DataBaseHandler
import com.example.hefillapp.com.example.hefillapp.FillLogDataClass
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import java.time.Duration
import java.time.LocalTime
import kotlin.math.max


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
    private lateinit var lineGraphView: GraphView
    private lateinit var progressBarHeLevel: ProgressBar
    private lateinit var meter: Chronometer

    // Constants
    private val initialXLimUpper: Double = 50.0
    private val initialXLimLower: Double = 0.0
    private val yLimLower: Double = 20.0
    private val yLimUpper: Double = 100.0
    private val maxDataPoints: Int = 1000

    // Variables
    private val seriesData: LineGraphSeries<DataPoint> = LineGraphSeries()
    private val seriesHorizontal: LineGraphSeries<DataPoint> = LineGraphSeries()
    private val seriesExtrapolation: LineGraphSeries<DataPoint> = LineGraphSeries()
    private var timeStart: LocalTime = LocalTime.now()
    private lateinit var timeNow: LocalTime
    private var elapsedSeconds: Double = 0.0
    private lateinit var targetLevel: String
    private var progressPercent = 0
    // Create lists with X and Y values
    val listX: ArrayList<Double> = ArrayList()
    val listY: ArrayList<Double> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filling)

        // Get the Intent that started this activity and extract the string for target Level
        targetLevel = intent.getStringExtra("EXTRA_TARGET_LEVEL").toString()

        //Chronometer
        findViewById<Chronometer>(R.id.chronometer).also { meter = it }

        //Progress Bar
        (findViewById<ProgressBar>(R.id.progress_Bar) as ProgressBar).also { progressBarHeLevel = it }
        progressBarHeLevel.max = targetLevel.toInt()
        progressBarHeLevel.progress = progressPercent

        // Create empty linegraph
        lineGraphView = findViewById(R.id.idGraphView)
        lineGraphView.animate()
        lineGraphView.viewport.isScrollable = true
        lineGraphView.viewport.isScalable = true
        lineGraphView.viewport.setScalableY(true)
        lineGraphView.viewport.setScrollableY(true)
        // Set appearance of datapoints
        seriesData.color = R.color.purple_200
        seriesData.isDrawDataPoints = true
        seriesData.dataPointsRadius = 10F
        seriesData.thickness = 8
        // Set limits and axes
        lineGraphView.viewport.setMinX(initialXLimLower)
        lineGraphView.viewport.setMaxX(initialXLimUpper)
        lineGraphView.viewport.setMinY(yLimLower)
        lineGraphView.viewport.setMaxY(yLimUpper)
        lineGraphView.viewport.isYAxisBoundsManual = true
        lineGraphView.viewport.isXAxisBoundsManual = true
        lineGraphView.gridLabelRenderer.horizontalAxisTitle = getString(R.string.plot_He_level_Xlabel)
        lineGraphView.gridLabelRenderer.verticalAxisTitle = getString(R.string.plot_He_level_YLabel)

        // Add horizontal line at target level
        seriesHorizontal.resetData(arrayOf(
            DataPoint(initialXLimLower, targetLevel.toDouble()),
            DataPoint(initialXLimUpper, targetLevel.toDouble())
        ))
        seriesHorizontal.color = R.color.purple_200
        lineGraphView.addSeries(seriesHorizontal)

        // Add extrapolation series
        seriesExtrapolation.color = R.color.purple_200
        lineGraphView.addSeries(seriesExtrapolation)

        // Add series to plot
        lineGraphView.addSeries(seriesData)

        var clickCount = 0
        val buttonMeasureHeLevel = findViewById<Button>(R.id.button_measure_He_level).setOnClickListener {
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

            // Open adding He level dialog popup
            val dialogMeasureLevel = dialog_fragment_enter_he_level()
            dialogMeasureLevel.show(supportFragmentManager, "customDialog")

            clickCount++
        }

        // Functionality for stop button
        findViewById<Button>(R.id.button_stop_fill).setOnClickListener{
            // Create Data class to be saved in log
            val ts = "ItemAdded"
            var tl : Long = 0L
            // this is how I can pass the final He level to my Log data class and use it for display later
            tl = listY.last().toLong()
            val testFillDataClass : FillLogDataClass = FillLogDataClass(0, ts, tl)

            // Open stop fill dialog
            stopFillAlertDialog(testFillDataClass)
        }

    }

    override fun receiveHeLevel(HeLevel: Double) {

        // Add point to data series
        seriesData.appendData(DataPoint(elapsedSeconds, HeLevel), false, maxDataPoints)

        //reset the axis limits if necessary
        if (elapsedSeconds > initialXLimUpper) {
            lineGraphView.viewport.setMaxX(elapsedSeconds + 10.0)

            // Extend horizontal line for target Level
            seriesHorizontal.resetData(arrayOf(
                DataPoint(initialXLimLower, targetLevel.toDouble()),
                DataPoint(elapsedSeconds + 10.0, targetLevel.toDouble())
            ))
        }

        // Add current data to list
        listX.clear()
        listY.clear()
        for (element in seriesData.getValues(0.0, elapsedSeconds)) {
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
            deltaTarget = targetLevel.toDouble() - listY.last()
            deltaTarget = max(0.0, deltaTarget)
            expTimeAtMeanRate = deltaTarget/meanRate
            expTimeAtLastRate = deltaTarget/lastRate

            // Update linear extrapolation
            seriesExtrapolation.resetData(arrayOf(
                DataPoint(0.0, listY.first()),
                DataPoint((yLimUpper-listY.first())/max(meanRate, 0.0), yLimUpper)
            ))

        }

        // Update Text view
        val textView = findViewById<TextView>(R.id.textView2).apply {
            text = "Average Rate: " + meanRate.toString() + " , Current Rate: " + lastRate.toString() +
                    ", diff to target: " + deltaTarget.toString() + " ,to go at mean:" + expTimeAtMeanRate.toString() +
                    " , to go at current: " + expTimeAtLastRate.toString()
        }

        // Update Progress Bar
        progressBarHeLevel.progress = listY.last().toInt()

    }

    // Dialog to open when stop button is pressed
    fun stopFillAlertDialog(fillLogDataClass: FillLogDataClass) {
        val builder = AlertDialog.Builder(this)
        //set title for alert dialog
        builder.setTitle("Stop fill")
        //set message for alert dialog
        builder.setMessage("Do you want to save log file?")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //performing positive action
        builder.setPositiveButton("Yes and back to main") { dialogInterface, which ->
            //add record to fill log
            addRecord(fillLogDataClass)
                Toast.makeText(
                    applicationContext,
                    "Record added ${fillLogDataClass.name}.",
                    Toast.LENGTH_LONG
                ).show()
            dialogInterface.dismiss()

            // Go back to main activity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        //performing negative action: dismiss and go back to main activity
        builder.setNegativeButton("No and back to main") { dialogInterface, which ->
            dialogInterface.dismiss()

            // Go back to main activity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.show()
    }

    //Method for saving new entry in fill log
    private fun addRecord(fillLogDataClass: FillLogDataClass) {
        val databaseHandler: DataBaseHandler = DataBaseHandler(this)
        if (!fillLogDataClass.name.isEmpty()) {
            val status =
                databaseHandler.addLogEntry(FillLogDataClass(0, fillLogDataClass.name, fillLogDataClass.final_he_level))
            if (status > -1) {
                Toast.makeText(applicationContext, "Record saved", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(
                applicationContext,
                "Name cannot be blank",
                Toast.LENGTH_LONG
            ).show()
        }
    }

}