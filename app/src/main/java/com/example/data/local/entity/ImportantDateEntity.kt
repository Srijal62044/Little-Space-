package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "important_dates")
data class ImportantDateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetDate: String, // YYYY-MM-DD
    val category: String = "Birthday", // Birthday, Event, Deadline, Anniversary, Holiday
    val icon: String = "🎂",
    val notes: String = "",
    val isReminderEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
