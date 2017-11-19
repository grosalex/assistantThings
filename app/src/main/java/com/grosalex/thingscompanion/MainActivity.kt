package com.grosalex.thingscompanion

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.grosalex.data.Task
import com.grosalex.data.TaskAdapter
import kotlinx.android.synthetic.main.activity_main.*

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.Socket
import java.net.UnknownHostException


class MainActivity : AppCompatActivity() {
    private var taskArrayList: ArrayList<Task> = ArrayList<Task>()
    private var layoutManager: LinearLayoutManager? = null

    lateinit var taskAdapter: MobileTaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        recyclerview.layoutManager = layoutManager
        taskAdapter = MobileTaskAdapter(this, taskArrayList)
        recyclerview.adapter = taskAdapter

        var t = Thread(loadListRunnable)
        t.start()


    }


    var loadListRunnable = Runnable {
        try {
            val s = Socket("192.168.0.34", 12345)

            var input = s.getInputStream()

            var sinput = BufferedReader(InputStreamReader(input))
            var values = sinput.readText()

            loadTasks(values)
            runOnUiThread {
                taskAdapter.datas = taskArrayList

                taskAdapter.notifyDataSetChanged();
            }

            s.close()


        } catch (e: UnknownHostException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }

    private fun loadTasks(s: String) {
        var gson = Gson()
        val turnsType = object : TypeToken<ArrayList<com.grosalex.data.Task>>() {}.type

        if (s.isNullOrEmpty()) return
        taskArrayList = gson.fromJson(s, turnsType)

        /*       taskAdapter?.datas= taskArrayList as ArrayList<Task>
               taskAdapter?.notifyDataSetChanged()*/

    }

}
