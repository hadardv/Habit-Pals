package com.example.habitpals

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.habitpals.fragments.FeedFragment
import com.example.habitpals.fragments.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // The default fragment is the feed
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FeedFragment()).commit()

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_feed -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FeedFragment()).commit()
                    true
                }
                R.id.nav_profile -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, ProfileFragment()).commit()
                    true
                }
                else -> false
            }
        }
    }
}
