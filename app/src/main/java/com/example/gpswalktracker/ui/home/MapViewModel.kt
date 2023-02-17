package com.example.gpswalktracker.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.gpswalktracker.location.LocationModel

class MapViewModel : ViewModel() {

    val locationUpdates = MutableLiveData<LocationModel>()


}