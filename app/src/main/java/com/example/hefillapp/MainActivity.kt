package com.example.hefillapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    /** Called when user taps Start new He fill activity button */
    fun setFillParameters(view: View) {
        val intent = Intent(this, SetFillParametersActivity::class.java)
        startActivity(intent)
    }

    /** Called when user taps He fill log button */
    fun openFillLog(view: View){
        val intent = Intent(this, FillLogActivity::class.java)
        startActivity(intent)
    }

    /** Called when the user taps He fill instructions button */
    fun openFillInstructions(view: View) {
        val intent = Intent(this, FillInstructionsActivity::class.java)
        startActivity(intent)
    }
}