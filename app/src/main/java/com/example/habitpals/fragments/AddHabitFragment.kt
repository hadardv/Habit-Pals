package com.example.habitpals.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.habitpals.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddHabitFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_habit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val habitNameInput = view.findViewById<EditText>(R.id.habit_name_input)
        val habitDurationInput = view.findViewById<EditText>(R.id.habit_duration_input)
        val saveHabitButton = view.findViewById<Button>(R.id.btn_save_habit)

//        saveHabitButton.setOnClickListener{
//            val userId = auth.currentUser?.uid ?: return@setOnClickListener
//            val habitName = habitNameInput.text.toString().trim()
//            val habitDuration = habitDurationInput.text.toString().trim()
//
//            if(habitName.isEmpty() || habitDuration.isEmpty()) {
//                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            val habitData = mapOf(
//                "name" to habitName,
//                "duration" to habitDuration.toInt() // Ensure it's stored as Int
//            )
//
//            db.collection("users").document(userId)
//                .collection("habits")
//                .add(habitData)
//                .addOnSuccessListener {
//                    Toast.makeText(requireContext(), "Habit Added!", Toast.LENGTH_SHORT).show()
//                    parentFragmentManager.popBackStack()
//                }
//                .addOnFailureListener{
//                    Toast.makeText(requireContext(), "Failed to add habit", Toast.LENGTH_SHORT).show()
//                }
//        }

        saveHabitButton.setOnClickListener {
            val userId = auth.currentUser?.uid ?: run {
                Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val habitName = habitNameInput.text.toString().trim()
            val habitDuration = habitDurationInput.text.toString().trim()

            if (habitName.isEmpty() || habitDuration.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val habitData = mapOf(
                "name" to habitName,
                "duration" to habitDuration.toInt() // Ensure it's stored as Int
            )

            db.collection("users").document(userId)
                .collection("habits")
                .add(habitData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Habit Added!", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to add habit: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

    }
}