package com.example.fixlink

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fixlink.TopAppBarFragment
import com.example.fixlink.BottomNavigationFragment
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat


class RegisterIssueActivity : AppCompatActivity() {

    private lateinit var equipmentSpinner: Spinner
    private lateinit var prioritySpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_issue)
        // Further setup like finding views and setting listeners will go here

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.topAppBarFragmentContainer, TopAppBarFragment())
                .commit()

            supportFragmentManager.beginTransaction()
                .replace(R.id.bottomNavigationContainer, BottomNavigationFragment())
                .commit()
        }

        setupSpinners()
        setLabelColors()
    }

    private fun setupSpinners() {
        equipmentSpinner = findViewById(R.id.equipment_spinner)
        prioritySpinner = findViewById(R.id.priority_spinner)

        // Create an ArrayAdapter using the string array and a default spinner layout
        val equipmentAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.equipment_options,
            android.R.layout.simple_spinner_item
        )

        val priorityAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.priority_options,
            android.R.layout.simple_spinner_item
        )

        // Specify the layout to use when the list of choices appears
        equipmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Apply the adapter to the spinner
        equipmentSpinner.adapter = equipmentAdapter
        prioritySpinner.adapter = priorityAdapter
    }

    private fun setLabelColors() {
        val purpleColor = ContextCompat.getColor(this, R.color.purple_primary)

        val titleLabel = findViewById<TextView>(R.id.title_label)
        val descriptionLabel = findViewById<TextView>(R.id.description_label)
        val locationLabel = findViewById<TextView>(R.id.location_label)
        val equipmentLabel = findViewById<TextView>(R.id.equipment_label)
        val priorityLabel = findViewById<TextView>(R.id.priority_label)

        titleLabel.setTextColor(purpleColor)
        descriptionLabel.setTextColor(purpleColor)
        locationLabel.setTextColor(purpleColor)
        equipmentLabel.setTextColor(purpleColor)
        priorityLabel.setTextColor(purpleColor)
    }
} 