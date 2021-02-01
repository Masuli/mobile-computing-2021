package com.example.reminderappsp

data class Reminder(val reminderTitle: String, val reminderDate: String, var isChecked: Boolean = false)