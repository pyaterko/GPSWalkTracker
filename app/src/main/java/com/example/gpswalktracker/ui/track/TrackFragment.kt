package com.example.gpswalktracker.ui.track

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.example.gpswalktracker.R.drawable
import com.example.gpswalktracker.R.string
import com.example.gpswalktracker.databinding.FragmentTrackBinding
import com.example.gpswalktracker.utils.settingsMap
import com.example.gpswalktracker.utils.viewModelCreator
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

class TrackFragment : Fragment() {

    private var _binding: FragmentTrackBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TrackViewModel by viewModelCreator {
        TrackViewModel(
            it.dataBase,
            arguments?.getInt(ARG_ITEM_ID, 0) ?: 0
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        settingsMap()
        _binding = FragmentTrackBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.infoTrackItem.observe(viewLifecycleOwner) {
            with(binding) {
                tvDate.text = it.date
                tvTime.text = it.time
                tvAverageSpeed.text = it.speed
                tvDistance.text = it.distance
                val polyline = getPolyLine(it.geoPoints)
                polyline.outlinePaint.color = Color.parseColor(
                    PreferenceManager.getDefaultSharedPreferences(requireContext())
                        .getString(
                            getString(string.track_color_key),
                            getString(string.default_color)
                        )
                )
                map.overlays.add(polyline)
                setMarkers(polyline.actualPoints)
                goToStartPosition(polyline.actualPoints[0])
                fatLocation.setOnClickListener {
                    binding.map.controller.animateTo(polyline.actualPoints[0])
                }
            }
        }
    }

    private fun goToStartPosition(startPosition: GeoPoint) {
        binding.map.controller.zoomTo(18.0)
        binding.map.controller.animateTo(startPosition)
    }

    private fun setMarkers(list: List<GeoPoint>) = with(binding) {
        val startMarker = Marker(map)
        val endMarker = Marker(map)
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        startMarker.icon = getDrawable(requireContext(), drawable.baseline_location_on)
        endMarker.icon = getDrawable(requireContext(), drawable.baseline_location_end)
        startMarker.position = list[0]
        endMarker.position = list[list.size - 1]
        map.overlays.add(startMarker)
        map.overlays.add(endMarker)
    }

    private fun getPolyLine(geoPoint: String): Polyline {
        val polyline = Polyline()
        val list = geoPoint.split("/")
        list.forEach {
            if (it.isEmpty()) return@forEach
            val points = it.split(" ")

            polyline.addPoint(GeoPoint(points[0].trim().toDouble(), points[1].trim().toDouble()))
        }
        return polyline
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_ITEM_ID = "ARG_ITEM_ID"

        @JvmStatic
        fun newInstance(itemId: Int) =
            TrackFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ITEM_ID, itemId)
                }
            }
    }
}