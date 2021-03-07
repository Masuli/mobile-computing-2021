package com.example.reminderappsp

import android.app.DatePickerDialog
import android.content.Intent
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.room.Room
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.reminderappsp.db.AppDatabase
import com.example.reminderappsp.db.ReminderInfo
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_add_reminder.*
import java.time.LocalDate
import java.util.*
import java.util.concurrent.TimeUnit

class AddReminderActivity : AppCompatActivity() {
    companion object {
        const val workerKey = "workerKey"
    }

    private lateinit var ref: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_reminder)

        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "reminderDB").allowMainThreadQueries().build()
        val reminderDao = db.reminderDao()
        val username = intent.getStringExtra("creator_id")

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val integerChars = '0'..'9'

        ref = FirebaseDatabase.getInstance().getReference("locations")
        val locationListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val latestLocation = snapshot.children.last().getValue<LocationInfo>()
                    if (latestLocation != null) {
                        tvLocationX.text = "${latestLocation.lat}, ${latestLocation.lng}"
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("myTag", "onCancelled called")
            }
        }
        ref.addValueEventListener(locationListener)

        tvDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(this, { _, mYear, mMonth, mDay ->
                tvDate.text = "" + mDay + "." + (mMonth + 1) + "." + mYear
            }, year, month, day)
            datePickerDialog.show()
        }

        tvLocationX.setOnClickListener {
            val meters = etMeters.text.toString()
            if (meters.isEmpty()) {
                tvReminderError.text = "Enter the meter range before selecting location."
            }
            else if (!isInteger(meters, integerChars)) {
                tvReminderError.text = "Meters must be an integer."
            }
            else {
                val intent = Intent(applicationContext, MapsActivity::class.java)
                intent.putExtra("radius", meters)
                startActivity(intent)
            }
        }

        btnSubmit.setOnClickListener {
            val reminderTitle = etTitle.text.toString()
            val reminderDate = tvDate.text.toString()
            val meters = etMeters.text.toString()
            val locationX = tvLocationX.text.toString()
            val locationY = etLocationY.text.toString()

            if (reminderDate.isNotEmpty()) {
                val dateParts = reminderDate.split(".").toTypedArray()
                val gCalendar = GregorianCalendar(dateParts[2].toInt(), dateParts[1].toInt() - 1, dateParts[0].toInt())
                if (reminderTitle.isNotEmpty() && gCalendar.timeInMillis > Calendar.getInstance().timeInMillis) {
                    val notificationId = setOneTimeWorkRequest(gCalendar.timeInMillis - Calendar.getInstance().timeInMillis, reminderTitle)
                    val reminder = ReminderInfo(null, reminderTitle, reminderDate, locationX, locationY, LocalDate.now().toString(), username, false, notificationId.toString(), meters)
                    reminderDao.insert(reminder)
                    finish()
                }
                else if (reminderDate.isNotEmpty() && gCalendar.timeInMillis < Calendar.getInstance().timeInMillis) {
                    tvReminderError.text = getString(R.string.future_reminder)
                }
            }
            else {
                tvReminderError.text = getString(R.string.invalid_reminder)
            }
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun setOneTimeWorkRequest(delay: Long, title: String): UUID {
        val data = Data.Builder().putString(workerKey, title).build()
        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(ReminderWorker::class.java)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS).setInputData(data).build()
        WorkManager.getInstance(applicationContext).enqueue(oneTimeWorkRequest)
        return oneTimeWorkRequest.id
    }

    private fun isInteger(input: String, integerChars: CharRange) = input.all { it in integerChars }
}