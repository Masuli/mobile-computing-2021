package com.example.reminderappsp

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.room.Room
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.reminderappsp.db.AppDatabase
import kotlinx.android.synthetic.main.activity_add_reminder.*
import kotlinx.android.synthetic.main.activity_edit_reminder.btnCancel
import kotlinx.android.synthetic.main.activity_edit_reminder.btnSubmit
import kotlinx.android.synthetic.main.activity_edit_reminder.etLocationY
import kotlinx.android.synthetic.main.activity_edit_reminder.etTitle
import kotlinx.android.synthetic.main.activity_edit_reminder.tvDate
import kotlinx.android.synthetic.main.activity_edit_reminder.tvLocationX
import kotlinx.android.synthetic.main.activity_edit_reminder.tvReminderError
import java.time.LocalDate
import java.util.*
import java.util.concurrent.TimeUnit

class EditReminderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_reminder)

        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "reminderDB").allowMainThreadQueries().build()
        val reminderDao = db.reminderDao()
        val id = intent.getIntExtra("id", 0)
        val selectedReminder = reminderDao.getReminderInfo(id)

        etTitle.setText(selectedReminder.title)
        tvDate.text = selectedReminder.date
        tvLocationX.text = selectedReminder.location_x
        etLocationY.setText(selectedReminder.location_y)

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val integerChars = '0'..'9'

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
            selectedReminder.title = etTitle.text.toString()
            selectedReminder.date = tvDate.text.toString()
            selectedReminder.meters = etMeters.text.toString()
            selectedReminder.location_x = tvLocationX.text.toString()
            selectedReminder.location_y = etLocationY.text.toString()
            selectedReminder.creation_time = LocalDate.now().toString()

            val dateParts = selectedReminder.date.split(".").toTypedArray()
            val gCalendar = GregorianCalendar(dateParts[2].toInt(), dateParts[1].toInt() - 1, dateParts[0].toInt())

            if ((selectedReminder.title.isNotEmpty() && selectedReminder.date.isNotEmpty()) && gCalendar.timeInMillis > Calendar.getInstance().timeInMillis) {
                val notificationId = setOneTimeWorkRequest(10000, selectedReminder.title)
                selectedReminder.notification_id = notificationId.toString()
                reminderDao.update(selectedReminder)
                finish()
            }
            else if (selectedReminder.date.isNotEmpty() && gCalendar.timeInMillis < Calendar.getInstance().timeInMillis) {
                tvReminderError.text = getString(R.string.future_reminder)
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
        val data = Data.Builder().putString(AddReminderActivity.workerKey, title).build()
        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(ReminderWorker::class.java)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS).setInputData(data).build()
        WorkManager.getInstance(applicationContext).enqueue(oneTimeWorkRequest)
        return oneTimeWorkRequest.id
    }

    private fun isInteger(input: String, integerChars: CharRange) = input.all { it in integerChars }
}