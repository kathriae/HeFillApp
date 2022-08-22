package com.example.hefillapp.com.example.hefillapp

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper

class DataBaseHandler(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object{
        private val DATABASE_VERSION = 2
        private val DATABASE_NAME = "HeFillLogDatabase"

        private val TABLE_FILL_LOG = "FillLogTable"
        private val KEY_ID = "_id"
        private val KEY_NAME = "name"
        private val KEY_FINAL_HE_LEVEL = "final_he_level"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_FILL_LOG_TABLE = ("CREATE TABLE " + TABLE_FILL_LOG + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_FINAL_HE_LEVEL + " NUMERIC" + ")")
        db?.execSQL(CREATE_FILL_LOG_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_FILL_LOG")
        onCreate(db)
    }

    /** Function to insert data */
    fun addLogEntry(fillLogEntry: FillLogDataClass): Long {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(KEY_NAME, fillLogEntry.name)
        contentValues.put(KEY_FINAL_HE_LEVEL, fillLogEntry.final_he_level)

        // Inserting employee details using insert query.
        val success = db.insert(TABLE_FILL_LOG, null, contentValues)
        //2nd argument is String containing nullColumnHack

        db.close() // Closing database connection
        return success
    }

    /** Method to read data from database */
    @SuppressLint("Range")
    fun viewRecord(): ArrayList<FillLogDataClass> {

        val recordList: ArrayList<FillLogDataClass> = ArrayList<FillLogDataClass>()

        // Query to select all the records from the table.
        val selectQuery = "SELECT  * FROM $TABLE_FILL_LOG"

        val db = this.readableDatabase
        // Cursor is used to read the record one by one. Add them to data model class.
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, null)

        } catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var id: Int
        var name: String
        var final_he_level: Long

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex(KEY_ID))
                name = cursor.getString(cursor.getColumnIndex(KEY_NAME))
                final_he_level = cursor.getLong(cursor.getColumnIndex(KEY_FINAL_HE_LEVEL))

                val record = FillLogDataClass(id = id, name = name, final_he_level = final_he_level)
                recordList.add(record)

            } while (cursor.moveToNext())
        }
        return recordList
    }
    /**
     * Function to delete record
     */
    fun deleteRecord(record: FillLogDataClass): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_ID, record.id)
        // Deleting Row
        val success = db.delete(TABLE_FILL_LOG, KEY_ID + "=" + record.id, null)
        //2nd argument is String containing nullColumnHack

        // Closing database connection
        db.close()
        return success
    }
}