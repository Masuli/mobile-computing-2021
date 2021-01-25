package com.example.reminderappsp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val tvUsername = findViewById<TextView>(R.id.tvUsername)
        val tvProfile = findViewById<TextView>(R.id.tvProfile)
        val tvLogout = findViewById<TextView>(R.id.tvLogout)
        val username = intent.getStringExtra("Username")

        tvProfile.text = "Logged in as:"
        tvUsername.text = username
        tvLogout.text = "LOGOUT"

        tvLogout.setOnClickListener {
            finish()
        }
    }
}