package com.example.habitpals.profile
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.habitpals.MainActivity
import com.example.habitpals.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

import androidx.appcompat.app.AppCompatActivity
import java.io.File

class ProfileSetupActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var profileImageView: ImageView
    private lateinit var uploadButton: Button
    private lateinit var saveButton: Button

    private var selectedImageUri: Uri? = null
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            profileImageView.setImageURI(uri)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profilesetup)

        nameEditText = findViewById(R.id.name_edit_text)
        profileImageView = findViewById(R.id.profile_image_view)
        uploadButton = findViewById(R.id.btn_upload)
        saveButton = findViewById(R.id.btn_save)

        uploadButton.setOnClickListener {
            selectProfileImage()
        }

        saveButton.setOnClickListener {
            saveUserProfile()
        }
    }

    private fun selectProfileImage() {
        imagePickerLauncher.launch("image/*")
    }

    private fun saveUserProfile() {
        val name = nameEditText.text.toString().trim()
        val userId = auth.currentUser?.uid ?: return

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImageUri != null) {
            uploadProfileImage(userId, name)
        } else {
            updateUserProfile(userId, name, "")
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            return
        }
    }

    private fun uploadProfileImage(userId: String, name: String) {
        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            return
        }

//        val storageRef = FirebaseStorage.getInstance().reference.child("profile_pictures/$userId.jpg")
//        Log.d("FirebaseStorage", "Saving image to path: $storagePath")

        val storagePath = "profile_pictures/$userId.jpg"
        Log.d("FirebaseStorage", "Saving image to path: $storagePath")

        val storageRef = storage.reference.child(storagePath)

        contentResolver.openInputStream(selectedImageUri!!)?.use { inputStream ->
            val tempFile = File.createTempFile("upload", ".jpg", cacheDir)
            tempFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            val fileUri = Uri.fromFile(tempFile)

            storageRef.putFile(fileUri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        updateUserProfile(userId, name, uri.toString())
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("FirebaseStorage", "Upload failed: ${e.message}")
                    Toast.makeText(this, "Image upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
        Log.d("FirebaseAuth", "User UID: ${FirebaseAuth.getInstance().currentUser?.uid}")
        Log.d("FirebaseStorage", "Uploading to: profile_pictures/$userId.jpg")


    }


    private fun updateUserProfile(userId: String, name: String, profilePictureUrl: String) {
        val userData = mapOf(
            "name" to name,
            "profilePicture" to profilePictureUrl
        )

        db.collection("users").document(userId)
            .update(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Profile update failed", Toast.LENGTH_SHORT).show()
            }
    }

}