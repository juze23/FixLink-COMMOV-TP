package com.example.fixlink

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.content.Intent
import android.widget.Toast

class ProfileActivity : AppCompatActivity() {

    private lateinit var btnEditProfile: Button
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val topAppBarFragment = TopAppBarFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.topAppBarFragmentContainer, topAppBarFragment)
            .commit()

        val bottomNavFragment = BottomNavigationFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.bottomNavigationContainer, bottomNavFragment)
            .commit()

        btnEditProfile = findViewById(R.id.btn_edit_profile)
        btnLogout = findViewById(R.id.btn_logout)

        btnEditProfile.setOnClickListener { navigateToEditProfile() }
        btnLogout.setOnClickListener {
            Toast.makeText(this, "Logout clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToEditProfile() {
        val intent = Intent(this, EditProfileActivity::class.java)
        startActivity(intent)
    }
}