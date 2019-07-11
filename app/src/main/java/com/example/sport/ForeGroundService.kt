package com.example.sport

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.widget.Toast
import okhttp3.*
import java.io.IOException
import java.lang.Exception
import java.util.*
import kotlin.concurrent.timerTask

class ForeGroundService : Service() {
    companion object {
        val CHANNEL_ID = "hey"
    }

    override fun onBind(intent: Intent?): IBinder? {
//        throw UnsupportedOperationException()
        return null
    }

    override fun onCreate() {
        super.onCreate()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "test", NotificationManager.IMPORTANCE_DEFAULT)
            val builder = Notification.Builder(this, CHANNEL_ID)
            val notification = builder.build()
            startForeground(1, notification)
        } else {
            val builder = NotificationCompat.Builder(this)
            val notification = builder.build()
//            startForeground(1, notification)
        }
        backgroundRequest()
        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("MissingPermission")
    fun backgroundRequest() {
        var log = 0
        val timerTask = object : TimerTask() {
            override fun run() {
                val serviceLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val serviceLocation = serviceLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                //request test
                val param = FormBody.Builder()
                Log.d("LatLng", serviceLocation.latitude.toString() + "//////" + serviceLocation.longitude.toString())
                try {
                    param.add("lat", serviceLocation.latitude.toString())
                    param.add("long", serviceLocation.longitude.toString())
                } catch (e: Exception) {
                    Log.d("Try", "Fail")
                }
                val req = Request.Builder()
                    .url("https://putsreq.com/RDfqKg5Kbfal0w955CpL")
                    .post(param.build())
                    .build()
                OkHttpClient().newCall(req).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
//                runOnUiThread { Toast.makeText(this@MainActivity, "Fail", Toast.LENGTH_SHORT).show() }
                    }

                    override fun onResponse(call: Call, response: Response) {
//                runOnUiThread { Toast.makeText(this@MainActivity, "Success", Toast.LENGTH_SHORT).show() }
                        log++
                        Log.d("log", log.toString())
                    }

                })
                //request test
            }
        }
        val timer = Timer()
        timer.schedule(timerTask, 0L, 5000)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("onDestroy", "Service Destroy")
    }
}