package com.example.fixlink

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.topAppBarFragmentContainer, TopAppBarFragment())
                replace(R.id.bottomNavigationContainer, BottomNavigationAdminFragment())
            }
        }
    }
} 