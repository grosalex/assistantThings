package com.grosalex.androidassistantthings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextClock
import android.widget.TextView

import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.grosalex.data.taskList
import com.squareup.picasso.Picasso

import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.net.ServerSocket
import java.net.UnknownHostException

import java.util.ArrayList

class MainActivity : Activity(), View.OnClickListener {


    private var title: TextView? = null
    private var weather: TextView? = null
    private var temperature: TextView? = null
    private var hourlyWeatherArrayList: ArrayList<HourlyWeather>? = null
    private var hourlyRecycler: RecyclerView? = null
    private var mAdapter: HourlyAdapter? = null
    private var mLayoutManager: LinearLayoutManager? = null
    private var icon: ImageView? = null
    private var mHandler: Handler? = null
    private var textClock: TextClock? = null
    private var ibHome: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        title = findViewById(R.id.title)
        weather = findViewById(R.id.weather)
        temperature = findViewById(R.id.temperature)
        icon = findViewById(R.id.weather_icon)
        hourlyRecycler = findViewById(R.id.hourly_recycler)
        ibHome = findViewById(R.id.ib_home)

        hourlyRecycler?.setHasFixedSize(true)

        textClock = findViewById(R.id.textClock)
        textClock?.format24Hour = "E d MMM HH:mm"
        textClock?.format12Hour = null

        // use a linear layout manager
        mLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        hourlyRecycler?.layoutManager = mLayoutManager

        // specify an adapter (see also next example)
        hourlyWeatherArrayList = ArrayList<HourlyWeather>()
        mHandler = Handler()

        mAdapter = HourlyAdapter(this, hourlyWeatherArrayList as ArrayList<HourlyWeather>)
        hourlyRecycler?.adapter = mAdapter
        fetchTodaysWeather()
        fetchNextDaysWeather()
        update()

        (ibHome as ImageButton).setOnClickListener(this)

        var t = Thread(Runnable {
            try {
                var end = false;
                var askListServerSocket = ServerSocket(12345);
                while (!end) {
                    //Server is waiting for client here, if needed
                    var s = askListServerSocket.accept();

                    // var input = BufferedReader( InputStreamReader(s.getInputStream()));
                    //var st = input.readLine();
                    val out = s.getOutputStream()
                    var outToServer = DataOutputStream(s.getOutputStream());
                    outToServer.writeBytes( taskList(this) + '\n');

                    Log.d("TcpExample", "Send to client: ");
                    s.close();
                }
                askListServerSocket.close();


            } catch (e: UnknownHostException) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        })
        t.start()
    }

    private fun update() {
        mHandler?.postDelayed({
            hourlyWeatherArrayList?.clear()
            fetchTodaysWeather()
            fetchNextDaysWeather()
            update()
        }, 3600000)
    }

    private fun fetchNextDaysWeather() {
        val queue = Volley.newRequestQueue(this)
        val url = "http://api.openweathermap.org/data/2.5/forecast?q=Paris,fr&units=metric&lang=fr&APPID=" + getString(R.string.weather_api_key)
        val jsObjRequest = JsonObjectRequest(Request.Method.GET, url, null, Response.Listener<JSONObject> { response ->
            try {
                for (i in 0..response.getJSONArray("list").length() - 1) {
                    hourlyWeatherArrayList?.add(HourlyWeather(response.getJSONArray("list").getJSONObject(i)))
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            mAdapter?.notifyDataSetChanged()
            //response.get
        }, Response.ErrorListener {
            // TODO Auto-generated method stub
        })

        // Access the RequestQueue through your singleton class.
        queue.add(jsObjRequest)
    }

    private fun fetchTodaysWeather() {

        val queue = Volley.newRequestQueue(this)
        val url = "http://api.openweathermap.org/data/2.5/weather?q=Paris,fr&units=metric&lang=fr&APPID=" + getString(R.string.weather_api_key)
        val jsObjRequest = JsonObjectRequest(Request.Method.GET, url, null, Response.Listener<JSONObject> { response ->
            Log.d(TAG, "onResponse: " + response.toString())
            try {
                weather?.text = response.getJSONArray("weather").getJSONObject(0).getString("description")
                temperature?.text = response.getJSONObject("main").getDouble("temp_min").toString() + " C° - " + response.getJSONObject("main").getDouble("temp_max") + " C°"
                Picasso.with(applicationContext).load("http://openweathermap.org/img/w/" + response.getJSONArray("weather").getJSONObject(0).getString("icon") + ".png").into(icon)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            //response.get
        }, Response.ErrorListener {
            // TODO Auto-generated method stub
        })

        // Access the RequestQueue through your singleton class.
        queue.add(jsObjRequest)
    }

    companion object {

        private val TAG = "MainActivity"
    }

    override fun onClick(v: View?) {
        val intent = Intent(this, TaskActivity::class.java)
        startActivity(intent)

    }
}
