package com.example.reminderappsp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.room.Room
import com.example.reminderappsp.db.AppDatabase
import kotlinx.android.synthetic.main.activity_edit_reminder.btnCancel
import kotlinx.android.synthetic.main.activity_edit_reminder.btnSubmit
import kotlinx.android.synthetic.main.activity_edit_reminder.etDate
import kotlinx.android.synthetic.main.activity_edit_reminder.etLocationX
import kotlinx.android.synthetic.main.activity_edit_reminder.etLocationY
import kotlinx.android.synthetic.main.activity_edit_reminder.etTitle
import kotlinx.android.synthetic.main.activity_edit_reminder.tvReminderError
import java.time.LocalDate

class EditReminderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_reminder)

        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "reminderDB").allowMainThreadQueries().build()
        val reminderDao = db.reminderDao()
        val id = intent.getIntExtra("id", 0)
        val selectedReminder = reminderDao.getReminderInfo(id)

        etTitle.setText(selectedReminder.title)
        etDate.setText(selectedReminder.date)
        etLocationX.setText(selectedReminder.location_x)
        etLocationY.setText(selectedReminder.location_y)

        btnSubmit.setOnClickListener {
            selectedReminder.title = etTitle.text.toString()
            selectedReminder.date = etDate.text.toString()
            selectedReminder.location_x = etLocationX.text.toString()
            selectedReminder.location_y = etLocationY.text.toString()
            selectedReminder.creation_time = LocalDate.now().toString()

            if (selectedReminder.title.isNotEmpty() && selectedReminder.date.isNotEmpty()) {
                reminderDao.update(selectedReminder)
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