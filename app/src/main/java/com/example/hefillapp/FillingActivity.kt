package com.example.hefillapp

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.hefillapp.com.example.hefillapp.DataBaseHandler
import com.example.hefillapp.com.example.hefillapp.FillLogDataClass
import com.google.gson.Gson
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.GridLabelRenderer
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
    private val initialXLimUpper: Double = 60.0
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
    private var elapsedTimeMinutes: Double = 0.0
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
        val date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MMM-yy"))

        // Set textviews
        findViewById<TextView>(R.id.textViewTitleFilling).apply{
            text = date
        }
        findViewById<TextView>(R.id.textViewFillingMagnet).apply{
            text = getString(R.string.view_log_file_magnet) + " " + magnetType
        }
        findViewById<TextView>(R.id.textViewFillingOperator).apply{
            text = getString(R.string.view_log_file_operator) + " " + fillOperator
        }
        findViewById<TextView>(R.id.textViewFillingTargetLevel).apply{
            text = getString(R.string.view_log_file_target_level) + " " + targetLevel + " %"
        }

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
                elapsedTimeMinutes = Duration.between(timeStart, timeNow).seconds.toDouble()/60.0

                // Start Chronometer to show elapsed time
                meter.base = SystemClock.elapsedRealtime()
                meter.start()
            }
            else {
                timeNow = LocalTime.now()
                elapsedTimeMinutes = Duration.between(timeStart, timeNow).seconds.toDouble()/60.0
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
                // Convert arraylists containing x and y values to string to store in SQLite
                val gson = Gson()
                val xValuesAsString = gson.toJson(listX)
                val yValuesAsString = gson.toJson(listY)

                val fillLogDataClass = FillLogDataClass(0,
                    dateAsString = date,
                    targetHeLevel= targetLevel.toDouble(),
                    averageRate = meanRate,
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
        seriesData.appendData(DataPoint(elapsedTimeMinutes, HeLevel), false, maxDataPoints)

        //reset the axis limits if necessary
        if (elapsedTimeMinutes > initialXLimUpper) {
            lineGraphView.viewport.setMaxX(elapsedTimeMinutes + 10.0)

            // Extend horizontal line for target Level
            seriesHorizontal.resetData(arrayOf(
                DataPoint(initialXLimLower, targetLevel.toDouble()),
                DataPoint(elapsedTimeMinutes + 10.0, targetLevel.toDouble())
            ))
        }

        // Add current data to list
        listX.clear()
        listY.clear()
        for (element in seriesData.getValues(0.0, elapsedTimeMinutes)) {
            listX.add(element.x)
            listY.add(element.y)
        }

        // Compute filling rate
        var lastRate = 0.0
        val deltaX = DoubleArray(listX.size -1)
        var expTimeAtLastRate = 0.0
        var expTimeAtMeanRate = 0.0
        // Reset average filling rate
        meanRate = 0.0
        if (listX.size > 1){
            for (i in deltaX.indices) {
                lastRate = (listY[i + 1] - listY[i]) / (listX[i + 1] - listX[i])
                meanRate += lastRate
            }
            meanRate /= (listX.size - 1).toDouble()

            // Compute expected time
            var deltaTarget = targetLevel.toDouble() - listY.last()
            deltaTarget = max(0.0, deltaTarget)
            expTimeAtMeanRate = deltaTarget/meanRate
            expTimeAtLastRate = deltaTarget/lastRate

            // Update linear extrapolation
            seriesExtrapolation.resetData(arrayOf(
                DataPoint(0.0, listY.first()),
                DataPoint((yLimUpper-listY.first())/max(meanRate, 0.0), yLimUpper)
            ))

        }

        // Update Text views with rates
        findViewById<TextView>(R.id.textViewFillingAverageRate).apply {
            text = getString(R.string.view_log_file_average_rate) + " " + String.format("%.2f", meanRate) +
                    "% / min ( " + String.format("%.1f", expTimeAtMeanRate) + " " + getString(R.string.text_fill_time_to_go) + ")"
        }
        findViewById<TextView>(R.id.textViewFillingCurrentRate).apply {
            text = getString(R.string.view_log_file_current_rate) + " " + String.format("%.2f", lastRate)+
                    " % / min ( " + String.format("%.1f", expTimeAtLastRate) + " " + getString(R.string.text_fill_time_to_go) + ")"
        }

        // Update Progress Bar
        progressBarHeLevel.progress = listY.last().toInt()

        // Show Toast if target level is reached
        if(HeLevel.toDouble() >= targetLevel.toDouble()) {
            Toast.makeText(
                applicationContext,
                "Great job ! " + ("\ud83e\udd73") + ("\ud83e\udd73") + ("\ud83e\udd73"),
                Toast.LENGTH_LONG
            ).show();
        }
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
        val databaseHandler = DataBaseHandler(this)
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