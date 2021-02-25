package com.example.reminderappsp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.reminderappsp.db.AppDatabase
import kotlinx.android.synthetic.main.activity_profile.*
import java.util.*

class ProfileActivity : AppCompatActivity() {
    private lateinit var reminderAdapter: ReminderAdapter
    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "reminderDB").allowMainThreadQueries().build()
        val reminderDao = db.reminderDao()

        username = intent.getStringExtra("username")!!
        tvUsername.text = username

        val reminders = reminderDao.getReminderInfos(username)
        val workManager = WorkManager.getInstance(applicationContext)
        for (reminder in reminders) {
            val workInfo = workManager.getWorkInfoById(UUID.fromString(reminder.notification_id)).get()
            if (workInfo.state != WorkInfo.State.SUCCEEDED) {
                reminders.remove(reminder)
            }
        }
        reminderAdapter = ReminderAdapter(reminders)
        rvReminders.adapter = reminderAdapter
        rvReminders.layoutManager = LinearLayoutManager(this)

        btnNewReminder.setOnClickListener {
            val intent = Intent(applicationContext, AddReminderActivity::class.java)
            intent.putExtra("creator_id", username)
            startActivity(intent)
        }

        btnDelete.setOnClickListener {
            val toBeDeleted = reminderAdapter.deleteReminders()
            for (reminder in toBeDeleted) {
                reminderDao.delete(reminder.uid)
            }
        }

        btnEditReminder.setOnClickListener {
            val checkReminders = reminderAdapter.getCheckedReminders()
            val size = checkReminders.size
            if (size == 1) {
                val intent = Intent(applicationContext, EditReminderActivity::class.java)
                intent.putExtra("id", checkReminders[0])
                startActivity(intent)
            }
            else if (size == 0) {
                tvEditError.text = getString(R.string.select_reminder)
            }
            else if (size > 1) {
                tvEditError.text = getString(R.string.edit_one_reminder)
            }
        }

        tvLogout.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "reminderDB").allowMainThreadQueries().build()
        val reminderDao = db.reminderDao()
        val reminders = reminderDao.getReminderInfos(username)
        val workManager = WorkManager.getInstance(applicationContext)
        for (reminder in reminders) {
            val workInfo = workManager.getWorkInfoById(UUID.fromString(reminder.notification_id)).get()
            if (workInfo.state != WorkInfo.State.SUCCEEDED) {
                reminders.remove(reminder)
            }
        }
        reminderAdapter = ReminderAdapter(reminders)
        rvReminders.adapter = reminderAdapter
        rvReminders.layoutManager = LinearLayoutManager(this)
        tvEditError.text = ""
    }
}