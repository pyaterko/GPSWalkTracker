package com.example.gpswalktracker

import android.app.Application
import com.example.gpswalktracker.data.TracksDataBase

class App : Application() {
    val dataBase by lazy {
        TracksDataBase.getInstance(this)
    }
}