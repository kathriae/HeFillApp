package com.example.hefillapp.com.example.hefillapp

/** Class to contain data for entry in fill log
 * ID, date as string, strings with arrays of time and He levels
 * magnet type, operator, target level and average rate as Long, comments
 */

class FillLogDataClass(val id: Int, val dateAsString: String, val targetHeLevel: Long,
                       val timeValuesAsString: String, val heLevelValuesAsString: String,
                       val magnetType: String, val operator: String, val averageRate: Long,
                       var comments: String)