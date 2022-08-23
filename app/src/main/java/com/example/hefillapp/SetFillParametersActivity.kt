package com.example.hefillapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.EditText

class SetFillParametersActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_fill_parameters)

        val spinner: Spinner = findViewById(R.id.magnets_spinner)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.magnets_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }
    }

    /** Called when the user taps the start fill button*/
    fun startNewFill(view: View) {
        val targetLevel = findViewById<EditText>(R.id.editTextNumberTargetLevel).text.toString()
        val magnetType = findViewById<Spinner>(R.id.magnets_spinner).selectedItem.toString()
        val operator = findViewById<EditText>(R.id.editTextOperator).text.toString()
        val intent = Intent(this, FillingActivity::class.java).apply {
            putExtra("EXTRA_TARGET_LEVEL", targetLevel)
            putExtra("EXTRA_MAGNET_TYPE", magnetType)
            putExtra("EXTRA_OPERATOR", operator)
        }
        startActivity(intent)
    }
}