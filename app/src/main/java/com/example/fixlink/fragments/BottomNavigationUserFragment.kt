package com.example.fixlink.fragments

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.fixlink.R
import com.example.fixlink.activities.IssuesUserActivity
import com.example.fixlink.activities.MaintenanceUserActivity
import com.example.fixlink.activities.ProfileActivity


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BottomNavigationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BottomNavigationUserFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var navIssues: LinearLayout
    private lateinit var navMaintenance: LinearLayout
    private lateinit var navProfile: LinearLayout

    private lateinit var iconIssues: ImageView
    private lateinit var iconMaintenance: ImageView
    private lateinit var iconProfile: ImageView

    private lateinit var textIssues: TextView
    private lateinit var textMaintenance: TextView
    private lateinit var textProfile: TextView

    private var selectedItemId: Int = R.id.nav_issues // Default selected item

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            // Get selected item from arguments if provided
            selectedItemId = it.getInt("selected_item", R.id.nav_issues)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bottom_navigation_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        navIssues = view.findViewById(R.id.nav_issues)
        navMaintenance = view.findViewById(R.id.nav_maintenance)
        navProfile = view.findViewById(R.id.nav_profile)

        iconIssues = view.findViewById(R.id.icon_issues)
        iconMaintenance = view.findViewById(R.id.icon_maintenance)
        iconProfile = view.findViewById(R.id.icon_profile)

        textIssues = view.findViewById(R.id.text_issues)
        textMaintenance = view.findViewById(R.id.text_maintenance)
        textProfile = view.findViewById(R.id.text_profile)

        // Only set selected item based on activity if no selected item was provided in arguments
        if (arguments?.getInt("selected_item", -1) == -1) {
            selectedItemId = when (activity) {
                is IssuesUserActivity -> R.id.nav_issues
                is MaintenanceUserActivity -> R.id.nav_maintenance
                is ProfileActivity -> R.id.nav_profile
                else -> R.id.nav_issues
            }
        }
        updateColors(selectedItemId)

        // Set click listeners with visual feedback
        navIssues.setOnClickListener { 
            if (activity !is IssuesUserActivity) {
                selectItem(R.id.nav_issues)
                val options = ActivityOptions.makeCustomAnimation(
                    context,
                    0,
                    0
                )
                val intent = Intent(requireContext(), IssuesUserActivity::class.java)
                startActivity(intent, options.toBundle())
            }
        }
        navMaintenance.setOnClickListener { 
            if (activity !is MaintenanceUserActivity) {
                selectItem(R.id.nav_maintenance)
                val options = ActivityOptions.makeCustomAnimation(
                    context,
                    0,
                    0
                )
                val intent = Intent(requireContext(), MaintenanceUserActivity::class.java)
                startActivity(intent, options.toBundle())
            }
        }
        navProfile.setOnClickListener { 
            if (activity !is ProfileActivity) {
                selectItem(R.id.nav_profile)
                val options = ActivityOptions.makeCustomAnimation(
                    context,
                    0,
                    0
                )
                val intent = Intent(requireContext(), ProfileActivity::class.java)
                startActivity(intent, options.toBundle())
            }
        }
    }

    private fun selectItem(itemId: Int) {
        selectedItemId = itemId
        updateColors(selectedItemId)
    }

    private fun updateColors(selectedItemId: Int) {
        val defaultColor = ContextCompat.getColor(requireContext(), R.color.gray_inactive)
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.purple_primary)

        // Reset all items to default color
        iconIssues.setColorFilter(defaultColor)
        textIssues.setTextColor(defaultColor)
        iconMaintenance.setColorFilter(defaultColor)
        textMaintenance.setTextColor(defaultColor)
        iconProfile.setColorFilter(defaultColor)
        textProfile.setTextColor(defaultColor)

        // Set selected item color
        when (selectedItemId) {
            R.id.nav_issues -> {
                iconIssues.setColorFilter(selectedColor)
                textIssues.setTextColor(selectedColor)
            }
            R.id.nav_maintenance -> {
                iconMaintenance.setColorFilter(selectedColor)
                textMaintenance.setTextColor(selectedColor)
            }
            R.id.nav_profile -> {
                iconProfile.setColorFilter(selectedColor)
                textProfile.setTextColor(selectedColor)
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BottomNavigationFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BottomNavigationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}