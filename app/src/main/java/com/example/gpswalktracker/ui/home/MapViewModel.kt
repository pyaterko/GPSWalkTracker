package com.example.gpswalktracker.ui.home

import androidx.lifecycle.*
import com.example.gpswalktracker.data.InfoTrackItem
import com.example.gpswalktracker.data.TracksDataBase
import com.example.gpswalktracker.location.LocationModel
import kotlinx.coroutines.launch

class MapViewModel(dataBase: TracksDataBase) : ViewModel() {

    private val trackDao = dataBase.trackDao()
    val locationUpdates = MutableLiveData<LocationModel>()
    fun addInfoTrack(infoTrackItem: InfoTrackItem)=viewModelScope.launch {
        trackDao.addItem(infoTrackItem)
    }

}