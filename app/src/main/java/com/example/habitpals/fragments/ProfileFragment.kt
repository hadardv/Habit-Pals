package com.example.habitpals.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import coil.load
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.habitpals.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = auth.currentUser?.uid ?: return

        val nameTextView = view.findViewById<TextView>(R.id.profile_name)
        val friendsTextView = view.findViewById<TextView>(R.id.profile_friends)
        val profileImageView = view.findViewById<ImageView>(R.id.profile_image)

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if(document.exists()) {
                    val name = document.getString("name") ?: "User"
                    val friendsCount = document.getLong("friendsCount") ?: 0
                    val profileImage = document.getString("profilePicture")

                    nameTextView.text = name
                    friendsTextView.text = "$friendsCount Friends"

                    profileImageView.load(profileImage) {
                        crossfade(true)
                        placeholder(R.drawable.ic_profile) // Optional placeholder
                    }
                }
            }.addOnFailureListener{e -> nameTextView.text = "Error loading name"}
    }
}

