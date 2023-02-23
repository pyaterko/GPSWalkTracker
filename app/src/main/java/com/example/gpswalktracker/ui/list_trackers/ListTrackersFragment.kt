package com.example.gpswalktracker.ui.list_trackers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.elveum.elementadapter.simpleAdapter
import com.example.gpswalktracker.data.InfoTrackItem
import com.example.gpswalktracker.databinding.FragmentListtrackersBinding
import com.example.gpswalktracker.databinding.InfoTrackItemBinding
import com.example.gpswalktracker.ui.track.TrackFragment
import com.example.gpswalktracker.utils.launchFragment
import com.example.gpswalktracker.utils.viewModelCreator

class ListTrackersFragment : Fragment() {

    private var _binding: FragmentListtrackersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ListTrackersViewModel by viewModelCreator { ListTrackersViewModel(it.dataBase) }
    private val adapter =
        simpleAdapter<InfoTrackItem, InfoTrackItemBinding> {
            areItemsSame = { oldItem, newItem -> oldItem.id == newItem.id }
            bind { item ->
                tvData.text = item.date
                tvDistance.text = item.distance
                tvSpeed.text = item.speed
                tvTime.text = item.time
            }
            listeners {
                ivDelete.onClick {
                    viewModel.deleteInfoTrack(it)
                }
                root.onClick {
                    val i=it
                    launchFragment(TrackFragment.newInstance(it.id))
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentListtrackersBinding.inflate(inflater, container, false)
        viewModel.listData.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            binding.tvEmptyList.isVisible = it.isEmpty()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            rvTrackList.layoutManager = LinearLayoutManager(requireContext())
            rvTrackList.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = ListTrackersFragment()
    }
}