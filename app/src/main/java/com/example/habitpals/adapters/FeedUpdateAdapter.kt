package com.example.habitpals.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.habitpals.R
import com.example.habitpals.models.FeedUpdate

class FeedUpdateAdapter(private val updates: List<FeedUpdate>) : RecyclerView.Adapter<FeedUpdateAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val friendProfile: ImageView = view.findViewById(R.id.friend_profile)
        val friendName: TextView = view.findViewById(R.id.friend_name)
        val updateMessage: TextView = view.findViewById(R.id.update_message)
        val updateTimestamp: TextView = view.findViewById(R.id.update_timestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_feed_update, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val update = updates[position]
        holder.friendName.text = update.friendName
        holder.updateMessage.text = when (update.type) {
            "added_habit" -> "added a new habit: ${update.habitName}"
            "completed_habit" -> "completed a habit: ${update.habitName}"
            else -> ""
        }
        holder.updateTimestamp.text = formatTimestamp(update.timestamp)

        holder.friendProfile.load(update.profilePicture) {
            crossfade(true)
            placeholder(R.drawable.placeholder_image)
        }
    }

    override fun getItemCount(): Int = updates.size

    private fun formatTimestamp(timestamp: Long): String {
        val currentTime = System.currentTimeMillis()
        val timeDifference = currentTime - timestamp

        return when {
            timeDifference < 60_000 -> "${timeDifference / 1000} seconds ago"
            timeDifference < 3_600_000 -> "${timeDifference / 60_000} minutes ago"
            timeDifference < 86_400_000 -> "${timeDifference / 3_600_000} hours ago"
            else -> "${timeDifference / 86_400_000} days ago"
        }
    }
}