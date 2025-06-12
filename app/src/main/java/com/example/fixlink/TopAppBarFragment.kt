package com.example.fixlink

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.fixlink.data.repository.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TopAppBarFragment : Fragment() {
    private lateinit var backButton: ImageButton
    private lateinit var notificationButton: ImageButton
    private lateinit var notificationBadge: TextView
    private val notificationRepository = NotificationRepository()
    private var currentUserId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_top_app_bar, container, false)
        backButton = view.findViewById(R.id.backButton)
        notificationButton = view.findViewById(R.id.notificationButton)
        notificationBadge = view.findViewById(R.id.notificationBadge)
        
        backButton.setOnClickListener {
            // If we're in a fragment that's part of a back stack, pop it
            if (parentFragmentManager.backStackEntryCount > 0) {
                parentFragmentManager.popBackStack()
            } else {
                // Otherwise, finish the activity
                activity?.finish()
            }
        }

        // Only show notification button in ProfileActivity
        if (activity is ProfileActivity) {
            notificationButton.visibility = View.VISIBLE
            notificationButton.setOnClickListener {
                val fragment = NotificationsFragment().apply {
                    arguments = Bundle().apply {
                        putString("userId", currentUserId)
                    }
                }
                // Show notifications container and hide profile content
                activity?.findViewById<View>(R.id.notificationsContainer)?.visibility = View.VISIBLE
                activity?.findViewById<View>(R.id.profileContentContainer)?.visibility = View.GONE
                
                parentFragmentManager.beginTransaction()
                    .replace(R.id.notificationsContainer, fragment)
                    .addToBackStack(null)
                    .commit()
            }
            // Start observing notifications only in ProfileActivity
            startObservingNotifications()
        } else {
            notificationButton.visibility = View.GONE
        }

        // Check if we should show the back button based on arguments
        arguments?.getBoolean("show_back_button", false)?.let { showBack ->
            if (showBack) {
                showBackButton()
            } else {
                hideBackButton()
            }
        }

        // Get current user ID from arguments
        currentUserId = arguments?.getString("userId")
        
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateFromArguments()
    }

    override fun onResume() {
        super.onResume()
        updateFromArguments()
        // Only observe notifications in ProfileActivity
        if (activity is ProfileActivity) {
            startObservingNotifications()
        }
    }

    private fun updateFromArguments() {
        arguments?.getString("userId")?.let { userId ->
            if (currentUserId != userId) {
                currentUserId = userId
                if (activity is ProfileActivity) {
                    startObservingNotifications()
                }
            }
        }
    }

    fun startObservingNotifications() {
        val userId = currentUserId ?: return
        
        CoroutineScope(Dispatchers.IO).launch {
            val result = notificationRepository.getUnreadNotificationsCount(userId)
            withContext(Dispatchers.Main) {
                if (result.isSuccess) {
                    val count = result.getOrNull() ?: 0
                    updateNotificationBadge(count)
                }
            }
        }
    }

    private fun updateNotificationBadge(count: Int) {
        if (count > 0) {
            notificationBadge.text = count.toString()
            notificationBadge.visibility = View.VISIBLE
        } else {
            notificationBadge.visibility = View.GONE
        }
    }

    fun showBackButton() {
        backButton.visibility = View.VISIBLE
    }

    fun hideBackButton() {
        backButton.visibility = View.GONE
    }
} 