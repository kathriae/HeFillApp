package com.example.hefillapp

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class FillInstructionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fill_instructions)

        findViewById<TextView>(R.id.textViewFillingInstructions).setMovementMethod(ScrollingMovementMethod())
    }
}