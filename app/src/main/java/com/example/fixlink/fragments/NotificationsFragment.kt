package com.example.fixlink

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fixlink.adapters.NotificationsAdapter
import com.example.fixlink.data.entities.Notification
import com.example.fixlink.data.repository.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [NotificationsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class NotificationsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private val notificationRepository = NotificationRepository()
    private var currentUserId: String? = null

    // Interface para comunicação com o TopAppBarFragment
    interface NotificationUpdateListener {
        fun onNotificationUpdated()
    }

    private var notificationUpdateListener: NotificationUpdateListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verifica se a activity implementa a interface
        if (context is NotificationUpdateListener) {
            notificationUpdateListener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        notificationUpdateListener = null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment NotificationFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NotificationsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)
        
        recyclerView = view.findViewById(R.id.notificationsRecyclerView)
        emptyView = view.findViewById(R.id.emptyNotificationsView)
        
        recyclerView.layoutManager = LinearLayoutManager(context)
        
        // Get current user ID from arguments
        currentUserId = arguments?.getString("userId")
        Log.d("NotificationsFragment", "onCreateView - currentUserId: $currentUserId")
        
        loadNotifications()
        
        return view
    }

    private fun loadNotifications() {
        val userId = currentUserId
        if (userId == null) {
            Log.e("NotificationsFragment", "loadNotifications - currentUserId is null")
            return
        }
        
        Log.d("NotificationsFragment", "loadNotifications - starting to load notifications for user: $userId")
        
        CoroutineScope(Dispatchers.IO).launch {
            val result = notificationRepository.getNotifications(userId)
            withContext(Dispatchers.Main) {
                if (result.isSuccess) {
                    val notifications = result.getOrNull() ?: emptyList()
                    Log.d("NotificationsFragment", "loadNotifications - loaded ${notifications.size} notifications")
                    
                    if (notifications.isEmpty()) {
                        Log.d("NotificationsFragment", "loadNotifications - no notifications, showing empty view")
                        recyclerView.visibility = View.GONE
                        emptyView.visibility = View.VISIBLE
                    } else {
                        Log.d("NotificationsFragment", "loadNotifications - setting up adapter with notifications")
                        recyclerView.visibility = View.VISIBLE
                        emptyView.visibility = View.GONE
                        recyclerView.adapter = NotificationsAdapter(notifications) { notification ->
                            markNotificationAsRead(notification)
                        }
                    }
                } else {
                    Log.e("NotificationsFragment", "loadNotifications - failed to load notifications: ${result.exceptionOrNull()}")
                }
            }
        }
    }

    private fun markNotificationAsRead(notification: Notification) {
        CoroutineScope(Dispatchers.IO).launch {
            notificationRepository.markNotificationAsRead(notification.notification_id)
            withContext(Dispatchers.Main) {
                loadNotifications() // Reload to update the UI
                // Notifica o TopAppBarFragment para atualizar o contador
                notificationUpdateListener?.onNotificationUpdated()
            }
        }
    }
} 