package com.example.fixlink

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.fixlink.NavigationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyTasksActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContentView(R.layout.activity_my_tasks)

        // Handle system insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            // Apply padding to the top app bar container
            findViewById<View>(R.id.topAppBarFragmentContainer).setPadding(
                insets.left,
                insets.top,
                insets.right,
                0
            )
            
            // Apply padding to the bottom navigation container
            findViewById<View>(R.id.bottomNavigationContainer).setPadding(
                insets.left,
                0,
                insets.right,
                insets.bottom
            )
            
            windowInsets
        }

        if (savedInstanceState == null) {
            // Add TopAppBarFragment
            supportFragmentManager.beginTransaction()
                .replace(R.id.topAppBarFragmentContainer, TopAppBarFragment())
                .commit()

            // Add MyTasksFragment
            supportFragmentManager.beginTransaction()
                .replace(R.id.myTasksContentFragmentContainer, MyTasksFragment())
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