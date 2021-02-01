package com.example.reminderappsp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {
    private lateinit var reminderAdapter: ReminderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

       tvUsername.text = intent.getStringExtra("Username")

        reminderAdapter = ReminderAdapter(mutableListOf())
        rvReminders.adapter = reminderAdapter
        rvReminders.layoutManager = LinearLayoutManager(this)

        btnNewReminder.setOnClickListener {
            val reminderTitle = etReminderTitle.text.toString()
            val reminderDate = etReminderDate.text.toString()
            if (reminderTitle.isNotEmpty() && reminderDate.isNotEmpty()) {
                val reminder = Reminder(reminderTitle, reminderDate)
                reminderAdapter.addReminder(reminder)
                tvReminderError.text = ""
                etReminderTitle.setText("")
                etReminderDate.setText("")
            }
            else {
                tvReminderError.text = getString(R.string.invalid_reminder)
            }
        }

        btnDelete.setOnClickListener {
            reminderAdapter.deleteReminders()
        }

        tvLogout.setOnClickListener {
            finish()
        }
    }
}