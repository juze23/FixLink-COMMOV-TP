package com.example.fixlink

import androidx.fragment.app.Fragment
import com.example.fixlink.BottomNavigationFragment
import com.example.fixlink.BottomNavigationUserFragment
import com.example.fixlink.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object NavigationUtils {
    private val userRepository = UserRepository()

    suspend fun getBottomNavigationFragment(): Fragment {
        return withContext(Dispatchers.IO) {
            val user = userRepository.getCurrentUser().getOrNull()
            when (user?.typeId) {
                1 -> BottomNavigationUserFragment() // Regular user
                2 -> BottomNavigationFragment()  // Technician
                else -> BottomNavigationAdminFragment() // Admin/Manager
            }
        }
    }
} 