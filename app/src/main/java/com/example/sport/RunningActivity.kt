package com.example.sport

import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.sport.fragment.MapFragment
import com.example.sport.fragment.StatusFragment
import kotlinx.android.synthetic.main.activity_running.*

class RunningActivity : AppCompatActivity(), MapFragment.OnFragmentInteractionListener,
    StatusFragment.OnFragmentInteractionListener {
    override fun onFragmentInteraction(uri: Uri) {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_running)

        viewpagerInit()
    }

    fun viewpagerInit() {
        val fragmentAdapter = FragmentAdapter(supportFragmentManager)
        viewpager.adapter = fragmentAdapter
    }
}
