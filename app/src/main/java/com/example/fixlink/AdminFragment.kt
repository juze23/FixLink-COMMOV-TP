package com.example.fixlink

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit

class AdminFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin, container, false)
        childFragmentManager.commit {
            replace(R.id.topAppBarFragmentContainer, TopAppBarFragment())
            replace(R.id.bottomNavigationContainer, BottomNavigationAdminFragment())
        }
        return view
    }
} 