package com.example.fixlink

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment

class TopAppBarFragment : Fragment() {
    private lateinit var backButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_top_app_bar, container, false)
        backButton = view.findViewById(R.id.backButton)
        
        backButton.setOnClickListener {
            // If we're in a fragment that's part of a back stack, pop it
            if (parentFragmentManager.backStackEntryCount > 0) {
                parentFragmentManager.popBackStack()
            } else {
                // Otherwise, finish the activity
                activity?.finish()
            }
        }
        
        return view
    }

    fun showBackButton() {
        backButton.visibility = View.VISIBLE
    }

    fun hideBackButton() {
        backButton.visibility = View.GONE
    }
} 