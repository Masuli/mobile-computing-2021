package com.example.reminderappsp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.reminderappsp.db.AppDatabase
import com.example.reminderappsp.db.ReminderInfo
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

        var manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var listener = object : LocationListener {
            override fun onLocationChanged(location: Location?) {
            }
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            }
            override fun onProviderEnabled(provider: String?) {
            }
            override fun onProviderDisabled(provider: String?) {
            }
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, listener)

        username = intent.getStringExtra("username")!!
        tvUsername.text = username

        val remindersTemp = reminderDao.getReminderInfos(username)
        val reminders = mutableListOf<ReminderInfo>().apply {addAll(remindersTemp)}
        val workManager = WorkManager.getInstance(applicationContext)
        for (reminder in remindersTemp) {
            val workInfo = workManager.getWorkInfoById(UUID.fromString(reminder.notification_id)).get()
            if (workInfo.state != WorkInfo.State.SUCCEEDED && workInfo.state != WorkInfo.State.CANCELLED) {
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
        val remindersTemp = reminderDao.getReminderInfos(username)
        val reminders = mutableListOf<ReminderInfo>().apply {addAll(remindersTemp)}
        val workManager = WorkManager.getInstance(applicationContext)
        for (reminder in remindersTemp) {
            val workInfo = workManager.getWorkInfoById(UUID.fromString(reminder.notification_id)).get()
            if (workInfo.state != WorkInfo.State.SUCCEEDED && workInfo.state != WorkInfo.State.CANCELLED) {
                reminders.remove(reminder)
            }
        }
        reminderAdapter = ReminderAdapter(reminders)
        rvReminders.adapter = reminderAdapter
        rvReminders.layoutManager = LinearLayoutManager(this)
        tvEditError.text = ""
    }
}