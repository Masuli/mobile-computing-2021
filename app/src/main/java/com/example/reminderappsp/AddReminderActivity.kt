package com.example.reminderappsp

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.room.Room
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.reminderappsp.db.AppDatabase
import com.example.reminderappsp.db.ReminderInfo
import kotlinx.android.synthetic.main.activity_add_reminder.*
import java.time.LocalDate
import java.util.*
import java.util.concurrent.TimeUnit

class AddReminderActivity : AppCompatActivity() {
    companion object {
        const val workerKey = "workerKey"
    }

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

        tvDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(this, { _, mYear, mMonth, mDay ->
                tvDate.text = "" + mDay + "." + (mMonth + 1) + "." + mYear
            }, year, month, day)
            datePickerDialog.show()
        }

        btnSubmit.setOnClickListener {
            val reminderTitle = etTitle.text.toString()
            val reminderDate = tvDate.text.toString()
            val locationX = etLocationX.text.toString()
            val locationY = etLocationY.text.toString()

            val dateParts = reminderDate.split(".").toTypedArray()
            val gCalendar = GregorianCalendar(dateParts[2].toInt(), dateParts[1].toInt() - 1, dateParts[0].toInt())

            if ((reminderTitle.isNotEmpty() && reminderDate.isNotEmpty()) && gCalendar.timeInMillis > Calendar.getInstance().timeInMillis) {
                val notificationId = setOneTimeWorkRequest(10000, reminderTitle)
                val reminder = ReminderInfo(null, reminderTitle, reminderDate, locationX, locationY, LocalDate.now().toString(), username, false, notificationId.toString())
                reminderDao.insert(reminder)
                //setOneTimeWorkRequest(gCalendar.timeInMillis - Calendar.getInstance().timeInMillis)
                finish()
            }
            else if (reminderDate.isNotEmpty() && gCalendar.timeInMillis < Calendar.getInstance().timeInMillis) {
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
        val data = Data.Builder().putString(workerKey, title).build()
        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(ReminderWorker::class.java)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS).setInputData(data).build()
        WorkManager.getInstance(applicationContext).enqueue(oneTimeWorkRequest)
        return oneTimeWorkRequest.id
    }
}