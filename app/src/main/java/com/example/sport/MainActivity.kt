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
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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
        var FLAG_CAMERAFOLLOW = false
        var lastClickTime = 0L
    }

    lateinit var locationManager: LocationManager
    lateinit var locationListener: LocationListenerImp
    lateinit var settingFragment: SettingFragment
    lateinit var flagSP: SharedPreferences

    inner class LocationListenerImp(val map: GoogleMap?) : LocationListener {
        override fun onLocationChanged(location: Location) {
            Log.d("LocationLisenter", location.latitude.toString() + "////////" + location.longitude.toString())
            Log.d("Flag", FLAG_CAMERAFOLLOW.toString())
            if (FLAG_CAMERAFOLLOW) {
                map?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 18F))
            }
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        }

        override fun onProviderEnabled(provider: String?) {
        }

        override fun onProviderDisabled(provider: String?) {
        }

    }

    override fun onMapReady(p0: GoogleMap?) {
        if (!checkGPSPermission(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) finish()

        locationInit(p0)
        btn_turn.setOnClickListener {
            serviceInit()
            localeToAddress()
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

    fun locationInit(map: GoogleMap?) {
        if (!checkGPSPermission(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) finish()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        val best = locationManager.getBestProvider(Criteria(), true)
        Log.d("BestProvider", best)
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 18F))
        map?.isMyLocationEnabled = true
        locationListener = LocationListenerImp(map)
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 20F, locationListener)
    }

    fun localeToAddress() {
        if (!checkGPSPermission(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) finish()
        val geocoder = Geocoder(this, Locale.getDefault())
        val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        val address = geocoder.getFromLocation(location.latitude, location.longitude, 1)

        AlertDialog.Builder(this)
            .setPositiveButton("Confirm") { dialog, which -> }
            .setTitle(address.get(0).getAddressLine(0))
            .show()
    }

    fun bottomNavigationViewSelected(it: MenuItem): Boolean {
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomsheet)
        when (it.itemId) {
            R.id.run -> {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                Log.d("bottom", "show")
            }
        }
        return true
    }

    fun sharedPreferenceInit(flagSP: SharedPreferences) {
        FLAG_CAMERAFOLLOW = flagSP.getBoolean("flag", false)
        settingFragment = SettingFragment.newInstance(FLAG_CAMERAFOLLOW)
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

        flagSP = getSharedPreferences("flag", Context.MODE_PRIVATE)
        setSupportActionBar(toolbar)
        sharedPreferenceInit(flagSP)
        bottomNavigationView.setOnNavigationItemSelectedListener {
            bottomNavigationViewSelected(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
//        return super.onOptionsItemSelected(item)
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        when (item?.itemId) {
            R.id.setting -> {
                if (SystemClock.elapsedRealtime() - lastClickTime > 1000) {
                    lastClickTime = SystemClock.elapsedRealtime()
                    fragmentTransaction.hide(map)
                    //why isAdded forever show false
                    supportFragmentManager.executePendingTransactions()
                    Log.d("add?", "${settingFragment.isAdded}")
                    if (settingFragment.isAdded) fragmentTransaction.show(settingFragment)
                    else {
                        fragmentTransaction.add(R.id.frame, settingFragment, "setting")
                    }
                    fragmentTransaction.commit()
                }
            }
        }
        return true
    }

    override fun changeSetting(follow: Boolean) {
        FLAG_CAMERAFOLLOW = follow
        flagSP.edit().putBoolean("flag", FLAG_CAMERAFOLLOW).apply()
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

    fun checkGPSPermission(permission1: String, permission2: String): Boolean {
        return (ActivityCompat.checkSelfPermission(
            this,
            permission1
        ) == PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(
            this,
            permission2
        ) == PackageManager.PERMISSION_GRANTED)
    }
}
