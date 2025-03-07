package com.example.habitpals.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.habitpals.R
import com.example.habitpals.models.Habit

class HabitsAdapter(private val habitsList: List<Habit>) : RecyclerView.Adapter<HabitsAdapter.HabitViewHolder>() {

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

        holder.addButton.setOnClickListener {
            // Increase progress logic here
        }
    }

    override fun getItemCount(): Int = habitsList.size
}