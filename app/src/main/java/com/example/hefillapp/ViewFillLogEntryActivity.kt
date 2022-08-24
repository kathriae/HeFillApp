package com.example.hefillapp

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hefillapp.com.example.hefillapp.DataBaseHandler
import com.google.gson.Gson
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.jjoe64.graphview.GraphView
import kotlin.math.max
import kotlin.math.roundToInt

class ViewFillLogEntryActivity : AppCompatActivity() {

    // Widgets
    private lateinit var lineGraphView: GraphView

    // Constants
    private val yLimLower: Double = 20.0
    private val yLimUpper: Double = 100.0

    // Variables
    private var itemPosition: Int = -1
    private val seriesData: LineGraphSeries<DataPoint> = LineGraphSeries()
    private val seriesHorizontal: LineGraphSeries<DataPoint> = LineGraphSeries()
    private val seriesExtrapolation: LineGraphSeries<DataPoint> = LineGraphSeries()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_fill_log_entry)

        // Get the Intent that started this activity and extract the position of item we clicked
        itemPosition = intent.getIntExtra("EXTRA_ITEM_POSITION", -1)

        // Access clicked item in database and display in textviews for testing
        val databaseHandler = DataBaseHandler(this)
        val spaceString = " "
        if(itemPosition > -1){
            val record = databaseHandler.viewRecord()[itemPosition]

            // This is how I can convert the string of x values back to an array list and then use this to plot!
            val gson = Gson()
            val xValuesAsArray: ArrayList<Double> = gson.fromJson(record.timeValuesAsString,
                ArrayList::class.java) as ArrayList<Double>
            val yValuesAsArray: ArrayList<Double> = gson.fromJson(record.heLevelValuesAsString,
                ArrayList::class.java) as ArrayList<Double>

            // Display details of fill log file
            findViewById<TextView>(R.id.textViewTitleViewLogFile).apply{
                text = record.dateAsString
            }
            findViewById<TextView>(R.id.textViewLogMagnet).apply{
                text = getString(R.string.view_log_file_magnet) + spaceString + record.magnetType
            }
            findViewById<TextView>(R.id.textViewLogOperator).apply{
                text = getString(R.string.view_log_file_operator) + spaceString + record.operator
            }
            findViewById<TextView>(R.id.textViewLogTargetLevel).apply{
                text = getString(R.string.view_log_file_target_level) + spaceString + record.targetHeLevel.toString() + " %"
            }
            findViewById<TextView>(R.id.textViewLogFillDuration).apply{
                text = getString(R.string.view_log_file_total_duration) + spaceString + xValuesAsArray.last().roundToInt().toString() + " min"
            }
            findViewById<TextView>(R.id.textViewLogFillLevels).apply{
                text = getString(R.string.view_log_file_fill_levels) + spaceString +
                        yValuesAsArray.first().roundToInt().toString() + " % - " +
                        yValuesAsArray.last().roundToInt().toString() + " %"
            }
            findViewById<TextView>(R.id.textViewLogAverageRate).apply{
                text = getString(R.string.view_log_file_average_rate) + spaceString + String.format("%.2f", record.averageRate) + " % / min"
            }
            findViewById<TextView>(R.id.textViewLogComments).apply{
                text = getString(R.string.view_log_file_comments) + spaceString + record.comments
            }

            // Convert array lists to dataseries for plotting
            for(i in xValuesAsArray.indices){
                seriesData.appendData(DataPoint(xValuesAsArray[i], yValuesAsArray[i]), false, xValuesAsArray.size + 1)
            }

            // Create linegraph (same layout as in FillingActivity)
            lineGraphView = findViewById(R.id.idGraphViewFillLog)
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
            lineGraphView.viewport.setMinX(0.0)
            lineGraphView.viewport.setMaxX(xValuesAsArray.last() + 5.0)
            lineGraphView.viewport.setMinY(yLimLower)
            lineGraphView.viewport.setMaxY(yLimUpper)
            lineGraphView.viewport.isYAxisBoundsManual = true
            lineGraphView.viewport.isXAxisBoundsManual = true
            lineGraphView.gridLabelRenderer.horizontalAxisTitle = getString(R.string.plot_He_level_Xlabel)
            lineGraphView.gridLabelRenderer.verticalAxisTitle = getString(R.string.plot_He_level_YLabel)

            // Add data series to plot
            lineGraphView.addSeries(seriesData)

            // Add horizontal line at target level
            seriesHorizontal.resetData(arrayOf(
                DataPoint(0.0, record.targetHeLevel),
                DataPoint(xValuesAsArray.last(), record.targetHeLevel)
            ))
            seriesHorizontal.color = R.color.purple_200
            lineGraphView.addSeries(seriesHorizontal)

            // Add linear extrapolation with mean rate
            seriesExtrapolation.resetData(arrayOf(
                DataPoint(0.0, yValuesAsArray.first()),
                DataPoint((yLimUpper-yValuesAsArray.first())/ max(record.averageRate, 0.0), yLimUpper)
            ))
            seriesExtrapolation.color = R.color.purple_200
            lineGraphView.addSeries(seriesExtrapolation)
        }
        else{
            throw ArrayIndexOutOfBoundsException("ID of Log Item invalid")
        }

    }
}