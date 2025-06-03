package com.example.fixlink

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.example.fixlink.ui.filters.IssuesFilterDialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class IssuesContentFragment : Fragment() {
    companion object {
        private const val TAG = "IssuesContentFragment"
    }

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
        val fabAddIssue: FloatingActionButton = view.findViewById(R.id.fab_add_issue)

        filterIcon.setOnClickListener {
            IssuesFilterDialogFragment().show(childFragmentManager, IssuesFilterDialogFragment.TAG)
        }

        fabAddIssue.setOnClickListener {
            Log.d(TAG, "FAB clicked, attempting to launch RegisterIssueActivity")
            try {
                val intent = Intent(requireContext(), RegisterIssueActivity::class.java)
                startActivity(intent)
                Log.d(TAG, "Successfully launched RegisterIssueActivity")
            } catch (e: Exception) {
                Log.e(TAG, "Error launching RegisterIssueActivity", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        // Setup for search, filter, RecyclerView, and add button goes here.
    }
} 