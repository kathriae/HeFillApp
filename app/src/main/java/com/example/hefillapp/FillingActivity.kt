package com.example.hefillapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class FillingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filling)

        // Get the Intent that started this activity and extract the string
        val targetLevel = intent.getStringExtra("EXTRA_TARGET_LEVEL")

        // Capture the layout's TextView and set the string as its text
        val textView = findViewById<TextView>(R.id.textView2).apply {
            text = targetLevel
        }
    }
}