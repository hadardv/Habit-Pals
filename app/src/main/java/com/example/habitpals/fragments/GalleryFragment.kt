package com.example.habitpals.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.habitpals.R
import com.example.habitpals.adapters.GalleryAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class GalleryFragment : Fragment() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uploadPhotoButton = view.findViewById<Button>(R.id.btn_upload_photo)
        val recyclerView = view.findViewById<RecyclerView>(R.id.gallery_recycler)
        progressBar = view.findViewById(R.id.progress_bar)

        recyclerView.layoutManager = GridLayoutManager(requireContext(),3)
        loadGalleryImages()

        uploadPhotoButton.setOnClickListener {
            openGallery()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePicker.launch(intent)
    }

    private val imagePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            if(imageUri != null){
                uploadImageToFirebase(imageUri)
            }
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val userId = auth.currentUser?.uid ?: return
        val fileName = UUID.randomUUID().toString() + ".jpg"
        val storageRef = storage.reference.child("gallery/$userId/$fileName")

        progressBar.visibility = View.VISIBLE

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    saveImageUrlToFirestore(uri.toString())
                }
            }
            .addOnFailureListener{
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveImageUrlToFirestore(imageUrl: String) {
        val userId = auth.currentUser?.uid ?: return
        val galleryRef = db.collection("users").document(userId).collection("gallery")

        val imageData = mapOf(
            "imageUrl" to imageUrl,
            "timestamp" to System.currentTimeMillis()
        )

        galleryRef.add(imageData)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Image uploaded!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener{
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Failed to save image", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadGalleryImages() {
        val userId = auth.currentUser?.uid ?: return
        val galleryRef = db.collection("users").document(userId).collection("gallery")

        galleryRef.orderBy("timestamp").get().addOnSuccessListener { documents ->
            val imageUrls = documents.mapNotNull { it.getString("imageUrl") }.toMutableList() // Convert to MutableList

            val adapter = GalleryAdapter(imageUrls)
            val recyclerView = view?.findViewById<RecyclerView>(R.id.gallery_recycler)
            recyclerView?.adapter = adapter
        }
    }


}