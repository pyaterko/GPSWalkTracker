package com.example.gpswalktracker.location

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.osmdroid.util.GeoPoint

@Parcelize
data class LocationModel(
    val speed:Float=0.0f,
    val distance:Float=0.0f,
    val geoPointsList:ArrayList<GeoPoint>
):Parcelable
