package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "Personal", // Study, Work, Personal, Shopping, Important, Habits, Creative, Wellness
    val priority: String = "Medium", // Low, Medium, High
    val dueDate: String = "", // YYYY-MM-DD
    val dueTime: String = "", // HH:mm
    val isCompleted: Boolean = false,
    val isRecurring: Boolean = false,
    val recurrence: String = "None", // Daily, Weekdays, Weekly
    val createdAt: Long = System.currentTimeMillis()
)
