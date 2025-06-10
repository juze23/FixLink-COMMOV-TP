package com.example.fixlink

import android.app.ActivityOptions
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import android.content.Intent

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class BottomNavigationAdminFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var navDashboard: LinearLayout
    private lateinit var navIssues: LinearLayout
    private lateinit var navMaintenance: LinearLayout
    private lateinit var navAdmin: LinearLayout
    private lateinit var navProfile: LinearLayout

    private lateinit var iconDashboard: ImageView
    private lateinit var iconIssues: ImageView
    private lateinit var iconMaintenance: ImageView
    private lateinit var iconAdmin: ImageView
    private lateinit var iconProfile: ImageView

    private lateinit var textDashboard: TextView
    private lateinit var textIssues: TextView
    private lateinit var textMaintenance: TextView
    private lateinit var textAdmin: TextView
    private lateinit var textProfile: TextView

    private var selectedItemId: Int = R.id.nav_dashboard // Default selected item

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            // Get selected item from arguments if provided
            selectedItemId = it.getInt("selected_item", R.id.nav_dashboard)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bottom_navigation_admin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        navDashboard = view.findViewById(R.id.nav_dashboard)
        navIssues = view.findViewById(R.id.nav_issues)
        navMaintenance = view.findViewById(R.id.nav_maintenance)
        navAdmin = view.findViewById(R.id.nav_admin)
        navProfile = view.findViewById(R.id.nav_profile)

        iconDashboard = view.findViewById(R.id.icon_dashboard)
        iconIssues = view.findViewById(R.id.icon_issues)
        iconMaintenance = view.findViewById(R.id.icon_maintenance)
        iconAdmin = view.findViewById(R.id.icon_admin)
        iconProfile = view.findViewById(R.id.icon_profile)

        textDashboard = view.findViewById(R.id.text_dashboard)
        textIssues = view.findViewById(R.id.text_issues)
        textMaintenance = view.findViewById(R.id.text_maintenance)
        textAdmin = view.findViewById(R.id.text_admin)
        textProfile = view.findViewById(R.id.text_profile)

        // Set initial selected item based on current activity
        selectedItemId = when (activity) {
            is DashboardActivity -> R.id.nav_dashboard
            is IssuesUserActivity -> R.id.nav_issues
            is MaintenanceUserActivity -> R.id.nav_maintenance
            is AdminActivity -> R.id.nav_admin
            is ProfileActivity -> R.id.nav_profile
            else -> R.id.nav_dashboard
        }
        updateColors(selectedItemId)

        // Set click listeners with visual feedback
        navDashboard.setOnClickListener {
            if (activity !is DashboardActivity) {
                selectItem(R.id.nav_dashboard)
                val options = ActivityOptions.makeCustomAnimation(
                    context,
                    0,
                    0
                )
                val intent = Intent(requireContext(), DashboardActivity::class.java)
                startActivity(intent, options.toBundle())
                activity?.finish()
            }
        }
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
                activity?.finish()
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
                activity?.finish()
            }
        }
        navAdmin.setOnClickListener {
            if (activity !is AdminActivity) {
                selectItem(R.id.nav_admin)
                val options = ActivityOptions.makeCustomAnimation(
                    context,
                    0,
                    0
                )
                val intent = Intent(requireContext(), AdminActivity::class.java)
                startActivity(intent, options.toBundle())
                activity?.finish()
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
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent, options.toBundle())
                activity?.finish()
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
        iconDashboard.setColorFilter(defaultColor)
        textDashboard.setTextColor(defaultColor)
        iconIssues.setColorFilter(defaultColor)
        textIssues.setTextColor(defaultColor)
        iconMaintenance.setColorFilter(defaultColor)
        textMaintenance.setTextColor(defaultColor)
        iconAdmin.setColorFilter(defaultColor)
        textAdmin.setTextColor(defaultColor)
        iconProfile.setColorFilter(defaultColor)
        textProfile.setTextColor(defaultColor)

        // Set selected item color
        when (selectedItemId) {
            R.id.nav_dashboard -> {
                iconDashboard.setColorFilter(selectedColor)
                textDashboard.setTextColor(selectedColor)
            }
            R.id.nav_issues -> {
                iconIssues.setColorFilter(selectedColor)
                textIssues.setTextColor(selectedColor)
            }
            R.id.nav_maintenance -> {
                iconMaintenance.setColorFilter(selectedColor)
                textMaintenance.setTextColor(selectedColor)
            }
            R.id.nav_admin -> {
                iconAdmin.setColorFilter(selectedColor)
                textAdmin.setTextColor(selectedColor)
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
            BottomNavigationAdminFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}