package com.example.fixlink

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import android.widget.LinearLayout
import android.widget.TextView
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView

class ViewAllListActivity : AppCompatActivity() {

    private lateinit var listContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_all_list)

        // Add the Top App Bar Fragment
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.topAppBarFragmentContainer, TopAppBarFragment())
            }
        }

        listContainer = findViewById(R.id.listContainer)

        // Get the list type from the intent
        val listType = intent.getStringExtra("list_type")

        // Populate the list based on the type
        if (listType == "technicians") {
            // TODO: Fetch actual technicians data
            val items = listOf("Luís Fernandes", "Luís Santos", "João Silva", "Tiago Gomes", "Ana Pereira", "Carlos Oliveira", "Sofia Martins")
            populateList(items, "Technicians")
        } else if (listType == "equipments") {
            // TODO: Fetch actual equipments data
            val items = listOf("Fire Extinguisher", "Server Room UPS System", "Air Conditioning System", "Printer Paper", "Projector - Room 101", "Smartboard - Room 203")
            populateList(items, "Equipments")
        }
    }

    private fun populateList(items: List<String>, title: String) {
        // TODO: Update Top App Bar title if needed
        // val topAppBarFragment = supportFragmentManager.findFragmentById(R.id.topAppBarFragmentContainer) as? TopAppBarFragment
        // topAppBarFragment?.updateTitle(title)

        val inflater = layoutInflater
        listContainer.removeAllViews() // Clear previous views
        for (item in items) {
            val itemView = inflater.inflate(R.layout.list_item_admin, listContainer, false)
            val nameTextView = itemView.findViewById<TextView>(R.id.itemNameTextView)
            val editIcon = itemView.findViewById<ImageView>(R.id.editIcon)
            val deleteIcon = itemView.findViewById<ImageView>(R.id.deleteIcon)

            nameTextView.text = item

            // TODO: Implement click listeners for edit and delete icons
            editIcon.setOnClickListener { 
                // Handle edit click - maybe pass data back or show a dialog
            }
            deleteIcon.setOnClickListener { 
                // Handle delete click
            }

            listContainer.addView(itemView)
        }
    }
} 