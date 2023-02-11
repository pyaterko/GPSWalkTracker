package com.example.gpswalktracker

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.gpswalktracker.databinding.ActivityMainBinding
import com.example.gpswalktracker.ui.dashboard.DashboardFragment
import com.example.gpswalktracker.ui.home.HomeFragment
import com.example.gpswalktracker.ui.notifications.SettingsFragment

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    @SuppressLint("CommitTransaction")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.nav_host_fragment_activity_main,HomeFragment())
            .commit()

        binding.navView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.navigation_home->{
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.nav_host_fragment_activity_main,HomeFragment())
                        .commit()
                }
                R.id.list_trackers->{
                    supportFragmentManager
                        .beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.nav_host_fragment_activity_main,DashboardFragment())
                        .commit()
                }
                R.id.settings->{
                    supportFragmentManager
                        .beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.nav_host_fragment_activity_main,SettingsFragment())
                        .commit()
                }
            }
            return@setOnItemSelectedListener true
        }


    }
}