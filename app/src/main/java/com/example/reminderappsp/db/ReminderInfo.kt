package com.example.reminderappsp.db

import android.media.MediaSession2Service
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.time.LocalTime
import java.util.*

@Entity(tableName = "reminders")
data class ReminderInfo(
        @PrimaryKey(autoGenerate = true) var uid: Int?,
        @ColumnInfo(name="title") var title: String,
        @ColumnInfo(name="date") var date: String,
        @ColumnInfo(name="location_x") var location_x: String?,
        @ColumnInfo(name="location_y") var location_y: String?,
        @ColumnInfo(name="creation_time") var creation_time: String,
        @ColumnInfo(name="creator_id") var creator_id: String?,
        @ColumnInfo(name="reminder_seen") var reminder_seen: Boolean,
        @ColumnInfo(name="notification_id") var notification_id: String,
        @ColumnInfo(name="meters") var meters: String?,
        var isChecked: Boolean = false
)