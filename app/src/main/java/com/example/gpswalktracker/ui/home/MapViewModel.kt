package com.example.gpswalktracker.ui.home

import androidx.lifecycle.*
import com.example.gpswalktracker.data.InfoTrackItem
import com.example.gpswalktracker.data.TracksDataBase
import com.example.gpswalktracker.location.LocationModel
import kotlinx.coroutines.launch

class MapViewModel(dataBase: TracksDataBase) : ViewModel() {

    val trackDao = dataBase.trackDao()
    val locationUpdates = MutableLiveData<LocationModel>()
    val tracks = trackDao.getList().asLiveData()
    fun addInfoTrack(infoTrackItem: InfoTrackItem)=viewModelScope.launch {
        trackDao.addItem(infoTrackItem)
    }

}