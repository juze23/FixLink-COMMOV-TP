package com.example.fixlink

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit

class AdminFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin, container, false)
        
        // Enable edge-to-edge display
        requireActivity().enableEdgeToEdge()
        
        // Handle window insets to make top bar extend into system bar area
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        childFragmentManager.commit {
            replace(R.id.topAppBarFragmentContainer, TopAppBarFragment())
            replace(R.id.bottomNavigationContainer, BottomNavigationAdminFragment())
        }
        return view
    }
} 