@file:Suppress("unused", "CascadeIf")

package com.zoffcc.applications.undereat

import SnackBarToast

fun Toast.Companion.makeText(context: Object?, string: String, duration: Int)
{
    if (duration == Toast.LENGTH_LONG)
    {
        SnackBarToast(string, 1600)
    }
    else if (duration == Toast.LENGTH_SHORT)
    {
        SnackBarToast(string, 1000)
    }
    else
    {
        SnackBarToast(string)
    }
}

fun Unit.show()
{
}


