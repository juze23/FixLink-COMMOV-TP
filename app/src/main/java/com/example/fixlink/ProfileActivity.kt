package com.example.fixlink

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.content.Intent
import android.widget.Toast
import android.widget.TextView
import android.widget.ImageView
import com.example.fixlink.data.repository.UserRepository
import com.example.fixlink.data.entities.User
import com.example.fixlink.NavigationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.fragment.app.commit
import com.example.fixlink.data.preferences.LoginPreferences
import com.example.fixlink.data.preferences.ProfilePreferences
import android.net.ConnectivityManager
import android.content.Context
import android.net.NetworkCapabilities
import com.google.android.material.snackbar.Snackbar
import android.view.View
import android.widget.ProgressBar

class ProfileActivity : AppCompatActivity(), NotificationsFragment.NotificationUpdateListener {

    private lateinit var btnEditProfile: Button
    private lateinit var btnLogout: Button
    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var syncStatusTextView: TextView
    private lateinit var syncProgressBar: ProgressBar
    private var isFromAdmin: Boolean = false

    private val userRepository = UserRepository()
    private var currentUser: User? = null
    private lateinit var loginPreferences: LoginPreferences
    private lateinit var profilePreferences: ProfilePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        profilePreferences = ProfilePreferences(this)
        loginPreferences = LoginPreferences(this)

        // Check if coming from admin
        isFromAdmin = intent.getBooleanExtra("FROM_ADMIN", false)

        initializeViews()
        
