package com.example.sport

import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback, SettingFragment.ChangeSetting {
    companion object {
        val REQ_PERMISSION = 0
        var FLAG_CAMERAFOLLOW = false
        var lastClickTime = 0L
    }

    override fun onMapReady(p0: GoogleMap?) {
        if (!checkGPSPermission(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) finish()

        p0?.isMyLocationEnabled = true
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        p0?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 18F))
        cameraFollowUser(5000, location.latitude, location.longitude, p0)
    }

    fun cameraFollowUser(timeout: Long, lat: Double, long: Double, map: GoogleMap?) {
            val task = object : TimerTask() {
                override fun run() {
                    if (FLAG_CAMERAFOLLOW) {
                        runOnUiThread() {
                            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, long), 18F))
                            Log.d("cameraFollowing", "$FLAG_CAMERAFOLLOW")
                        }
                    }
                }
            }
            val timer = Timer()
            timer.schedule(task, 0, timeout)
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
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
//        return super.onOptionsItemSelected(item)
        val settingFragment = SettingFragment.newInstance(FLAG_CAMERAFOLLOW)
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        when (item?.itemId) {
            R.id.setting -> {
                Log.d("add?", "${settingFragment.isAdded}")
                if (SystemClock.elapsedRealtime() - lastClickTime > 1000) {
                    lastClickTime = SystemClock.elapsedRealtime()
                    fragmentTransaction.hide(map)
                    //why isAdded forever show false
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

    private fun checkGPSPermission(permission1: String, permission2: String): Boolean {
        return (ActivityCompat.checkSelfPermission(
            this,
            permission1
        ) == PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(
            this,
            permission2
        ) == PackageManager.PERMISSION_GRANTED)
    }
}
