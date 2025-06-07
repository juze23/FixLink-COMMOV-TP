package com.example.fixlink

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fixlink.NavigationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IssuesUserActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_issues_user)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_issues_host)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Add TopAppBarFragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.topAppBarFragmentContainer, TopAppBarFragment())
                .commit()

            // Add IssuesContentFragment
            supportFragmentManager.beginTransaction()
                .replace(R.id.issuesContentFragmentContainer, IssuesContentFragment())
                .commit()

            // Add appropriate bottom navigation based on user type
            CoroutineScope(Dispatchers.Main).launch {
                val bottomNavFragment = withContext(Dispatchers.IO) {
                    NavigationUtils.getBottomNavigationFragment()
                }
                supportFragmentManager.beginTransaction()
                    .replace(R.id.bottomNavigationContainer, bottomNavFragment)
                    .commit()
            }
        }
    }
} 