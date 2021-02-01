package com.example.reminderappsp

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.reminder_item_layout.view.*

class ReminderAdapter(private val reminders: MutableList<Reminder>) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {
    class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        return ReminderViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.reminder_item_layout,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val currentReminder = reminders[position]
        holder.itemView.apply {
            tvReminderTitle.text = currentReminder.reminderTitle
            tvReminderDate.text = currentReminder.reminderDate
            cbReminderCheckBox.isChecked = currentReminder.isChecked
            cbReminderCheckBox.setOnCheckedChangeListener { _, isChecked ->
                currentReminder.isChecked = !currentReminder.isChecked
            }
        }
    }

    override fun getItemCount(): Int {
        return reminders.size
    }

    fun addReminder(reminder: Reminder) {
        reminders.add(reminder)
        notifyItemInserted(reminders.size - 1)
    }

    fun deleteReminders() {
        reminders.removeAll { reminder -> reminder.isChecked }
        notifyDataSetChanged()
    }
}