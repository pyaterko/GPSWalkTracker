package com.example.gpswalktracker.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.gpswalktracker.MainActivity
import com.example.gpswalktracker.R
import com.example.gpswalktracker.databinding.FragmentMapBinding
import com.example.gpswalktracker.location.LocationModel
import com.example.gpswalktracker.location.LocationService
import com.example.gpswalktracker.utils.DialogManager
import com.example.gpswalktracker.utils.TimeUtils
import com.example.gpswalktracker.utils.checkPermission
import com.google.android.gms.location.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.*

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private val mapViewModel: MapViewModel by activityViewModels()
    private lateinit var pLauncher: ActivityResultLauncher<Array<String>>
    private var hasNotificationPermissionGranted = false
    private var isServiceRunning = false
    private var firstStart = true
    private var timer: Timer? = null
    private var startTime = 0L
    private var polyLine: Polyline? = null
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
                        intent.getParcelableExtra(LocationService.LOC_MODEL_INTENT)
                    }
                mapViewModel.locationUpdates.value = locModel
            }
        }

    }


    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            hasNotificationPermissionGranted = isGranted
            if (!isGranted) {
                if (Build.VERSION.SDK_INT >= 33) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                        showNotificationPermissionRationale()
                    } else {
                        showSettingDialog()
                    }
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.notification_permission_granted),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }

    private fun showSettingDialog() {
        MaterialAlertDialogBuilder(
            requireContext(),
            com.google.android.material.R.style.MaterialAlertDialog_Material3
        )
            .setTitle(getString(R.string.notification_permission))
            .setMessage(getString(R.string.notification_permission_is_required))
            .setPositiveButton("Ok") { _, _ ->
                val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse((activity as MainActivity).packageName)
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showNotificationPermissionRationale() {

        MaterialAlertDialogBuilder(
            requireContext(),
            com.google.android.material.R.style.MaterialAlertDialog_Material3
        )
            .setTitle("Alert")
            .setMessage("Notification permission is required, to show notification")
            .setPositiveButton("Ok") { _, _ ->
                if (Build.VERSION.SDK_INT >= 33) {
                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        settingsMap()
        LocalBroadcastManager
            .getInstance(activity as AppCompatActivity)
            .registerReceiver(receiver, IntentFilter(LocationService.LOC_MODEL_INTENT))
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
            binding.fatRun.setImageResource(android.R.drawable.ic_media_pause)
        }
        registerPermissions()
        checkLocationPermission()
        binding.fatRun.setOnClickListener {
            if (!isServiceRunning) {
                startLocationService()
                LocationService.startTime = System.currentTimeMillis()
                startTimer()
            } else {
                stopLocationService()
                timer?.cancel()
            }
            isServiceRunning = !isServiceRunning
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setData(locationModel: LocationModel) {
        binding.tvDistance.text =
            "${getString(R.string.distanse)} ${
                String.format(
                    "%.1f",
                    locationModel.distance
                )
            } ${getString(R.string._m)}"
        binding.tvSpeed.text =
            "${getString(R.string.speed)} ${
                String.format(
                    "%.1f",
                    3.6f * locationModel.speed
                )
            } ${getString(R.string.km_h)}"
        binding.tvAverageSpeed.text =
            "${getString(R.string.average_speed)} ${
                getAverageSpeed(locationModel.distance)
            } ${getString(R.string.km_h)}"
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
                    binding.tvTime.text =
                        "Time " + TimeUtils.getTime(System.currentTimeMillis() - startTime)
                }
            }

        }, 1000, 1000)
    }

    private fun stopLocationService() {
        binding.fatRun.setImageResource(android.R.drawable.ic_media_play)
        activity?.stopService(Intent(requireContext(), LocationService::class.java))
    }

    private fun startLocationService() {
        if (Build.VERSION.SDK_INT >= 33) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            hasNotificationPermissionGranted = true
        }
        if (checkPermission(Manifest.permission.POST_NOTIFICATIONS)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                binding.fatRun.setImageResource(android.R.drawable.ic_media_pause)
                activity?.startForegroundService(
                    Intent(
                        requireContext(),
                        LocationService::class.java
                    )
                )
            } else {
                binding.fatRun.setImageResource(android.R.drawable.ic_media_pause)
                activity?.startService(
                    Intent(
                        requireContext(),
                        LocationService::class.java
                    )
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            initOSM()

        }
    }

    private fun checkLocationPermission() {
        if (!checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                DialogManager.showDialogLocationPermission(requireContext()) {
                    requestLocationPermission()
                }
            } else {
                requestLocationPermission()
            }
        } else {
            checkBackgroundLocation()
        }
    }

    private fun checkBackgroundLocation() {
        if (!checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            requestBackgroundLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
            ),
            MY_PERMISSIONS_REQUEST_LOCATION
        )
    }

    private fun requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION
            )
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_LOCATION
            )
        }
    }

    private fun registerPermissions() {
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            if (it.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
                initOSM()
                checkEnabledGPS()
            } else {
                checkLocationPermission()
            }
        }
        checkLocPermissionsBeforeInitMap()
    }

    private fun checkLocPermissionsBeforeInitMap() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            checkPermissionAfterQ()
        } else {
            checkPermissionBeforeQ()
        }
    }

    private fun checkPermissionBeforeQ() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            initOSM()
            checkEnabledGPS()
        } else {
            pLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkPermissionAfterQ() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        ) {
            initOSM()
            checkEnabledGPS()
        } else {
            pLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                )
            )
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

    private fun initOSM(
    ) = with(binding) {
        polyLine = Polyline()
        polyLine?.outlinePaint?.color = Color.BLUE
        map.controller.setZoom(18.0)
        val mLocProvider = GpsMyLocationProvider(requireContext())
        val mLocOverlay = MyLocationNewOverlay(mLocProvider, map)
        mLocOverlay.enableMyLocation()
        mLocOverlay.enableFollowLocation()
        mLocOverlay.runOnFirstFix {
            map.overlays.clear()
            map.overlays.add(mLocOverlay)
            map.overlays.add(polyLine)
        }
    }

    private fun settingsMap() {
        Configuration.getInstance().load(
            context,
            activity?.getSharedPreferences(
                getString(R.string.osm_pref_key),
                Context.MODE_PRIVATE
            )
        )
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
    }

    private fun addPoints(list: List<GeoPoint>) {
        polyLine?.addPoint(list[list.size - 1])
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        LocalBroadcastManager
            .getInstance(activity as AppCompatActivity)
            .unregisterReceiver(receiver)
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_LOCATION = 99
        private const val MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION = 66

        @JvmStatic
        fun newInstance() = MapFragment()
    }
}