package com.example.gpswalktracker.ui.list_trackers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.gpswalktracker.databinding.FragmentListtrackersBinding
import com.example.gpswalktracker.ui.home.HomeFragment

class ListTrackersFragment : Fragment() {

    private var _binding: FragmentListtrackersBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val listTrackersViewModel =
            ViewModelProvider(this).get(ListTrackersViewModel::class.java)

        _binding = FragmentListtrackersBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textDashboard
        listTrackersViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
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