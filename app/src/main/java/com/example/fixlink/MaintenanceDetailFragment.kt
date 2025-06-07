package com.example.fixlink

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.graphics.drawable.GradientDrawable
import android.graphics.Color

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MaintenanceDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MaintenanceDetailFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_maintenance_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val statusChip = view.findViewById<TextView>(R.id.statusChip)

        fun setChipColor(chip: TextView, color: Int) {
            val drawable = GradientDrawable()
            drawable.cornerRadius = 32f
            drawable.setColor(color)
            chip.background = drawable
        }

        // Exemplo: definir o status dinamicamente
        val status = "Pending" // Aqui deves buscar o status real da manutenção
        statusChip.text = status
        when (status.lowercase()) {
            "pending", "pendente" -> setChipColor(statusChip, Color.parseColor("#E0E0E0"))      // Pending: cinza claro
            "assigned", "atribuído", "atribuido" -> setChipColor(statusChip, Color.parseColor("#B3E5FC"))     // Assigned: azul claro
            "under repair", "em reparação", "em reparacao" -> setChipColor(statusChip, Color.parseColor("#E1E0F7"))  // Under Repair: lilás claro
            "resolved", "resolvido" -> setChipColor(statusChip, Color.parseColor("#66BB6A"))     // Resolved: verde
            else -> setChipColor(statusChip, Color.LTGRAY)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MaintenanceDetailFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MaintenanceDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}