package com.example.sport

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.PointF
import android.location.*
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback, SettingFragment.ChangeSetting {
    companion object {
        val REQ_PERMISSION = 0
        val CORSE = android.Manifest.permission.ACCESS_COARSE_LOCATION
        val FINE = android.Manifest.permission.ACCESS_FINE_LOCATION
        var lastClickTime = 0L
        var cameraFollowFlag = false
        var serviceFlag = true
    }

    lateinit var locationManager: LocationManager
    lateinit var flagSP: SharedPreferences
    lateinit var settingFragment: SettingFragment

    inner class LocationListenerImp(val map: GoogleMap?) : LocationListener {
        override fun onLocationChanged(location: Location) {
            if (cameraFollowFlag) {
                map?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 18F))
                Log.d("camera", "-------------------------------------------------------------")
            }
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        }

        override fun onProviderEnabled(provider: String?) {
        }

        override fun onProviderDisabled(provider: String?) {
        }

    }


    fun serviceInit() {
        val service = Intent(this@MainActivity, ForeGroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(service)
        } else {
            startService(service)
        }
    }

    fun getLocation(): Location {
        if (!checkGPSPermission(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) finish()

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        criteria.isSpeedRequired = true
        locationManager.getBestProvider(criteria, true)
        val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        Log.d("speed", location.speed.toString())
        return location
    }

    fun locationInit(map: GoogleMap?) {
        checkGPSPermission(CORSE, FINE)
        val location = getLocation()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val best = locationManager.getBestProvider(Criteria(), true)
        Log.d("BestProvider", best)
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 18F))
        map?.isMyLocationEnabled = true
        val locationListener = LocationListenerImp(map)
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 20F, locationListener)
    }

    fun localeToAddress() {
        if (!checkGPSPermission(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) finish()
        val geocoder = Geocoder(this, Locale.getDefault())
        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val address = geocoder.getFromLocation(location.latitude, location.longitude, 1)

        AlertDialog.Builder(this)
            .setPositiveButton("Confirm") { dialog, which -> }
            .setTitle(address.get(0).getAddressLine(0))
            .show()
    }

    fun checkGPSPermission(permission1: String, permission2: String): Boolean {
        return (ActivityCompat.checkSelfPermission(
            this,
            permission1
        ) == PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(
            this,
            permission2
        ) == PackageManager.PERMISSION_GRANTED)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (checkGPSPermission(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            val map = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            map.getMapAsync(this)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ), REQ_PERMISSION
            )
        }

        setSupportActionBar(toolbar)
        flagSP = getSharedPreferences("flag", Context.MODE_PRIVATE)
        cameraFollowFlag = flagSP.getBoolean("flag", false)
        settingFragment = SettingFragment.newInstance(cameraFollowFlag)

        btn_start.setOnClickListener {
            startActivity(Intent(this, RunningActivity::class.java))
        }
    }

    override fun onMapReady(p0: GoogleMap?) {
        if (!checkGPSPermission(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) finish()

        locationInit(p0)

        val intent = Intent(this, ForeGroundService::class.java)

        btn_turn.visibility = View.INVISIBLE
        btn_turn.setOnClickListener {
            if (serviceFlag) serviceInit()
            else stopService(intent)
            serviceFlag = !serviceFlag
//            localeToAddress()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
//        return super.onOptionsItemSelected(item)
//        settingFragment = SettingFragment.newInstance(cameraFollowFlag)
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        when (item?.itemId) {
            R.id.setting -> {
                Log.d("add?", "AddorNot: ${settingFragment.isAdded} | Visible: ${settingFragment.isVisible}")
                if (!settingFragment.isVisible) {
                    if (SystemClock.elapsedRealtime() - lastClickTime > 1000) {
                        lastClickTime = SystemClock.elapsedRealtime()
                        fragmentTransaction.hide(map)
                        //why isAdded forever show false
                        if (settingFragment.isAdded) fragmentTransaction.show(settingFragment)
                        else fragmentTransaction.add(R.id.frame, settingFragment, "setting")
                        fragmentTransaction.commit()
                        supportFragmentManager.executePendingTransactions()
                    }
                }
            }
        }
        return true
    }

    override fun changeSetting(follow: Boolean) {
        cameraFollowFlag = follow
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("destroy", " onDestroy")
    }

    override fun onPause() {
        super.onPause()
//        if (locationManager != null) {
//            locationManager.removeUpdates(locationListener)
//        }
        flagSP.edit().putBoolean("flag", cameraFollowFlag).apply()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQ_PERMISSION) {
            if (checkGPSPermission(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                val map = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
                map.getMapAsync(this)
            } else {
                finish()
            }
        }
    }

}