        // Load user data first
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = userRepository.getCurrentUser()
                result.fold(
                    onSuccess = { user ->
                        currentUser = user
                        withContext(Dispatchers.Main) {
                            updateUI(user)
                            // Now that we have the user data, add the fragments
                            if (savedInstanceState == null) {
                                val topAppBarFragment = TopAppBarFragment().apply {
                                    arguments = Bundle().apply {
                                        putString("userId", user.user_id)
                                    }
                                }
                                supportFragmentManager.beginTransaction()
                                    .replace(R.id.topAppBarFragmentContainer, topAppBarFragment)
                                    .commit()

                                // Add appropriate bottom navigation based on user type
                                val bottomNavFragment = withContext(Dispatchers.IO) {
                                    NavigationUtils.getBottomNavigationFragment()
                                }
                                // Set the selected item to profile
                                when (bottomNavFragment) {
                                    is BottomNavigationUserFragment,
                                    is BottomNavigationFragment,
                                    is BottomNavigationAdminFragment -> {
                                        bottomNavFragment.arguments = Bundle().apply {
                                            putInt("selected_item", R.id.nav_profile)
                                        }
                                    }
                                }
                                supportFragmentManager.beginTransaction()
                                    .replace(R.id.bottomNavigationContainer, bottomNavFragment)
                                    .commit()
                            }
                            hideSyncStatus()
                        }
                    },
                    onFailure = { error ->
                        withContext(Dispatchers.Main) {
                            // If we have cached data, show it
                            if (currentUser != null) {
                                updateUI(currentUser!!)
                                if (!isNetworkAvailable()) {
                                    showOfflineSyncStatus()
                                }
                            } else {
                                // Only show error if we have no cached data
                                Toast.makeText(this@ProfileActivity, "Error loading profile: ${error.message}", Toast.LENGTH_SHORT).show()
                            }
                            hideSyncStatus()
                        }
                    }
                )
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // If we have cached data, show it
                    if (currentUser != null) {
                        updateUI(currentUser!!)
                        if (!isNetworkAvailable()) {
                            showOfflineSyncStatus()
                        }
                    } else {
                        // Only show error if we have no cached data
                        Toast.makeText(this@ProfileActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                    hideSyncStatus()
                }
            }
        }

        btnEditProfile.setOnClickListener { navigateToEditProfile() }
        btnLogout.setOnClickListener { handleLogout() }

        // Check for pending updates
        checkPendingUpdates()
    }

    private fun initializeViews() {
        btnEditProfile = findViewById(R.id.btn_edit_profile)
        btnLogout = findViewById(R.id.btn_logout)
        nameTextView = findViewById(R.id.edit_name)
        emailTextView = findViewById(R.id.edit_email)
        phoneTextView = findViewById(R.id.edit_phone)
        syncStatusTextView = findViewById(R.id.syncStatusTextView)
        syncProgressBar = findViewById(R.id.syncProgressBar)
    }

    private fun loadUserData() {
        // Show loading state
        syncProgressBar.visibility = View.VISIBLE
        syncStatusTextView.visibility = View.VISIBLE
        syncStatusTextView.text = "Loading profile..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = userRepository.getCurrentUser()
                result.fold(
                    onSuccess = { user ->
                        currentUser = user
                        withContext(Dispatchers.Main) {
                            updateUI(user)
                            // Update TopAppBarFragment with current user ID
                            val topAppBarFragment = supportFragmentManager.findFragmentById(R.id.topAppBarFragmentContainer) as? TopAppBarFragment
                            topAppBarFragment?.arguments = Bundle().apply {
                                putString("userId", user.user_id)
                            }
                            hideSyncStatus()
                        }
                    },
                    onFailure = { error ->
                        withContext(Dispatchers.Main) {
                            // If we have cached data, show it
                            if (currentUser != null) {
                                updateUI(currentUser!!)
                                if (!isNetworkAvailable()) {
                                    showOfflineSyncStatus()
                                }
                            } else {
                                // Only show error if we have no cached data
                                Toast.makeText(this@ProfileActivity, "Error loading profile: ${error.message}", Toast.LENGTH_SHORT).show()
                            }
                            hideSyncStatus()
                        }
                    }
                )
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // If we have cached data, show it
                    if (currentUser != null) {
                        updateUI(currentUser!!)
                        if (!isNetworkAvailable()) {
                            showOfflineSyncStatus()
                        }
                    } else {
                        // Only show error if we have no cached data
                        Toast.makeText(this@ProfileActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                    hideSyncStatus()
                }
            }
        }
    }

    private fun updateUI(user: User) {
        nameTextView.text = if (user.lastname.isNullOrEmpty()) user.firstname else "${user.firstname} ${user.lastname}"
        emailTextView.text = user.email
        phoneTextView.text = user.phoneNumber ?: "Not set"
        // TODO: Load profile image if available
    }

    private fun navigateToEditProfile() {
        val intent = Intent(this, EditProfileActivity::class.java).apply {
            putExtra("USER_ID", currentUser?.user_id)
            putExtra("USER_NAME", if (currentUser?.lastname.isNullOrEmpty()) currentUser?.firstname else "${currentUser?.firstname} ${currentUser?.lastname}")
            putExtra("USER_EMAIL", currentUser?.email)
            putExtra("USER_PHONE", currentUser?.phoneNumber)
        }
        startActivity(intent)
    }

    private fun handleLogout() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = userRepository.logout()
                result.fold(
                    onSuccess = {
                        // Clear login preferences
                        loginPreferences.clearLoginState()
                        
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@ProfileActivity, "Logged out successfully", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@ProfileActivity, LoginActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            startActivity(intent)
                            finish()
                        }
                    },
                    onFailure = { error ->
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@ProfileActivity, "Error logging out: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProfileActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
        
        // Show back button only if coming from admin
        if (isFromAdmin) {
            val topAppBarFragment = supportFragmentManager.findFragmentById(R.id.topAppBarFragmentContainer) as? TopAppBarFragment
            topAppBarFragment?.showBackButton()
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun checkPendingUpdates() {
        if (profilePreferences.hasPendingUpdates()) {
            val pendingUpdate = profilePreferences.getPendingProfileUpdate()
            if (pendingUpdate != null) {
                if (isNetworkAvailable()) {
                    syncPendingUpdate(pendingUpdate)
                } else {
                    showOfflineSyncStatus()
                }
            }
        } else {
            hideSyncStatus()
        }
    }

    private fun showOfflineSyncStatus() {
        syncStatusTextView.visibility = View.VISIBLE
        syncStatusTextView.text = "Changes saved offline - Will sync when online"
        syncProgressBar.visibility = View.GONE
    }

    private fun showSyncingStatus() {
        syncStatusTextView.visibility = View.VISIBLE
        syncStatusTextView.text = "Syncing changes..."
        syncProgressBar.visibility = View.VISIBLE
    }

    private fun hideSyncStatus() {
        syncStatusTextView.visibility = View.GONE
        syncProgressBar.visibility = View.GONE
    }

    private fun syncPendingUpdate(update: ProfilePreferences.ProfileUpdate) {
        showSyncingStatus()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = userRepository.updateUserProfile(
                    userId = update.userId,
                    firstname = update.firstname,
                    lastname = update.lastname,
                    email = update.email,
                    phone = update.phone
                )

                withContext(Dispatchers.Main) {
                    if (result.isSuccess) {
                        profilePreferences.clearPendingUpdates()
                        hideSyncStatus()
                        loadUserData() // Reload user data to show updated info
                    } else {
                        // If sync fails, show offline status
                        showOfflineSyncStatus()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // If any error occurs, show offline status
                    showOfflineSyncStatus()
                }
            }
        }
    }

    override fun onNotificationUpdated() {
        // Atualiza o contador de notificações no TopAppBarFragment
        val topAppBarFragment = supportFragmentManager.findFragmentById(R.id.topAppBarFragmentContainer) as? TopAppBarFragment
        topAppBarFragment?.startObservingNotifications()
    }
}