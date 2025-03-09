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
    private val onAddFriendClick: (userId: String) -> Unit // Callback for the "Add Friend" button
) : RecyclerView.Adapter<FriendAdapter.ViewHolder>() {

    // ViewHolder for Friend items
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.friend_profile_image)
        val friendName: TextView = view.findViewById(R.id.friend_name)
        val addFriendButton: Button = view.findViewById(R.id.btn_add_friend)
    }

    // Inflate the layout for each item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)
        return ViewHolder(view)
    }

    // Bind data to the views
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
    }

    // Return the number of items in the list
    override fun getItemCount(): Int = friends.size
}