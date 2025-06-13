package com.example.fixlink

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fixlink.adapters.RecentActivityFullAdapter
import com.example.fixlink.data.entities.*

class RecentActivityFullFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecentActivityFullAdapter

    companion object {
        fun newInstance(
            issues: List<Issue>,
            users: List<User>,
            equipments: List<Equipment>,
            locations: List<Location>
        ): RecentActivityFullFragment {
            return RecentActivityFullFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("issues", ArrayList(issues))
                    putSerializable("users", ArrayList(users))
                    putSerializable("equipments", ArrayList(equipments))
                    putSerializable("locations", ArrayList(locations))
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recent_activity_full, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the top app bar fragment and show back button
        (parentFragmentManager.findFragmentById(R.id.topAppBarFragmentContainer) as? TopAppBarFragment)?.apply {
            showBackButton()
        }

        recyclerView = view.findViewById(R.id.recentActivityRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Get data from arguments
        val issues = arguments?.getSerializable("issues") as? ArrayList<Issue> ?: arrayListOf()
        val users = arguments?.getSerializable("users") as? ArrayList<User> ?: arrayListOf()
        val equipments = arguments?.getSerializable("equipments") as? ArrayList<Equipment> ?: arrayListOf()
        val locations = arguments?.getSerializable("locations") as? ArrayList<Location> ?: arrayListOf()

        adapter = RecentActivityFullAdapter(issues, users, equipments, locations)
        recyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Hide back button when leaving this fragment
        (parentFragmentManager.findFragmentById(R.id.topAppBarFragmentContainer) as? TopAppBarFragment)?.hideBackButton()
    }
} 