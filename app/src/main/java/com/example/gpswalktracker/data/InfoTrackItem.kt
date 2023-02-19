package com.example.gpswalktracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "info_track_table")
data class InfoTrackItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val time:String,
    val date:String,
    val distance:String,
    val speed:String,
    val geoPoints:String
)


