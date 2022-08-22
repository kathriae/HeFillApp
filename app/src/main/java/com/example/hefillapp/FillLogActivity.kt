package com.example.hefillapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hefillapp.com.example.hefillapp.DataBaseHandler
import com.example.hefillapp.com.example.hefillapp.FillLogDataClass
import com.example.hefillapp.com.example.hefillapp.adapters.ItemAdapter

class FillLogActivity : AppCompatActivity(), ItemAdapter.OnItemClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fill_log)

        // Show recycler view with fill log entries
        setupListofDataIntoRecyclerView()
    }

    private fun getItemsList(): ArrayList<FillLogDataClass> {
        //creating the instance of DatabaseHandler class
        val databaseHandler: DataBaseHandler = DataBaseHandler(this)
        //calling the viewEmployee method of DatabaseHandler class to read the records
        val RecordList: ArrayList<FillLogDataClass> = databaseHandler.viewRecord()

        return RecordList
    }

    // interface method to define what happens upon clicking item in recyclerview
    override fun onItemClick(position: Int) {
        Toast.makeText(this, "pressed item no $position", Toast.LENGTH_SHORT).show()
        val databaseHandler: DataBaseHandler = DataBaseHandler(this)
        val record = databaseHandler.viewRecord()[position]
        Toast.makeText(this, "final he level of item ${record.final_he_level}", Toast.LENGTH_SHORT).show()

        // Open new activity
        val intent = Intent(this, ViewFillLogEntryActivity::class.java)
        intent.putExtra("EXTRA_ITEM_POSITION", position);
        startActivity(intent)
    }

    /** Function is used to show the list on UI of inserted data.
    */
    private fun setupListofDataIntoRecyclerView() {
        // Show recycler View if entries are present
        if (getItemsList().size > 0) {
            // Set the LayoutManager that this RecyclerView will use.
            findViewById<RecyclerView>(R.id.recyclerViewLogItems).layoutManager = LinearLayoutManager(this)
            // Adapter class is initialized and list is passed in the param.
            val itemAdapter = ItemAdapter(this, getItemsList(), this)
            // adapter instance is set to the recyclerview to inflate the items.
            findViewById<RecyclerView>(R.id.recyclerViewLogItems).adapter = itemAdapter
        } else {
            findViewById<RecyclerView>(R.id.recyclerViewLogItems).isInvisible = true
            Toast.makeText(
                applicationContext,
                "No entries to show.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Called when delete icon is pressed: opens dialog
    fun deleteRecordAlertDialog(fillLogDataClass: FillLogDataClass) {
        val builder = AlertDialog.Builder(this)
        //set title for alert dialog
        builder.setTitle("Delete Record")
        //set message for alert dialog
        builder.setMessage("Are you sure you wants to delete ${fillLogDataClass.name}.")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //performing positive action
        builder.setPositiveButton("Yes") { dialogInterface, which ->

            //creating the instance of DatabaseHandler class
            val databaseHandler: DataBaseHandler = DataBaseHandler(this)
            //calling the deleteRecord method of DataBaseHandler class to delete record
            val status = databaseHandler.deleteRecord(FillLogDataClass(fillLogDataClass.id, "", 0L))
            if (status > -1) {
                Toast.makeText(
                    applicationContext,
                    "Record deleted successfully.",
                    Toast.LENGTH_LONG
                ).show()
                // Reload recycler view and only show remaining entries
                setupListofDataIntoRecyclerView()
            }

            dialogInterface.dismiss()
        }
        //performing negative action
        builder.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss()
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false) // Dont allow to cancel after clicking on remaining screen area
        alertDialog.show()
    }
}