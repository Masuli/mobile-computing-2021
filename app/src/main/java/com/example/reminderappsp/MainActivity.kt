package com.example.reminderappsp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPref = getSharedPreferences("users", Context.MODE_PRIVATE)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()
            val checkPassword = sharedPref.getString(username, null)

            if (password.isEmpty() && username.isEmpty()) {
                tvLoginError.text = getString(R.string.empty_username_and_password)
            }
            else if (username.isEmpty()) {
                tvLoginError.text = getString(R.string.empty_username)
            }
            else if (password.isEmpty()) {
                tvLoginError.text = getString(R.string.empty_password)
            }
            else if (password == checkPassword) {
                tvLoginError.text = ""
                val intent = Intent(applicationContext, ProfileActivity::class.java)
                intent.putExtra("Username", username)
                startActivity(intent)
            }
            else if (checkPassword == null && username.isNotEmpty()) {
                tvLoginError.text = getString(R.string.invalid_login)
            }
            else {
                tvLoginError.text = getString(R.string.invalid_login)
            }
        }

        btnSignUp.setOnClickListener {
            //Log.d("Lab", "Login Clicked")
            startActivity(Intent(applicationContext, SignUpActivity::class.java))
        }
    }
}