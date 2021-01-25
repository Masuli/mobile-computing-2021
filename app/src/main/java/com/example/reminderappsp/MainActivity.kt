package com.example.reminderappsp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val tvLoginError = findViewById<TextView>(R.id.tvLoginError)

        val sharedPref = getSharedPreferences("users", Context.MODE_PRIVATE)

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()
            val checkPassword = sharedPref.getString(username, null)

            if (password == "" && username == "") {
                tvLoginError.text = "Password and username fields are empty."
            }
            else if (username == "") {
                tvLoginError.text = "Username field is empty."
            }
            else if (password == "") {
                tvLoginError.text = "Password field is empty."
            }
            else if (password == checkPassword) {
                tvLoginError.text = ""
                val intent = Intent(applicationContext, ProfileActivity::class.java)
                intent.putExtra("Username", username)
                startActivity(intent)
            }
            else if (checkPassword == null && username != "") {
                tvLoginError.text = "Invalid username or password."
            }
            else {
                tvLoginError.text = "Invalid username or password."
            }
        }

        findViewById<Button>(R.id.btnSignUp).setOnClickListener {
            //Log.d("Lab", "Login Clicked")
            startActivity(Intent(applicationContext, SignUpActivity::class.java))
        }
    }
}