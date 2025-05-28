package com.example.fixlink

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

    private lateinit var iconDashboard: ImageView
    private lateinit var iconIssues: ImageView
    private lateinit var iconMaintenance: ImageView
    private lateinit var iconAdmin: ImageView

    private lateinit var textDashboard: TextView
    private lateinit var textIssues: TextView
    private lateinit var textMaintenance: TextView
    private lateinit var textAdmin: TextView

    private var selectedItemId: Int = R.id.nav_dashboard // Default selected item

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bottom_navigation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        navDashboard = view.findViewById(R.id.nav_dashboard)
        navIssues = view.findViewById(R.id.nav_issues)
        navMaintenance = view.findViewById(R.id.nav_maintenance)
        navAdmin = view.findViewById(R.id.nav_admin)

        iconDashboard = view.findViewById(R.id.icon_dashboard)
        iconIssues = view.findViewById(R.id.icon_issues)
        iconMaintenance = view.findViewById(R.id.icon_maintenance)
        iconAdmin = view.findViewById(R.id.icon_admin)

        textDashboard = view.findViewById(R.id.text_dashboard)
        textIssues = view.findViewById(R.id.text_issues)
        textMaintenance = view.findViewById(R.id.text_maintenance)
        textAdmin = view.findViewById(R.id.text_admin)

        // Set initial selected item color
        updateColors(selectedItemId)

        // Set click listeners
        navDashboard.setOnClickListener { selectItem(R.id.nav_dashboard) }
        navIssues.setOnClickListener { selectItem(R.id.nav_issues) }
        navMaintenance.setOnClickListener { selectItem(R.id.nav_maintenance) }
        navAdmin.setOnClickListener { selectItem(R.id.nav_admin) }
    }

    private fun selectItem(itemId: Int) {
        if (selectedItemId != itemId) {
            selectedItemId = itemId
            updateColors(selectedItemId)
        }
    }

    private fun updateColors(selectedItemId: Int) {
        val defaultColor = ContextCompat.getColor(requireContext(), R.color.default_nav_item_color)
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.purple_primary)

        // Reset all items to default (outline icons + default color)
        iconDashboard.setImageResource(R.drawable.ic_dashboard_outline)
        iconDashboard.setColorFilter(defaultColor)
        textDashboard.setTextColor(defaultColor)

        iconIssues.setImageResource(R.drawable.ic_issues_outline)
        iconIssues.setColorFilter(defaultColor)
        textIssues.setTextColor(defaultColor)

        iconMaintenance.setImageResource(R.drawable.ic_maintenace_outline)
        iconMaintenance.setColorFilter(defaultColor)
        textMaintenance.setTextColor(defaultColor)

        iconAdmin.setImageResource(R.drawable.ic_admin_outline)
        iconAdmin.setColorFilter(defaultColor)
        textAdmin.setTextColor(defaultColor)

        // Set selected item (filled icon + selected color)
        when (selectedItemId) {
            R.id.nav_dashboard -> {
                iconDashboard.setImageResource(R.drawable.ic_dashboard)
                iconDashboard.setColorFilter(selectedColor)
                textDashboard.setTextColor(selectedColor)
            }
            R.id.nav_issues -> {
                iconIssues.setImageResource(R.drawable.ic_issues)
                iconIssues.setColorFilter(selectedColor)
                textIssues.setTextColor(selectedColor)
            }
            R.id.nav_maintenance -> {
                iconMaintenance.setImageResource(R.drawable.ic_maintenance)
                iconMaintenance.setColorFilter(selectedColor)
                textMaintenance.setTextColor(selectedColor)
            }
            R.id.nav_admin -> {
                iconAdmin.setImageResource(R.drawable.ic_admin)
                iconAdmin.setColorFilter(selectedColor)
                textAdmin.setTextColor(selectedColor)
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