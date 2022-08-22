package com.example.hefillapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.hefillapp.com.example.hefillapp.DataBaseHandler

class ViewFillLogEntryActivity : AppCompatActivity() {

    // Variables
    private var itemPosition: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_fill_log_entry)

        // Get the Intent that started this activity and extract the position of item we clicked
        itemPosition = intent.getIntExtra("EXTRA_ITEM_POSITION", -1)

        // Access clicked item in database and display in textviews for testing
        val databaseHandler: DataBaseHandler = DataBaseHandler(this)
        if(itemPosition > -1){
            val record = databaseHandler.viewRecord()[itemPosition]
            findViewById<TextView>(R.id.textView_test).apply{
                text = record.name
            }
            findViewById<TextView>(R.id.textView_test2).apply{
                text = record.final_he_level.toString()
            }
        }


    }
}