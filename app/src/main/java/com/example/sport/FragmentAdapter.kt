package com.example.sport

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.example.sport.fragment.MapFragment
import com.example.sport.fragment.StatusFragment


class FragmentAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    override fun getItem(p0: Int): Fragment = when (p0) {
        0 -> MapFragment()
        else -> StatusFragment()
    }


    override fun getCount(): Int = 2
}