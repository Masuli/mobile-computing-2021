package com.example.reminderappsp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ReminderDao {
    @Insert
    fun insert(reminder: ReminderInfo)

    @Update
    fun update(reminder: ReminderInfo)

    @Query("DELETE FROM reminders WHERE uid = :id")
    fun delete(id: Int?)

    @Query("SELECT * FROM reminders WHERE uid = :id")
    fun getReminderInfo(id: Int?): ReminderInfo

    @Query("SELECT * FROM reminders WHERE location_x = :locationX")
    fun getLocationInfo(locationX: String?): ReminderInfo

    @Query("SELECT * FROM reminders WHERE creator_id = :id")
    fun getReminderInfos(id: String?): MutableList<ReminderInfo>
}