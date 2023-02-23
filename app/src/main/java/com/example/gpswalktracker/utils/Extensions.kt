package com.example.gpswalktracker.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.gpswalktracker.R
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig

fun Fragment.launchFragment(fragment: Fragment) {
    (activity as AppCompatActivity).supportFragmentManager
        .beginTransaction()
        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
        .replace(R.id.nav_host_fragment_activity_main, fragment)
        .addToBackStack(null)
        .commit()
}

fun AppCompatActivity.launchFragment(fragment: Fragment) {
    if (supportFragmentManager.fragments.isNotEmpty()) {
        if (supportFragmentManager.fragments[0].javaClass == fragment.javaClass) return
    }
    supportFragmentManager
        .beginTransaction()
        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
        .replace(R.id.nav_host_fragment_activity_main, fragment)
        .commit()
}

fun Fragment.checkPermission(permission: String): Boolean {
    return when (PackageManager.PERMISSION_GRANTED) {
        ContextCompat.checkSelfPermission(activity as AppCompatActivity, permission) -> true
        else -> false
    }
}

fun Fragment.settingsMap() {
    Configuration.getInstance().load(
        context,
        activity?.getSharedPreferences(
            getString(R.string.osm_pref_key),
            Context.MODE_PRIVATE
        )
    )
    Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
}