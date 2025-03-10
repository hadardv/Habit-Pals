package com.example.habitpals.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.habitpals.R
import com.example.habitpals.models.User

class FriendAdapter(
    private val friends: List<User>,
    private val onAddFriendClick: (userId: String) -> Unit,
    private val onProfileClick: (userId: String) -> Unit
) : RecyclerView.Adapter<FriendAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.friend_profile_image)
        val friendName: TextView = view.findViewById(R.id.friend_name)
        val addFriendButton: Button = view.findViewById(R.id.btn_add_friend)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val friend = friends[position]
        holder.friendName.text = friend.name
        holder.profileImage.load(friend.profilePicture) {
            crossfade(true)
            placeholder(R.drawable.ic_profile) // Default profile picture
        }
        holder.addFriendButton.setOnClickListener {
            onAddFriendClick(friend.userId) // Trigger the callback when the button is clicked
        }

        holder.itemView.setOnClickListener {
            onProfileClick(friend.userId)
        }

    }

    override fun getItemCount(): Int = friends.size
}