package com.grosalex.thingscompanion

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.grosalex.data.Task
import com.grosalex.data.TaskAdapter

/**
 * Created by grosalex on 9/25/17.
 */
class MobileTaskAdapter(mContext: Context, datas: ArrayList<Task>) : TaskAdapter(mContext, datas){
    init {
        width = ViewGroup.LayoutParams.MATCH_PARENT
    }
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): TaskViewHolder {
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.mobile_task_item, parent, false)
        return TaskViewHolder(v)
    }
}