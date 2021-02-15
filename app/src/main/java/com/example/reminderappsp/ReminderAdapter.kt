package com.example.reminderappsp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.reminderappsp.db.ReminderInfo
import kotlinx.android.synthetic.main.reminder_item_layout.view.*

class ReminderAdapter(private val reminders: MutableList<ReminderInfo>) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {
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
            tvReminderTitle.text = currentReminder.title
            tvReminderDate.text = currentReminder.date
            tvLocationX.text = currentReminder.location_x
            tvLocationY.text = currentReminder.location_y
            cbReminderCheckBox.isChecked = currentReminder.isChecked
            cbReminderCheckBox.setOnCheckedChangeListener { _, _ ->
                currentReminder.isChecked = !currentReminder.isChecked
            }
        }
    }

    override fun getItemCount(): Int {
        return reminders.size
    }

    fun addReminder(reminder: ReminderInfo) {
        reminders.add(reminder)
        notifyItemInserted(reminders.size - 1)
    }

    fun getCheckedReminders(): MutableList<Int?> {
        val checkedReminders = mutableListOf<Int?>()
        for (reminder in reminders) {
            if (reminder.isChecked) {
                checkedReminders.add(reminder.uid)
            }
        }
        return checkedReminders
    }

    fun deleteReminders(): MutableList<ReminderInfo> {
        val toBeDeleted = mutableListOf<ReminderInfo>()
        for (reminder in reminders) {
            if (reminder.isChecked) {
                toBeDeleted.add(reminder)
            }
        }
        reminders.removeAll { reminder -> reminder.isChecked }
        notifyDataSetChanged()
        return toBeDeleted
    }
}