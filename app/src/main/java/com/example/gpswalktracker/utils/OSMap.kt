package com.example.gpswalktracker.utils

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.example.gpswalktracker.R
import com.example.gpswalktracker.databinding.FragmentHomeBinding
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

object OSMap {

     fun initOSM(
         context: Context,
         binding: FragmentHomeBinding,
     ) = with(binding) {
        map.controller.setZoom(18.0)
        val mLocProvider = GpsMyLocationProvider(context)
        val mLocOverlay = MyLocationNewOverlay(mLocProvider, map)
        mLocOverlay.enableMyLocation()
        mLocOverlay.enableFollowLocation()
        mLocOverlay.runOnFirstFix {
            map.overlays.clear()
            map.overlays.add(mLocOverlay)
        }
    }

     fun settingsMap(context: Context,activity:AppCompatActivity) {
        Configuration.getInstance().load(
            context,
            activity.getSharedPreferences(context.getString(R.string.osm_pref_key), Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
    }

}