package com.example.gpswalktracker

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.gpswalktracker.databinding.ActivityMainBinding
import com.example.gpswalktracker.ui.home.MapFragment
import com.example.gpswalktracker.ui.list_trackers.ListTrackersFragment
import com.example.gpswalktracker.ui.settings.SettingsFragment
import com.example.gpswalktracker.utils.launchFragment
import com.google.android.gms.location.*

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    @SuppressLint("CommitTransaction")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        if (savedInstanceState == null) {
            launchFragment(MapFragment.newInstance())
        }

        binding.navView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    launchFragment(MapFragment.newInstance())
                }
                R.id.list_trackers -> {
                    launchFragment(ListTrackersFragment.newInstance())
                }
                R.id.settings -> {
                    launchFragment(SettingsFragment())
                }
            }
            return@setOnItemSelectedListener true
        }
    }

}