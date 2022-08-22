package com.example.hefillapp.com.example.hefillapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.hefillapp.FillLogActivity
import com.example.hefillapp.R
import com.example.hefillapp.com.example.hefillapp.FillLogDataClass

/** Class containing item adapter for scrollable recycler View
 * Will be used to display list of He fill logs */

class ItemAdapter(val context: Context, val items: ArrayList<FillLogDataClass>,
    val listener: OnItemClickListener) :
    RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    // Layout of individual items set in separate xml file
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_fill_log_row, parent, false
            )
        )
    }

    // Binding of individual items
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items.get(position)

        // Get value to display from FillLogDataClass
        holder.tvItem.text = item.name

        // Define action for delete button
        holder.ivDelete.setOnClickListener { view ->
            if (context is FillLogActivity) {
                context.deleteRecordAlertDialog(item)
            }
        }

    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener{
        val ivDelete = view.findViewById<ImageView>(R.id.imageViewDeleteLog)
        val tvItem = view.findViewById<TextView>(R.id.textViewLogItem)

        init {
            tvItem.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            val position : Int = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position)
            }
        }
    }

    // Create interface to handle events following the item click
    // interface is used to "forward" click event to eg fill log activity
    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }
}