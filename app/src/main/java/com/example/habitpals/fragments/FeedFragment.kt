package com.example.habitpals.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.habitpals.R
import com.example.habitpals.adapters.FeedUpdateAdapter
import com.example.habitpals.adapters.FriendAdapter
import com.example.habitpals.models.FeedUpdate
import com.example.habitpals.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FieldValue

class FeedFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var feedUpdateAdapter: FeedUpdateAdapter
    private lateinit var friendAdapter: FriendAdapter
    private val feedUpdates = mutableListOf<FeedUpdate>()
    private val searchResults = mutableListOf<User>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerViews
        val feedRecyclerView = view.findViewById<RecyclerView>(R.id.feed_recycler)
        val searchRecyclerView = view.findViewById<RecyclerView>(R.id.search_results_recycler)

        // Set up LayoutManagers
        feedRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        searchRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Set up Adapters
        feedUpdateAdapter = FeedUpdateAdapter(feedUpdates)
        friendAdapter = FriendAdapter(searchResults) { userId ->
            addFriend(userId) // Handle "Add Friend" button click
        }

        // Bind Adapters to RecyclerViews
        feedRecyclerView.adapter = feedUpdateAdapter
        searchRecyclerView.adapter = friendAdapter

        // Fetch friend updates
        fetchFriendUpdates()

        // Set up SearchView listener
        val searchView = view.findViewById<androidx.appcompat.widget.SearchView>(R.id.search_friends)
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    searchFriends(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrEmpty()) {
                    searchFriends(newText)
                }
                return true
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchFriendUpdates() {
        val userId = auth.currentUser?.uid ?: return

        // Get the user's friends
        db.collection("users").document(userId)
            .collection("friends")
            .get()
            .addOnSuccessListener { friends ->
                for (friend in friends) {
                    val friendId = friend.getString("friendId") ?: ""
                    // Fetch the updated from the friend's feed collection
                    db.collection("users").document(friendId)
                        .collection("feed")
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .get()
                        .addOnSuccessListener { feedDocuments ->
                            for (document in feedDocuments) {
                                val type = document.getString("type") ?: ""
                                val habitName = document.getString("habitName") ?: ""
                                val timestamp = document.getLong("timestamp") ?: 0
                                val friendName = document.getString("friendName") ?: ""
                                val profilePicture = document.getString("profilePicture") ?: ""
                                feedUpdates.add(FeedUpdate(friendId, friendName, type, habitName, timestamp,profilePicture))
                            }
                            // Update the adapter
                            feedUpdateAdapter.notifyDataSetChanged()
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("FeedFragment", "Error fetching friend updates: ${e.message}")
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun searchFriends(query: String) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users")
            .whereGreaterThanOrEqualTo("name", query) // Case-sensitive search
            .whereLessThanOrEqualTo("name", query + "\uf8ff") // Match partial names
            .get()
            .addOnSuccessListener { documents ->
                searchResults.clear()
                for (document in documents) {
                    val name = document.getString("name") ?: ""
                    val profilePicture = document.getString("profilePicture") ?: ""
                    val userId = document.id
                    searchResults.add(User(userId, name, profilePicture))
                }
                // Update the adapter
                friendAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("FeedFragment", "Error searching friends: ${e.message}")
            }
    }

    private fun addFriend(friendId: String) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .collection("friends")
            .document(friendId)
            .set(mapOf("friendId" to friendId))
            .addOnSuccessListener {
                db.collection("users").document(userId)
                    .update("friends", FieldValue.arrayUnion(friendId))
                    .addOnSuccessListener {
                        // Increment the friendsCount field
                        db.collection("users").document(userId)
                            .update("friendsCount", FieldValue.increment(1))
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Friend added!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Failed to update friends count: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Failed to update friends array: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to add friend: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}