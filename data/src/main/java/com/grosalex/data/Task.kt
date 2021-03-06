package com.grosalex.data

import android.util.Log
import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Created by grosalex on 9/25/17.
 */


/**
 * Created by grosalex on 10/09/17.
 */
class Task (@SerializedName("title") var title : String, @SerializedName("last_time_done") var lastTimeDone : Date, @SerializedName("recurrence") var recurrence: Long, @SerializedName("doer") var doer : Boolean){
    @SerializedName("dueDate")
    var dueDate : Date = Date()
    public val TASK_PREFS:String = "TASK_LIST"

    fun hasBeenDone() {
        doer = ! doer
        lastTimeDone = Date()
        dueDate = Date(lastTimeDone.time + recurrence * 1000L * 60L * 60L * 24L)
        Log.d("HASBEENDONE",dueDate.toString())
    }

    fun hasToBeDone() :Boolean  {
        return Date() >= dueDate
        }

    fun swap() {
        doer = !doer
    }

}