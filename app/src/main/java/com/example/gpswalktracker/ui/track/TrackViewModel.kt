package com.example.gpswalktracker.ui.track

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gpswalktracker.data.InfoTrackItem
import com.example.gpswalktracker.data.TracksDataBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class TrackViewModel(
    dataBase: TracksDataBase,
    itemId: Int,
) : ViewModel() {

    private val trackDao = dataBase.trackDao()
    private var _infoTrackItem = MutableLiveData<InfoTrackItem>()
    val infoTrackItem: LiveData<InfoTrackItem> = _infoTrackItem

    init {
        getTrack(itemId)
    }

    private fun getTrack(itemId: Int) = viewModelScope.launch {
        val item = withContext(Dispatchers.IO) {
            trackDao.getItemById(itemId)
        }
        _infoTrackItem.value = item
    }

}