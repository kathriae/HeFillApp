package com.example.hefillapp

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import java.lang.ClassCastException
import java.lang.Exception

class dialog_fragment_enter_he_level: DialogFragment() {

    interface NewHeLevelAdded{
        fun receiveHeLevel(HeLevel: Double)
    }

    lateinit var newHeLevelAdded: NewHeLevelAdded

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            newHeLevelAdded = context as NewHeLevelAdded
        }catch (e : ClassCastException){
            e.printStackTrace()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var rootView: View = inflater.inflate(R.layout.fragment_enter_he_level, container, false)

        // Cancel Button pressed
        rootView.findViewById<Button>(R.id.buttonCancelHeLevelDialog).setOnClickListener {
            dismiss()
        }

        // Add Button pressed
        rootView.findViewById<Button>(R.id.buttonAddHeLevelDialog).setOnClickListener {
            val enteredHeLevel =
                rootView.findViewById<EditText>(R.id.editTextNumberHeLevelDialog).text.toString()
            if(!enteredHeLevel.isEmpty()) {
                newHeLevelAdded.receiveHeLevel(enteredHeLevel.toDouble())
            }
            dismiss()
        }

        return rootView
    }

}
