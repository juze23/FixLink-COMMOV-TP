package com.example.fixlink

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.fixlink.ui.filters.MaintenanceFilterDialogFragment
import android.util.Log

class MaintenanceContentFragment : Fragment() {

    private val TAG = "MaintenanceContentFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_maintenance_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called.")
        // Setup for search, filter, RecyclerView, and add button goes here.

        val filterIcon = view.findViewById<ImageView>(R.id.filterIcon)
        if (filterIcon != null) {
            Log.d(TAG, "Filter icon found.")
            filterIcon.setOnClickListener {
                Log.d(TAG, "Filter icon clicked. Showing dialog.")
                MaintenanceFilterDialogFragment().show(requireActivity().supportFragmentManager, MaintenanceFilterDialogFragment.TAG)
            }
        } else {
            Log.e(TAG, "Filter icon not found!")
        }
    }
} 