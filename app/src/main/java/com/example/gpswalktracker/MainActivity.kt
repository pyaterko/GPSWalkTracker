package com.example.gpswalktracker

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.gpswalktracker.databinding.ActivityMainBinding
import com.example.gpswalktracker.ui.home.HomeFragment
import com.example.gpswalktracker.ui.list_trackers.ListTrackersFragment
import com.example.gpswalktracker.ui.settings.SettingsFragment
import com.example.gpswalktracker.ui.utils.launchFragment

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    @SuppressLint("CommitTransaction")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        if (savedInstanceState == null) {
            launchFragment(HomeFragment.newInstance())
        }

        binding.navView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    launchFragment(HomeFragment.newInstance())
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