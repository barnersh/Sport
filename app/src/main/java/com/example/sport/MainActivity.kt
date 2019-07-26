package com.example.sport

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.location.*
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.ActivityCompat
import android.support.v4.widget.NestedScrollView
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottomsheet.*
import java.util.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback, SettingFragment.ChangeSetting {
    companion object {
        val REQ_PERMISSION = 0
        var FLAG_CAMERAFOLLOW = false
        var lastClickTime = 0L
        private var flag_play = false
        private var sec = 0
        private var min = 0
        private var distance = 0F
    }


    lateinit var locationManager: LocationManager
    lateinit var locationListener: LocationListenerImp
    lateinit var settingFragment: SettingFragment
    lateinit var flagSP: SharedPreferences
    lateinit var timer: Timer

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

        img_play.setOnClickListener {
            val startLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val startPoint = LatLng(startLocation.latitude, startLocation.longitude)
            playPressed(p0, startPoint)
        }

        img_stop.setOnClickListener {
            stopPressed()
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
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                Log.d("bottom", "show")
            }
        }
        return true
    }

    fun sharedPreferenceInit(flagSP: SharedPreferences) {
        FLAG_CAMERAFOLLOW = flagSP.getBoolean("flag", false)
        settingFragment = SettingFragment.newInstance(FLAG_CAMERAFOLLOW)
    }

    fun bottomSheetInit(bottomSheetBehavior: BottomSheetBehavior<NestedScrollView>) {
        bottomSheetBehavior.peekHeight = 500
        bottomSheetBehavior.isHideable = true
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

//    fun bottomSheetExpanded() {
//        val coordinatorParams = CoordinatorLayout.LayoutParams(
//            CoordinatorLayout.LayoutParams.MATCH_PARENT,
//            CoordinatorLayout.LayoutParams.MATCH_PARENT
//        )
//        coordinatorParams.dodgeInsetEdges = 80
//        map.view?.layoutParams = coordinatorParams
//    }

    fun buttonShowPlay() {
        val green = ColorDrawable(resources.getColor(R.color.green)) as Drawable
        val play = resources.getDrawable(R.drawable.ic_play_arrow_black_24dp)
        val playWithBackground = LayerDrawable(arrayOf(green, play))
        Glide
            .with(this)
            .load(playWithBackground)
            .apply(RequestOptions.circleCropTransform())
            .into(img_play)
    }

    fun buttonShowPause() {
        val green = ColorDrawable(resources.getColor(R.color.green)) as Drawable
        val pause = resources.getDrawable(R.drawable.ic_pause_black_24dp)
        val pauseWithBackground = LayerDrawable(arrayOf(green, pause))
        Glide
            .with(this)
            .load(pauseWithBackground)
            .apply(RequestOptions.circleCropTransform())
            .into(img_play)
    }

    fun buttonInit() {
        val green = ColorDrawable(resources.getColor(R.color.green)) as Drawable
        val stop = resources.getDrawable(R.drawable.ic_stop_black_24dp)
        val stopWithBackground = LayerDrawable(arrayOf(green, stop))
        buttonShowPlay()
        Glide
            .with(this)
            .load(stopWithBackground)
            .apply(RequestOptions.circleCropTransform())
            .into(img_stop)
    }

    @SuppressLint("MissingPermission")
    fun playPressed(map: GoogleMap?, startPoint: LatLng) {
        flag_play = !flag_play
        var startPoint = startPoint
        if (flag_play) {
            buttonShowPause()
            timer = Timer()
            timer.schedule(object : TimerTask() {
                override fun run() {
                    sec++
                    if (sec == 60) {
                        sec = 0
                        min++
                    }
                    val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    var endPoint = LatLng(location.latitude, location.longitude)
                    val poly = PolylineOptions()
                    poly.color(Color.GREEN)
                    poly.add(startPoint)
                    poly.add(endPoint)
                    distance += getDistance(startPoint, endPoint)
                    runOnUiThread {
                        tv_timer.text = String.format("%02d", min) + ":" + String.format("%02d", sec)
                        tv_distance.text = String.format("%.2f", distance)
                        tv_speed.text = location.speed.toString()
                        map?.addPolyline(poly)
                    }
                    startPoint = endPoint
                }
            }, 0, 1000)
        } else {
            buttonShowPlay()
            timer.cancel()
        }
    }

    fun getDistance(startPoint: LatLng, endPoint: LatLng): Float {
        val result = FloatArray(1)
        Location.distanceBetween(
            startPoint.latitude,
            startPoint.longitude,
            endPoint.latitude,
            endPoint.longitude,
            result
        )
        return result[0]
    }

    fun stopPressed() {
        timer.cancel()
        buttonShowPlay()
        sec = 0
        min = 0
        tv_timer.text = String.format("%02d", min) + ":" + String.format("%02d", sec)
        flag_play = false
    }

    fun init() {
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomsheet)
        setSupportActionBar(toolbar)
        flagSP = getSharedPreferences("flag", Context.MODE_PRIVATE)
        sharedPreferenceInit(flagSP)
        bottomSheetInit(bottomSheetBehavior)
        buttonInit()
        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) {
//                if (p1 > 0) {
//                    bottomSheetExpanded()
//                }
            }

            override fun onStateChanged(p0: View, p1: Int) {
            }

        })
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

        init()

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
