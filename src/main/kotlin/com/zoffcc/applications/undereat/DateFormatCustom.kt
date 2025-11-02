package com.zoffcc.applications.undereat

import java.text.SimpleDateFormat

class DateFormat2
{
    //private fun DateFormat2.Companion.format(string: String, addedTimestamp: Long)
    //{
    //}

    companion object
    {
        fun Companion.format(string: String, timestamp: Long): String
        {
            return SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(timestamp)
        }
    }
}