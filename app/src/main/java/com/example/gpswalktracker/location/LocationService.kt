package com.example.gpswalktracker.location

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.gpswalktracker.MainActivity
import com.example.gpswalktracker.R
import com.google.android.gms.location.*
import org.osmdroid.util.GeoPoint

class LocationService : Service() {

    private var fusedLocationProvider: FusedLocationProviderClient? = null
    private var lastLocation: Location? = null
    private var distance = 0.0f

    private val locationRequest: LocationRequest =
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
            .setWaitForAccurateLocation(true)
            .setMinUpdateIntervalMillis(3000L)
            .setMaxUpdateDelayMillis(6000L)
            .build()

    private var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val geoPointList = ArrayList<GeoPoint>()
            val currentLocation = locationResult.lastLocation
            if (lastLocation != null && currentLocation != null) {
                geoPointList.add(GeoPoint(currentLocation.latitude, currentLocation.longitude))
                //       if (currentLocation.speed > 0.3) {
                distance += lastLocation?.distanceTo(currentLocation)!!
                //        }
                val locationModel = LocationModel(
                    speed = currentLocation.speed,
                    distance = distance,
                    geoPointsList = geoPointList
                )
                val intent = Intent(LOC_MODEL_INTENT).apply {
                    putExtra(LOC_MODEL_INTENT, locationModel)
                }
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
            }
            lastLocation = currentLocation
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        notification()
        fusedLocationProvider?.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )
        isRunning = true
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(baseContext)
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        fusedLocationProvider?.removeLocationUpdates(locationCallback)
    }

    private fun notification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                LOCATION_SERVICE_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            val nManager =
                getSystemService(NotificationManager::class.java) as NotificationManager
            nManager.createNotificationChannel(notificationChannel)
        }
        val intent = Intent(this, MainActivity::class.java)
        val pIntent = PendingIntent.getActivity(
            this,
            1,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(
            this,
            CHANNEL_ID
        ).setSmallIcon(R.drawable.baseline_my_location)
            .setContentTitle("Tracker Running")
            .setPriority(NotificationManager.IMPORTANCE_MAX)
            .setContentIntent(pIntent)
            .build()
        startForeground(99, notification)
    }

    companion object {
        const val CHANNEL_ID = "CHANNEL_ID"
        const val LOC_MODEL_INTENT = "LOC_MODEL_INTENT"
        const val LOCATION_SERVICE_NAME = "Location service"
        var isRunning = false
        var startTime = 0L
    }

}