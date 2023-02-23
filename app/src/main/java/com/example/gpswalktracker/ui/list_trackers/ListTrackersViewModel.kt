package com.example.gpswalktracker.ui.list_trackers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.gpswalktracker.data.InfoTrackItem
import com.example.gpswalktracker.data.TracksDataBase
import kotlinx.coroutines.launch

class ListTrackersViewModel(dataBase: TracksDataBase) : ViewModel() {

    val trackDao = dataBase.trackDao()
    val listData = trackDao.getList().asLiveData()
    fun deleteInfoTrack(infoTrackItem: InfoTrackItem) = viewModelScope.launch {
        trackDao.deleteItem(infoTrackItem.id)
    }
}