package com.example.reminderappsp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val sharedPref = getSharedPreferences("users", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        btnSignUpSubmit.setOnClickListener {
            val chosenUsername = etSignUpUsername.text.toString()
            val chosenPassword = etSignUpPassword.text.toString()
            val chosenConfirmPassword = etSignUpConfirmPassword.text.toString()

            val checkName = sharedPref.getString(chosenUsername, null)

            if (checkName != null) {
                tvUsernameError.text = getString(R.string.username_not_available)
            }
            else if (chosenUsername.isEmpty()) {
                tvUsernameError.text = getString(R.string.empty_username)
            }
            else {
                tvUsernameError.text = ""
            }

            if (chosenPassword != chosenConfirmPassword) {
                tvPasswordError.text = getString(R.string.password_mismatch)
                etSignUpPassword.setText("")
                etSignUpConfirmPassword.setText("")
            }
            else if (chosenPassword.isEmpty()) {
                tvPasswordError.text = getString(R.string.empty_password)
            }
            else {
                tvPasswordError.text = ""
            }

            if (chosenPassword == chosenConfirmPassword && checkName == null && chosenUsername.isNotEmpty() && chosenPassword.isNotEmpty()) {
                editor.apply {
                    putString(chosenUsername, chosenPassword)
                    apply()
                    finish()
                }
            }
        }
    }
}