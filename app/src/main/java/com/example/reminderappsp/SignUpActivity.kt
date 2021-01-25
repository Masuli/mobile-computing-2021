package com.example.reminderappsp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val sharedPref = getSharedPreferences("users", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        val etUsername = findViewById<EditText>(R.id.etSignUpUsername)
        val etPassword = findViewById<EditText>(R.id.etSignUpPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etSignUpConfirmPassword)
        val tvPasswordError = findViewById<TextView>(R.id.tvPasswordMismatch)
        val tvUsernameError = findViewById<TextView>(R.id.tvUsernameTaken)

        findViewById<Button>(R.id.btnSignUpSubmit).setOnClickListener {
            val chosenUsername = etUsername.text.toString()
            val chosenPassword = etPassword.text.toString()
            val chosenConfirmPassword = etConfirmPassword.text.toString()

            val checkName = sharedPref.getString(chosenUsername, null)

            if (checkName != null) {
                tvUsernameError.text = "This username is not available."
            }
            else if (chosenUsername == "") {
                tvUsernameError.text = "Username field is empty."
            }
            else {
                tvUsernameError.text = ""
            }

            if (chosenPassword != chosenConfirmPassword) {
                tvPasswordError.text = "Passwords do not match."
                etPassword.setText("")
                etConfirmPassword.setText("")
            }
            else if (chosenPassword == "") {
                tvPasswordError.text = "Password field is empty."
            }
            else {
                tvPasswordError.text = ""
            }

            if (chosenPassword == chosenConfirmPassword && checkName == null && chosenUsername != "" && chosenPassword != "") {
                editor.apply {
                    putString(chosenUsername, chosenPassword)
                    apply()
                    finish()
                }
            }
        }
    }
}