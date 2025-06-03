package com.example.fixlink

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class ViewAllListFragment : Fragment() {

    private lateinit var listContainer: LinearLayout
    private lateinit var fragmentTitleTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_view_all_list, container, false)

        listContainer = view.findViewById(R.id.listContainer)
        fragmentTitleTextView = view.findViewById(R.id.fragmentTitleTextView)

        val listType = arguments?.getString(ARG_LIST_TYPE)

        if (listType == "technicians") {
            fragmentTitleTextView.text = "All Technicians"
            // TODO: Fetch and display all technicians
            val items = listOf("Luís Fernandes", "Luís Santos", "João Silva", "Tiago Gomes", "Ana Pereira", "Carlos Oliveira", "Sofia Martins")
            populateList(items)

        } else if (listType == "equipments") {
            fragmentTitleTextView.text = "All Equipments"
            // TODO: Fetch and display all equipments
            val items = listOf("Fire Extinguisher", "Server Room UPS System", "Air Conditioning System", "Printer Paper", "Projector - Room 101", "Smartboard - Room 203")
             populateList(items)
        }

        return view
    }

    private fun populateList(items: List<String>) {
        val inflater = layoutInflater
        listContainer.removeAllViews()
        for (item in items) {
            val itemView = inflater.inflate(R.layout.list_item_admin, listContainer, false)
            val nameTextView = itemView.findViewById<TextView>(R.id.itemNameTextView)
            val editIcon = itemView.findViewById<ImageView>(R.id.editIcon)
            val deleteIcon = itemView.findViewById<ImageView>(R.id.deleteIcon)

            nameTextView.text = item

            // TODO: Implement click listeners for edit and delete icons if needed in this view
            editIcon.setOnClickListener { 
                // Handle edit click
            }
            deleteIcon.setOnClickListener { 
                // Handle delete click
            }

            listContainer.addView(itemView)
        }
    }

    companion object {
        private const val ARG_LIST_TYPE = "list_type"

        fun newInstance(listType: String): ViewAllListFragment {
            val fragment = ViewAllListFragment()
            val args = Bundle()
            args.putString(ARG_LIST_TYPE, listType)
            fragment.arguments = args
            return fragment
        }
    }
} 