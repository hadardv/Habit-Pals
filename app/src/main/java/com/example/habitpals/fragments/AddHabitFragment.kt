package com.example.habitpals.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowId
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
                "duration" to habitDuration.toInt() ,
                "progress" to 0
            )

            db.collection("users").document(userId)
                .collection("habits")
                .add(habitData)
                .addOnSuccessListener {
                    addHabitToFeed(userId,habitName)
                    Toast.makeText(requireContext(), "Habit Added!", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to add habit: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

    }

    private fun fetchUserName(userId: String, onSuccess: (String) -> Unit) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: "Unknown User"
                    onSuccess(name)
                } else {
                    Log.e("FetchUserName", "User document does not exist")
                    onSuccess("Unknown User")
                }
            }
            .addOnFailureListener { e ->
                Log.e("FetchUserName", "Error fetching user name: ${e.message}")
                onSuccess("Unknown User")
            }
    }

    private fun addHabitToFeed(userId: String, habitName: String) {
        // Fetch the user's name and profile picture
        fetchUserProfile(userId) { userName, profilePicture ->
            val feedUpdate = hashMapOf(
                "type" to "added_habit",
                "habitName" to habitName,
                "timestamp" to System.currentTimeMillis(),
                "friendName" to userName, // Add the user's name
                "profilePicture" to profilePicture // Add the user's profile picture
            )

            db.collection("users").document(userId)
                .collection("feed")
                .add(feedUpdate)
                .addOnSuccessListener {
                    Log.d("AddHabitFragment", "Feed update added for new habit: $habitName")
                }
                .addOnFailureListener { e ->
                    Log.e("AddHabitFragment", "Error adding feed update: ${e.message}")
                }
        }
    }
    // Function to fetch the user's name and profile picture
    private fun fetchUserProfile(userId: String, onSuccess: (String, String) -> Unit) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: "Unknown User"
                    val profilePicture = document.getString("profilePicture") ?: "" // Fetch the profile picture
                    onSuccess(name, profilePicture)
                } else {
                    Log.e("FetchUserProfile", "User document does not exist")
                    onSuccess("Unknown User", "") // Default values
                }
            }
            .addOnFailureListener { e ->
                Log.e("FetchUserProfile", "Error fetching user profile: ${e.message}")
                onSuccess("Unknown User", "") // Default values
            }
    }

}