package com.example.fixlink

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.fixlink.ui.filters.IssuesFilterDialogFragment

class IssuesContentFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_issues_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val filterIcon: ImageView = view.findViewById(R.id.filterIcon)

        filterIcon.setOnClickListener {
            IssuesFilterDialogFragment().show(childFragmentManager, IssuesFilterDialogFragment.TAG)
        }
        // Setup for search, filter, RecyclerView, and add button goes here.
    }
} 