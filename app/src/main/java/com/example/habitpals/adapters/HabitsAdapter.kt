package com.example.habitpals.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.habitpals.R
import com.example.habitpals.models.Habit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HabitsAdapter(
    private val habitsList: List<Habit>,
    private val isCurrentUser: Boolean
) : RecyclerView.Adapter<HabitsAdapter.HabitViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()


    class HabitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val habitTitle: TextView = view.findViewById(R.id.habit_title)
        val progressBar: ProgressBar = view.findViewById(R.id.habit_progress)
        val progressText: TextView = view.findViewById(R.id.progress_text)
        val addButton: ImageView = view.findViewById(R.id.add_progress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habitsList[position]
        holder.habitTitle.text = habit.title
        holder.progressBar.max = habit.duration
        holder.progressBar.progress = habit.progress
        holder.progressText.text = "${habit.progress}/${habit.duration}"

        holder.addButton.visibility = if (isCurrentUser) View.VISIBLE else View.GONE


        holder.addButton.setOnClickListener {
            if(habit.progress < habit.duration) {
                habit.progress += 1
                holder.progressBar.progress = habit.progress
                holder.progressText.text = "${habit.progress}/${habit.duration}"

                updateProgressInFirestore(habit, position)
                if (habit.progress == habit.duration) {
                    Toast.makeText(holder.itemView.context, "Habit completed!", Toast.LENGTH_SHORT).show()
                    addCompletedHabitToFeed(habit.title)
                }
            }else {
                Toast.makeText(holder.itemView.context, "Habit already completed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addCompletedHabitToFeed(habitName: String) {
        val userId = auth.currentUser?.uid ?: return

        // Fetch the user's name (or use a cached value)
        fetchUserName(userId) { userName ->
            val feedUpdate = hashMapOf(
                "type" to "completed_habit",
                "habitName" to habitName,
                "timestamp" to System.currentTimeMillis(),
                "friendName" to userName // Add the user's name
            )

            db.collection("users").document(userId)
                .collection("feed")
                .add(feedUpdate)
                .addOnSuccessListener {
                    Log.d("HabitsAdapter", "Feed update added for completed habit: $habitName")
                }
                .addOnFailureListener { e ->
                    Log.e("HabitsAdapter", "Error adding feed update: ${e.message}")
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

    @SuppressLint("SuspiciousIndentation")
    private fun updateProgressInFirestore(habit: Habit, position: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?:return
        val habitId = habit.habitId

            FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("habits")
            .document(habitId)
            .update("progress",habit.progress)
            .addOnSuccessListener {
                Log.d("HabitsAdapter", "Progress updated successfully")
            }
            .addOnFailureListener{ e ->
                Log.e("HabitsAdapter", "Error updating progress: ${e.message}")
            }

    }
    override fun getItemCount(): Int = habitsList.size
}