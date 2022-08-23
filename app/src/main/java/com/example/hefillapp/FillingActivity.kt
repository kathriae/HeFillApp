package com.example.hefillapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.hefillapp.com.example.hefillapp.DataBaseHandler
import com.example.hefillapp.com.example.hefillapp.FillLogDataClass
import com.google.gson.Gson
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.max

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
    private var elapsedTimeSeconds: Double = 0.0
    private var meanRate = 0.0
    private lateinit var targetLevel: String
    private lateinit var magnetType: String
    private lateinit var fillOperator:String
    private var progressPercent = 0
    // Create lists with X and Y values
    private val listX: ArrayList<Double> = ArrayList()
    private val listY: ArrayList<Double> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filling)

        // Get fill parameters from intent that started the filling activity
        targetLevel = intent.getStringExtra("EXTRA_TARGET_LEVEL").toString()
        magnetType = intent.getStringExtra("EXTRA_MAGNET_TYPE").toString()
        fillOperator = intent.getStringExtra("EXTRA_OPERATOR").toString()

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
        findViewById<Button>(R.id.button_measure_He_level).setOnClickListener {

            // Start chronometer when measure button pressed for the first time
            if(clickCount == 0) {
                timeStart = LocalTime.now()
                timeNow = timeStart
                elapsedTimeSeconds = Duration.between(timeStart, timeNow).seconds.toDouble()

                // Start Chronometer to show elapsed time
                meter.start()
            }
            else {
                timeNow = LocalTime.now()
                elapsedTimeSeconds = Duration.between(timeStart, timeNow).seconds.toDouble()
            }

            // Open adding He level dialog popup
            val dialogMeasureLevel = dialog_fragment_enter_he_level()
            dialogMeasureLevel.show(supportFragmentManager, "customDialog")

            clickCount++
        }

        // Stop fill button
        findViewById<Button>(R.id.button_stop_fill).setOnClickListener{

            // If data points present: open save log file dialog
            if(listX.size > 0) {
                // Create Fill Log Data class to be saved in log file
                val date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MMM-yy"))
                // Convert arraylists containing x and y values to string to store in SQLite
                val gson = Gson()
                val xValuesAsString = gson.toJson(listX)
                val yValuesAsString = gson.toJson(listY)

                val fillLogDataClass: FillLogDataClass = FillLogDataClass(0,
                    dateAsString = date,
                    targetHeLevel= targetLevel.toLong(),
                    averageRate = meanRate.toLong(),
                    heLevelValuesAsString = yValuesAsString,
                    timeValuesAsString = xValuesAsString,
                    operator = fillOperator,
                    magnetType = magnetType,
                    comments = "")

                // Open stop fill dialog
                stopFillAlertDialog(fillLogDataClass)
            }
            else{
                Toast.makeText(
                    applicationContext,
                    "no datapoints taken yet",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

    }

    override fun receiveHeLevel(HeLevel: Double) {

        // Add point to data series
        seriesData.appendData(DataPoint(elapsedTimeSeconds, HeLevel), false, maxDataPoints)

        //reset the axis limits if necessary
        if (elapsedTimeSeconds > initialXLimUpper) {
            lineGraphView.viewport.setMaxX(elapsedTimeSeconds + 10.0)

            // Extend horizontal line for target Level
            seriesHorizontal.resetData(arrayOf(
                DataPoint(initialXLimLower, targetLevel.toDouble()),
                DataPoint(elapsedTimeSeconds + 10.0, targetLevel.toDouble())
            ))
        }

        // Add current data to list
        listX.clear()
        listY.clear()
        for (element in seriesData.getValues(0.0, elapsedTimeSeconds)) {
            listX.add(element.x)
            listY.add(element.y)
        }

        // Compute filling rate
        var lastRate: Double = 0.0
        val deltaX = DoubleArray(listX.size -1)
        var deltaTarget: Double = 0.0
        var expTimeAtLastRate: Double = 0.0
        var expTimeAtMeanRate: Double = 0.0
        // Reset average filling rate
        meanRate = 0.0
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
        builder.setTitle(R.string.header_stop_fill_dialog)
        //set message for alert dialog
        builder.setMessage(R.string.save_log_file_dialog)
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //performing positive action
        builder.setPositiveButton(R.string.stop_fill_dialog_save_log_button) { dialogInterface, which ->
            // Open dialog for entering comments
            addCommentDialog(fillLogDataClass)
        }
        //performing negative action: dismiss and go back to main activity
        builder.setNegativeButton(R.string.stop_fill_dialog_no_save_log_button) { dialogInterface, which ->
            dialogInterface.dismiss()
            // Go back to main activity
            val intentMain = Intent(this, MainActivity::class.java)
            startActivity(intentMain)
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.show()
    }

    //Method for saving new entry in fill log
    private fun addRecord(fillLogDataClass: FillLogDataClass) {
        val databaseHandler: DataBaseHandler = DataBaseHandler(this)
        if (!fillLogDataClass.dateAsString.isEmpty()) {
            val status =
                databaseHandler.addLogEntry(FillLogDataClass(0,
                    dateAsString = fillLogDataClass.dateAsString,
                    targetHeLevel = fillLogDataClass.targetHeLevel,
                    timeValuesAsString = fillLogDataClass.timeValuesAsString,
                    heLevelValuesAsString = fillLogDataClass.heLevelValuesAsString,
                    averageRate = fillLogDataClass.averageRate,
                    magnetType = fillLogDataClass.magnetType,
                    operator = fillLogDataClass.operator,
                    comments = fillLogDataClass.comments))
            if (status > -1) {
                // Adding entry was succesful
                Toast.makeText(
                    applicationContext,
                    R.string.message_log_file_added,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Function for adding comments dialog before saving log file
    private fun addCommentDialog(fillLogDataClass: FillLogDataClass){

        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        builder.setTitle(R.string.header_add_comment_dialog)
        val dialogLayout = inflater.inflate(R.layout.fragment_dialog_add_comment, null)
        val editText  = dialogLayout.findViewById<EditText>(R.id.editTextAddCommentDialog)
        builder.setView(dialogLayout)
        builder.setPositiveButton(R.string.add_comment_ok_button) { dialogInterface, which ->
            // Add comment prior to adding log entry
            val comment = editText.text.toString()
            fillLogDataClass.comments = comment
            // Add record to fill log
            addRecord(fillLogDataClass)

            dialogInterface.dismiss()
            // Go back to main activity
            val intentMain = Intent(this, MainActivity::class.java)
            startActivity(intentMain)
        }
        builder.show()
    }

}