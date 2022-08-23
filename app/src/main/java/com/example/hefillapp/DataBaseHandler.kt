package com.example.hefillapp.com.example.hefillapp

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper

class DataBaseHandler(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME,
    null, DATABASE_VERSION) {

    companion object{
        private val DATABASE_VERSION = 4
        private val DATABASE_NAME = "HeFillLogDatabase"

        private val TABLE_FILL_LOG = "FillLogTable"
        private val KEY_ID = "_id"
        private val KEY_DATE = "dateAsString"
        private val KEY_TARGET_HE_LEVEL = "targetHeLevel"
        private val KEY_TIME_VALUES = "timeValuesAsString"
        private val KEY_HE_LEVEL_VALUES = "heLevelValuesAsString"
        private val KEY_OPERATOR = "operator"
        private val KEY_MAGNET = "magnet"
        private val KEY_RATE = "averageRate"
        private val KEY_COMMENTS = "comments"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_FILL_LOG_TABLE = ("CREATE TABLE " + TABLE_FILL_LOG + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_DATE + " TEXT,"
                + KEY_TARGET_HE_LEVEL + " NUMERIC, "
                + KEY_RATE + " NUMERIC, "
                + KEY_HE_LEVEL_VALUES + " TEXT, "
                + KEY_TIME_VALUES + " TEXT, "
                + KEY_OPERATOR + " TEXT, "
                + KEY_MAGNET + " TEXT, "
                + KEY_COMMENTS + " TEXT"
                + ")")
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
        contentValues.put(KEY_DATE, fillLogEntry.dateAsString)
        contentValues.put(KEY_TARGET_HE_LEVEL, fillLogEntry.targetHeLevel)
        contentValues.put(KEY_TIME_VALUES, fillLogEntry.timeValuesAsString)
        contentValues.put(KEY_HE_LEVEL_VALUES, fillLogEntry.heLevelValuesAsString)
        contentValues.put(KEY_RATE, fillLogEntry.averageRate)
        contentValues.put(KEY_MAGNET, fillLogEntry.magnetType)
        contentValues.put(KEY_OPERATOR, fillLogEntry.operator)
        contentValues.put(KEY_COMMENTS, fillLogEntry.comments)

        // Inserting he fill log details using insert query.
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
        var date: String
        var target_he_level: Double
        var average_rate: Double
        var time_values_as_string: String
        var he_level_values_as_string: String
        var operator: String
        var magnet: String
        var comments: String

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex(KEY_ID))
                date = cursor.getString(cursor.getColumnIndex(KEY_DATE))
                target_he_level = cursor.getDouble(cursor.getColumnIndex(KEY_TARGET_HE_LEVEL))
                average_rate = cursor.getDouble(cursor.getColumnIndex(KEY_RATE))
                time_values_as_string = cursor.getString(cursor.getColumnIndex(KEY_TIME_VALUES))
                he_level_values_as_string = cursor.getString(cursor.getColumnIndex(KEY_HE_LEVEL_VALUES))
                operator = cursor.getString(cursor.getColumnIndex(KEY_OPERATOR))
                magnet = cursor.getString(cursor.getColumnIndex(KEY_MAGNET))
                comments = cursor.getString(cursor.getColumnIndex(KEY_COMMENTS))


                val record = FillLogDataClass(id = id,
                    dateAsString = date,
                    targetHeLevel = target_he_level,
                    timeValuesAsString = time_values_as_string,
                    heLevelValuesAsString = he_level_values_as_string,
                    magnetType = magnet,
                    averageRate = average_rate,
                    operator = operator,
                    comments = comments)
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