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
        Log.d("FeedFragment", "onViewCreated called")

        val feedRecyclerView = view.findViewById<RecyclerView>(R.id.feed_recycler)
        val searchRecyclerView = view.findViewById<RecyclerView>(R.id.search_results_recycler)

        feedRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        searchRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        feedUpdateAdapter = FeedUpdateAdapter(feedUpdates) { feedUpdate ->
            handleLikeClick(feedUpdate)
        }
        friendAdapter = FriendAdapter(
            searchResults,
            { userId -> addFriend(userId) },
            { userId -> navigateToProfile(userId) }
        )

        feedRecyclerView.adapter = feedUpdateAdapter
        searchRecyclerView.adapter = friendAdapter

        Log.d("FeedFragment", "Calling fetchFriendUpdates")
        fetchFriendUpdates()

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

    private fun navigateToProfile(userId: String) {
        val profileFragment = ProfileFragment.newInstance(userId)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, profileFragment)
            .addToBackStack(null)
            .commit()
    }


    private fun handleLikeClick(feedUpdate: FeedUpdate) {
        val userId = auth.currentUser?.uid ?: return
        val friendId = feedUpdate.friendId
        val feedDocumentId = feedUpdate.feedDocumentId

        db.collection("users").document(friendId)
            .collection("feed").document(feedDocumentId)
            .collection("likes")
            .document(userId)
            .set(mapOf(
                "userId" to userId,
                "timestamp" to System.currentTimeMillis()
            ))
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Liked!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to like: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchFriendUpdates() {
        val userId = auth.currentUser?.uid ?: return
        feedUpdates.clear()


        db.collection("users").document(userId)
            .collection("friends")
            .get()
            .addOnSuccessListener { friends ->
                for (friend in friends) {
                    val friendId = friend.getString("friendId") ?: ""
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
                                val feedDocumentId = document.id

                                db.collection("users").document(friendId)
                                    .collection("feed").document(feedDocumentId)
                                    .collection("likes")
                                    .get()
                                    .addOnSuccessListener { likesDocuments ->
                                        val likes = likesDocuments.size()
                                        feedUpdates.add(FeedUpdate(friendId, friendName, type, habitName, timestamp, profilePicture, likes, feedDocumentId))
                                        feedUpdateAdapter.notifyDataSetChanged()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("FeedFragment", "Error fetching likes: ${e.message}")
                                        feedUpdates.add(FeedUpdate(friendId, friendName, type, habitName, timestamp, profilePicture, 0, feedDocumentId))
                                        feedUpdateAdapter.notifyDataSetChanged()
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("FeedFragment", "Error fetching feed updates: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("FeedFragment", "Error fetching friends: ${e.message}")
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun searchFriends(query: String) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users")
            .whereGreaterThanOrEqualTo("name", query)
            .whereLessThanOrEqualTo("name", query + "\uf8ff")
            .get()
            .addOnSuccessListener { documents ->
                searchResults.clear()
                for (document in documents) {
                    val name = document.getString("name") ?: ""
                    val profilePicture = document.getString("profilePicture") ?: ""
                    val userId = document.id
                    searchResults.add(User(userId, name, profilePicture))
                }
                friendAdapter.notifyDataSetChanged()
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