package com.example.reminderappsp.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ReminderInfo::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reminderDao() : ReminderDao
}