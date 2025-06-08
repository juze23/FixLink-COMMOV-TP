package com.example.fixlink

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fixlink.data.entities.User
import com.example.fixlink.data.repository.IssueRepository
import com.example.fixlink.data.repository.UserRepository
import com.example.fixlink.databinding.ActivityChooseTechnicianBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChooseTechnicianActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChooseTechnicianBinding
    private lateinit var userRepository: UserRepository
    private lateinit var issueRepository: IssueRepository
    private lateinit var adapter: TechnicianAdapter
    private var issueId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChooseTechnicianBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Add the Top App Bar Fragment
        if (savedInstanceState == null) {
            val topAppBarFragment = TopAppBarFragment()
            supportFragmentManager.commit {
                replace(R.id.topAppBarFragmentContainer, topAppBarFragment)
            }
        }

        // Get issue ID from intent
        issueId = intent.getStringExtra("ISSUE_ID")
        if (issueId == null) {
            Toast.makeText(this, "Error: Issue ID not provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize repositories
        userRepository = UserRepository()
        issueRepository = IssueRepository()

        // Set up RecyclerView
        binding.techniciansRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TechnicianAdapter { technician ->
            assignTechnician(technician)
        }
        binding.techniciansRecyclerView.adapter = adapter

        // Add bottom navigation fragment
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.bottomNavigationContainer, BottomNavigationAdminFragment())
            }
        }

        // Load technicians
        loadTechnicians()
    }

    override fun onResume() {
        super.onResume()
        // Show back button in top app bar after fragment is initialized
        val topAppBarFragment = supportFragmentManager.findFragmentById(R.id.topAppBarFragmentContainer) as? TopAppBarFragment
        topAppBarFragment?.showBackButton()
    }

    private fun loadTechnicians() {
        showLoading(true)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val usersResult = userRepository.getAllUsers()
                if (usersResult.isFailure) {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                        Toast.makeText(this@ChooseTechnicianActivity, "Error loading technicians", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val technicians = usersResult.getOrNull()?.filter { it.typeId == 2 } ?: emptyList()
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    if (technicians.isEmpty()) {
                        Toast.makeText(this@ChooseTechnicianActivity, "No technicians available", Toast.LENGTH_SHORT).show()
                    } else {
                        adapter.submitList(technicians)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Log.e("ChooseTechnicianActivity", "Error loading technicians", e)
                    Toast.makeText(this@ChooseTechnicianActivity, "Error loading technicians", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun assignTechnician(technician: User) {
        showLoading(true)
        val currentIssueId = issueId ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = issueRepository.assignTechnicianToIssue(currentIssueId, technician.user_id)
                
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    if (result.isSuccess) {
                        Toast.makeText(this@ChooseTechnicianActivity, "Technician assigned successfully", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        Toast.makeText(this@ChooseTechnicianActivity, "Failed to assign technician", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Log.e("ChooseTechnicianActivity", "Error assigning technician", e)
                    Toast.makeText(this@ChooseTechnicianActivity, "Error assigning technician", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.techniciansRecyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }
}