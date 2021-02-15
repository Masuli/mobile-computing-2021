package com.example.reminderappsp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.room.Room
import com.example.reminderappsp.db.AppDatabase
import com.example.reminderappsp.db.ReminderInfo
import kotlinx.android.synthetic.main.activity_add_reminder.*
import java.time.LocalDate

class AddReminderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_reminder)

        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "reminderDB").allowMainThreadQueries().build()
        val reminderDao = db.reminderDao()
        val username = intent.getStringExtra("creator_id")

        btnSubmit.setOnClickListener {
            val reminderTitle = etTitle.text.toString()
            val reminderDate = etDate.text.toString()
            val locationX = etLocationX.text.toString()
            val locationY = etLocationY.text.toString()
            if (reminderTitle.isNotEmpty() && reminderDate.isNotEmpty()) {
                val reminder = ReminderInfo(null, reminderTitle, reminderDate, locationX, locationY, LocalDate.now().toString(), username, false)
                reminderDao.insert(reminder)
                finish()
            }
            else {
                tvReminderError.text = getString(R.string.invalid_reminder)
            }
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }
}