package com.example.gpswalktracker.ui.utils

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.gpswalktracker.R

fun Fragment.launchFragment(fragment: Fragment){
    (activity as AppCompatActivity).supportFragmentManager
        .beginTransaction()
        .setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out)
        .replace(R.id.nav_host_fragment_activity_main,fragment)
        .commit()
}

fun AppCompatActivity.launchFragment(fragment: Fragment){
    supportFragmentManager
        .beginTransaction()
        .setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out)
        .replace(R.id.nav_host_fragment_activity_main,fragment)
        .commit()
}