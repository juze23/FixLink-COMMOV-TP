package com.example.fixlink

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fixlink.TopAppBarFragment
import com.example.fixlink.BottomNavigationAdminFragment
import androidx.fragment.app.commit
import android.widget.LinearLayout
import android.widget.TextView
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout.LayoutParams
import android.util.TypedValue
import androidx.core.content.ContextCompat
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.widget.ImageView

class ChooseTechnicianActivity : AppCompatActivity() {

    private lateinit var techniciansListLayout: LinearLayout
    private var selectedTechnicianView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_technician)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.topAppBarFragmentContainer, TopAppBarFragment())
                replace(R.id.bottomNavigationContainer, BottomNavigationAdminFragment())
            }
        }

        techniciansListLayout = findViewById(R.id.techniciansListLayout)
        populateTechnicianList()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun populateTechnicianList() {
        val technicians = listOf(
            "Luís Fernandes",
            "Ana Silva",
            "Cristiano Ronaldo",
            "Diogo Costa",
            "Pepe",
            "Bernardo Silva",
            "Rafael Leão",
            "Bruno Fernandes",
            "Diogo Dalot",
            "Rúben Dias",
            "Vitinha",
            "Danilo Pereira"
        )

        val inflater = LayoutInflater.from(this)
        val marginBottom = resources.getDimensionPixelSize(R.dimen.technician_item_margin_bottom)

        for (technician in technicians) {
            val technicianItemView = inflater.inflate(R.layout.list_item_technician, techniciansListLayout, false)
            val technicianNameTextView = technicianItemView.findViewById<TextView>(R.id.technicianNameTextView)
            val checkmarkImageView = technicianItemView.findViewById<ImageView>(R.id.checkmarkImageView)

            technicianNameTextView.text = technician

            // Set layout parameters with margin
            val layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                this.bottomMargin = marginBottom
            }
            technicianItemView.layoutParams = layoutParams

            // Set click listener
            technicianItemView.setOnClickListener { 
                selectTechnician(it)
            }

            techniciansListLayout.addView(technicianItemView)
        }
    }

    private fun selectTechnician(itemView: View) {
        // Hide checkmark for previously selected item
        selectedTechnicianView?.findViewById<ImageView>(R.id.checkmarkImageView)?.visibility = View.GONE

        // Show checkmark for the newly selected item
        itemView.findViewById<ImageView>(R.id.checkmarkImageView)?.visibility = View.VISIBLE

        // Update the selected view reference
        selectedTechnicianView = itemView

        // You can also store the selected technician's name or data here if needed
        val selectedName = itemView.findViewById<TextView>(R.id.technicianNameTextView).text.toString()
        // For example, store in a ViewModel or pass back to the calling activity
    }
}