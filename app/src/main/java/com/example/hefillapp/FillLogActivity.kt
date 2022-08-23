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
        setupListOfDataIntoRecyclerView()
    }

    private fun getItemsList(): ArrayList<FillLogDataClass> {
        val databaseHandler: DataBaseHandler = DataBaseHandler(this)
        // Get list of records in database
        val RecordList: ArrayList<FillLogDataClass> = databaseHandler.viewRecord()
        return RecordList
    }

    // Interface for itemClick in RecyclerView
    override fun onItemClick(position: Int) {
        val databaseHandler: DataBaseHandler = DataBaseHandler(this)
        val record = databaseHandler.viewRecord()[position]

        // Open new activity to display log file
        val intent = Intent(this, ViewFillLogEntryActivity::class.java)
        intent.putExtra("EXTRA_ITEM_POSITION", position);
        startActivity(intent)
    }

    /** Function is used to show the list on UI of inserted data.
    */
    private fun setupListOfDataIntoRecyclerView() {

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
        }
    }

    // Called when delete icon is pressed: opens dialog
    fun deleteRecordAlertDialog(fillLogDataClass: FillLogDataClass) {
        val builder = AlertDialog.Builder(this)
        //  Title for alert dialog
        builder.setTitle(R.string.header_delete_log_dialog)
        // Message for alert dialog
        val message: String = getString(R.string.message_delete_log_dialog) + " " + fillLogDataClass.dateAsString
        builder.setMessage(message)
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        // Positive action
        builder.setPositiveButton(R.string.yes_button) { dialogInterface, which ->

            val databaseHandler: DataBaseHandler = DataBaseHandler(this)

            //calling the deleteRecord method of DataBaseHandler class to delete record
            val status = databaseHandler.deleteRecord(FillLogDataClass(fillLogDataClass.id,
                "",
                0L,
                "",
                "",
                "",
                "",
                0L,
                ""))
            if (status > -1) {
                Toast.makeText(
                    applicationContext,
                    R.string.message_record_deleted,
                    Toast.LENGTH_LONG
                ).show()
                // Reload recycler view and only show remaining entries
                setupListOfDataIntoRecyclerView()
            }

            dialogInterface.dismiss()
        }
        //performing negative action
        builder.setNegativeButton(R.string.no_button) { dialogInterface, which ->
            dialogInterface.dismiss()
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false) // Dont allow to cancel after clicking on remaining screen area
        alertDialog.show()
    }
}