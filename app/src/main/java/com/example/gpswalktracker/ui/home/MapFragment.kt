package com.example.gpswalktracker.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.example.gpswalktracker.R
import com.example.gpswalktracker.data.InfoTrackItem
import com.example.gpswalktracker.databinding.FragmentMapBinding
import com.example.gpswalktracker.location.LocationModel
import com.example.gpswalktracker.location.LocationService
import com.example.gpswalktracker.utils.*
import com.google.android.gms.location.*
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.*


class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private val mapViewModel: MapViewModel by viewModelCreator { MapViewModel(it.dataBase) }

    private lateinit var mLocOverlay: MyLocationNewOverlay
    private var locationModel: LocationModel? = null
    private var timer: Timer? = null
    private var polyLine: Polyline? = null
    private var hasNotificationPermissionGranted = false
    private var isServiceRunning = false
    private var firstStart = true
    private var startTime = 0L

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isGranted ->
            if (isGranted.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
                initOSM()
                checkEnabledGPS()
                requestBackgroundLocationPermission()
            }
        }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(contex: Context?, intent: Intent?) {
            if (intent?.action == LocationService.LOC_MODEL_INTENT) {
                val locModel =
                    if (Build.VERSION.SDK_INT >= 33) {
                        intent.getParcelableExtra(
                            LocationService.LOC_MODEL_INTENT,
                            LocationModel::class.java
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(LocationService.LOC_MODEL_INTENT)
                    }
                mapViewModel.locationUpdates.value = locModel
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        settingsMap()
        registerBroadcastReceiver()
        mapViewModel.locationUpdates.observe(viewLifecycleOwner) {
            setData(it)
            updatePolyline(it.geoPointsList)
        }
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isServiceRunning = LocationService.isRunning
        if (isServiceRunning) {
            binding.fatRun.setImageResource(R.drawable.baseline_stop)
        }
        requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        binding.fatRun.setOnClickListener {
            toGeolocationManagement()
        }
        binding.fatLocation.setOnClickListener {
            toStartPosition()
        }
    }

    override fun onResume() {
        super.onResume()
        firstStart = true
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            initOSM()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        LocalBroadcastManager
            .getInstance(activity as AppCompatActivity)
            .unregisterReceiver(receiver)
        locationModel = null
        polyLine = null
    }

    private fun registerBroadcastReceiver() {
        LocalBroadcastManager
            .getInstance(activity as AppCompatActivity)
            .registerReceiver(receiver, IntentFilter(LocationService.LOC_MODEL_INTENT))
    }

    private fun toStartPosition() {
        binding.map.controller.animateTo(mLocOverlay.myLocation)
        mLocOverlay.enableFollowLocation()
    }

    private fun toGeolocationManagement() {
        if (!isServiceRunning) {
            startLocationService()
            LocationService.startTime = System.currentTimeMillis()
            startTimer()
        } else {
            stopLocationService()
            timer?.cancel()
            val trackItem = getInfoTrackItem()
            DialogManager.showDialogForSavingTheTrack(
                requireContext(),
                trackItem
            ) {
                mapViewModel.addInfoTrack(trackItem)
            }
        }
        if (hasNotificationPermissionGranted) {
            isServiceRunning = !isServiceRunning
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setData(locationModel: LocationModel) {
        val distance = "${getString(R.string.distanse)} ${
            String.format(
                "%.1f",
                locationModel.distance
            )
        } ${getString(R.string._m)}"
        val speed = "${getString(R.string.speed)} ${
            String.format(
                "%.1f",
                3.6f * locationModel.speed
            )
        } ${getString(R.string.km_h)}"
        this.locationModel = locationModel
        binding.tvDistance.text = distance
        binding.tvSpeed.text = speed

        binding.tvAverageSpeed.text =
            "${getString(R.string.average_speed)} ${
                getAverageSpeed(locationModel.distance)
            } ${getString(R.string.km_h)}"
    }

    private fun getGeoPointsToString(list: List<GeoPoint>): String {
        val stringBuilder = java.lang.StringBuilder()
        list.forEach {
            stringBuilder.append("${it.latitude} ${it.longitude}/")
        }
        return stringBuilder.toString()
    }

    private fun getInfoTrackItem(): InfoTrackItem {
        val distance = "${getString(R.string.distanse)} ${
            String.format(
                "%.1f",
                locationModel?.distance
            )
        } ${getString(R.string._m)}"
        val speed = "${getString(R.string.speed)} ${
            String.format(
                "%.1f",
                3.6f * (locationModel?.speed ?: 0f)
            )
        } ${getString(R.string.km_h)}"
        return InfoTrackItem(
            id = UNDEFINED_ID,
            time = getCurrentTime(),
            date = DateUtils.getDate(),
            distance = distance,
            speed = speed,
            geoPoints = getGeoPointsToString(locationModel?.geoPointsList ?: emptyList())
        )
    }

    private fun getAverageSpeed(distance: Float): String {
        return String.format(
            "%.1f",
            3.6f * (distance / ((System.currentTimeMillis() - startTime) / 1000f))
        )
    }

    private fun startTimer() {
        timer?.cancel()
        timer = Timer()
        startTime = LocationService.startTime
        timer?.schedule(object : TimerTask() {
            @SuppressLint("SetTextI18n")
            override fun run() {
                activity?.runOnUiThread {
                    binding.tvTime.text = getCurrentTime()

                }
            }
        }, 1000, 1000)
    }

    private fun getCurrentTime() =
        getString(R.string.time) + DateUtils.getTime(System.currentTimeMillis() - startTime)

    private fun stopLocationService() {
        binding.fatRun.setImageResource(R.drawable.baseline_play)
        activity?.stopService(Intent(requireContext(), LocationService::class.java))
    }

    private fun startLocationService() {
        if (Build.VERSION.SDK_INT >= 33) {
            requestNotificationPermission()
        } else {
            hasNotificationPermissionGranted = true
        }
        if (checkPermission(Manifest.permission.POST_NOTIFICATIONS)) {
            hasNotificationPermissionGranted = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                binding.fatRun.setImageResource(R.drawable.baseline_stop)
                activity?.startForegroundService(
                    Intent(
                        requireContext(),
                        LocationService::class.java
                    )
                )
            } else {
                binding.fatRun.setImageResource(R.drawable.baseline_stop)
                activity?.startService(
                    Intent(
                        requireContext(),
                        LocationService::class.java
                    )
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
            DialogManager.showNotificationPermissionRationale(requireContext()) {
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
            }
        } else {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
        }
    }

    private fun requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                DialogManager.showDialogLocationPermission(requireContext()) {
                    requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
                }
            }
        }
    }

    private fun checkEnabledGPS() {
        val locManager =
            activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isEnabled =
            locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!isEnabled) {
            DialogManager.showGPSEnabledDialog(requireContext()) {
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        }
    }

    private fun initOSM() = with(binding) {
        polyLine = Polyline()
        polyLine?.outlinePaint?.color = Color.parseColor(
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getString(getString(R.string.track_color_key), getString(R.string.default_color))
        )
        map.controller.setZoom(18.0)
        val mLocProvider = GpsMyLocationProvider(requireContext())
        mLocOverlay = MyLocationNewOverlay(mLocProvider, map)
        mLocOverlay.enableMyLocation()
        mLocOverlay.enableFollowLocation()
        mLocOverlay.runOnFirstFix {
            map.overlays.clear()
            map.overlays.add(polyLine)
            map.overlays.add(mLocOverlay)
        }
    }

    private fun addPoints(list: List<GeoPoint>) {
        if (list.isNotEmpty()) {
            polyLine?.addPoint(list[list.size - 1])
        }
    }

    private fun fillPointsAfterPause(list: List<GeoPoint>) {
        list.forEach {
            polyLine?.addPoint(it)
        }
    }

    private fun updatePolyline(list: List<GeoPoint>) {
        if (list.size > 1 && firstStart) {
            fillPointsAfterPause(list)
            firstStart = false
        } else {
            addPoints(list)
        }
    }

    companion object {
        private const val UNDEFINED_ID = 0

        @JvmStatic
        fun newInstance() = MapFragment()
    }
}