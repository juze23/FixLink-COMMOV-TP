package com.example.fixlink.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fixlink.R
import com.example.fixlink.fragments.BottomNavigationFragment
import com.example.fixlink.fragments.FirstLoginFragment
import com.example.fixlink.fragments.TopAppBarFragment

class FirstLoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_first_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val topAppBarFragment = TopAppBarFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.topAppBarFragmentContainer, topAppBarFragment)
            .commit()

        val firstLoginFragment = FirstLoginFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.firstLoginFragmentContainer, firstLoginFragment)
            .commit()

        val bottomNavFragment = BottomNavigationFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.bottomNavigationContainer, bottomNavFragment)
            .commit()
    }
}