package com.example.habitpals.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.habitpals.R
import com.example.habitpals.profile.ProfileSetupActivity
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {
    private lateinit var auth : FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var email_edit_text : EditText
    private lateinit var password_edit_text : EditText
    private lateinit var btn_signup : Button
    private lateinit var txt_login : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        Log.d("FirestoreCheck", "Firestore instance: $db")


        email_edit_text = findViewById(R.id.email_edit_text)
        password_edit_text = findViewById(R.id.password_edit_text)
        btn_signup = findViewById(R.id.btn_signup)
        txt_login = findViewById(R.id.txt_login)

        txt_login.setOnClickListener {
            val intent = Intent(this@SignupActivity, LoginActivity::class.java)
            startActivity(intent)
        }



        btn_signup.setOnClickListener{
            val email = email_edit_text.text.toString().trim()
            val password = password_edit_text.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            } else if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            } else {
                registerUser(email, password)
            }
        }

    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener(this){ task ->
                if (task.isSuccessful){
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        // when a user signs up i store his data in the db with some empty fields for now
                        val defaultUserData = hashMapOf(
                            "name" to "", // empty field, the user will update it later
                            "email" to email,
                            "profilePicture" to "", //empty field, the user will upload a photo later
                            "friends" to listOf<String>()
                        )

                        db.collection("users").document(userId)
                            .set(defaultUserData)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Account created! Set up your profile",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val intent = Intent(this, ProfileSetupActivity::class.java)
                                intent.putExtra("USER_ID", userId)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Error saving user data: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        Toast.makeText(this, "Authentication failed. Please login again.", Toast.LENGTH_SHORT).show()
                        return@addOnCompleteListener
                    }
                } else {
                    Toast.makeText(this, "Sign-Up Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}