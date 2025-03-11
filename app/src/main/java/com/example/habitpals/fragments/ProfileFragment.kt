package com.example.habitpals.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import coil.load
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.habitpals.R
import com.example.habitpals.adapters.GalleryAdapter
import com.example.habitpals.adapters.HabitsAdapter
import com.example.habitpals.auth.LoginActivity
import com.example.habitpals.models.Habit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage

class ProfileFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var galleryAdapter: GalleryAdapter
    private val galleryImageUrls = mutableListOf<String>()
    private var userId: String? = null

    companion object {
        private const val USER_ID = "userId"

        fun newInstance(userId: String): ProfileFragment {
            val fragment = ProfileFragment()
            val args = Bundle()
            args.putString(USER_ID, userId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = arguments?.getString(USER_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val profileUserId = userId ?: auth.currentUser?.uid ?: return

        val nameTextView = view.findViewById<TextView>(R.id.profile_name)
        val friendsTextView = view.findViewById<TextView>(R.id.profile_friends)
        val profileImageView = view.findViewById<ImageView>(R.id.profile_image)
        val addHabitButton = view.findViewById<Button>(R.id.btn_add_habit)
        val galleryButton = view.findViewById<Button>(R.id.btn_gallery)
        val signOutButton = view.findViewById<Button>(R.id.btn_sign_out)



        val isCurrentUser = profileUserId == auth.currentUser?.uid
        val habitsRecyclerView = view.findViewById<RecyclerView>(R.id.habits_recycler)
        val habitsList = mutableListOf<Habit>()
        val habitsAdapter = HabitsAdapter(habitsList, isCurrentUser)
        habitsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        habitsRecyclerView.adapter = habitsAdapter

        val galleryRecyclerView = view.findViewById<RecyclerView>(R.id.gallery_recycler)
        galleryAdapter = GalleryAdapter(galleryImageUrls)
        galleryRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        galleryRecyclerView.adapter = galleryAdapter


        fetchGalleryImages(profileUserId)
        fetchHabits(profileUserId, habitsList, habitsAdapter)

        if (profileUserId == auth.currentUser?.uid) {
            addHabitButton.visibility = View.VISIBLE
            signOutButton.visibility = View.VISIBLE

            addHabitButton.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, AddHabitFragment())
                    .addToBackStack(null)
                    .commit()
            }
            signOutButton.setOnClickListener {
                signOut()
            }
            galleryButton.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, GalleryFragment())
                    .addToBackStack(null)
                    .commit()
            }
        } else {

            addHabitButton.visibility = View.GONE
            galleryButton.visibility = View.GONE
            signOutButton.visibility = View.GONE
        }

        db.collection("users").document(profileUserId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: "User"
                    val profileImage = document.getString("profilePicture")
                    val friends = document.get("friends") as? List<*> ?: emptyList<Any>()
                    val friendsCount = friends.size

                    nameTextView.text = name
                    friendsTextView.text = "$friendsCount Friends"

                    if (!profileImage.isNullOrEmpty()) {
                        profileImageView.load(profileImage) {
                            crossfade(true)
                            placeholder(R.drawable.ic_profile) // Fallback image
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("ProfileFragment", "Error fetching user data: ${e.message}")
            }

    }


    @SuppressLint("NotifyDataSetChanged")
    private fun fetchHabits(userId: String, habitsList: MutableList<Habit>, adapter: HabitsAdapter) {
        db.collection("users").document(userId).collection("habits")
            .get()
            .addOnSuccessListener { result ->
                habitsList.clear()
                for (document in result) {
                    val habitId = document.id
                    val name = document.getString("name") ?: "Unknown Habit"
                    val duration = document.getLong("duration")?.toInt() ?: 0
                    val progress = document.getLong("progress")?.toInt() ?: 0
                    val habit = Habit(habitId, name, duration, progress)
                    habitsList.add(habit)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("ProfileFragment", "Error fetching habits: ${e.message}")
            }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun fetchGalleryImages(userId: String) {
        db.collection("users").document(userId).collection("gallery")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(6)
            .get()
            .addOnSuccessListener { documents ->
                galleryImageUrls.clear()
                for (document in documents) {
                    val imageUrl = document.getString("imageUrl")
                    if (imageUrl != null) {
                        Log.d("ProfileFragment", "Fetched image URL: $imageUrl")
                        galleryImageUrls.add(imageUrl)
                    }
                }
                Log.d("ProfileFragment", "Total images fetched: ${galleryImageUrls.size}")
                galleryAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("ProfileFragment", "Error fetching gallery images: ${e.message}")
            }
    }

    private fun signOut() {
        auth.signOut()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

}

