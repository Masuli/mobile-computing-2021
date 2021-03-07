package com.example.reminderappsp

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.startForegroundService
import androidx.room.Room
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.reminderappsp.db.AppDatabase
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.util.*
import java.util.concurrent.TimeUnit

class GeofenceReceiver : BroadcastReceiver() {
    lateinit var key: String
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            val db = Room.databaseBuilder(context, AppDatabase::class.java, "reminderDB").allowMainThreadQueries().build()
            val reminderDao = db.reminderDao()
            val geofencingEvent = GeofencingEvent.fromIntent(intent)
            val geofenceTransition = geofencingEvent.geofenceTransition

            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                    geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL)  {
                if (intent != null) {
                    key = intent.getStringExtra("key")!!
                }
                val database = Firebase.database
                val reference = database.getReference("locations")
                val locationListener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val location = snapshot.getValue<LocationInfo>()
                        if (location != null) {
                            val reminder = reminderDao.getLocationInfo("${location.lat}, ${location.lng}")
                            WorkManager.getInstance().cancelWorkById(UUID.fromString(reminder.notification_id))
                            setOneTimeWorkRequest(3000, reminder.title, context)
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                            Log.d("myTag", "onCancelled called")
                    }
                }
                val child = reference.child(key)
                child.addValueEventListener(locationListener)
            }
        }
    }

    private fun setOneTimeWorkRequest(delay: Long, title: String, context: Context): UUID {
        val data = Data.Builder().putString(AddReminderActivity.workerKey, title).build()
        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(ReminderWorker::class.java)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS).setInputData(data).build()
        WorkManager.getInstance(context).enqueue(oneTimeWorkRequest)
        return oneTimeWorkRequest.id
    }
}