package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_activity")
data class DailyActivityEntity(
    @PrimaryKey val date: String, // "yyyy-MM-dd"
    val tasksCompleted: Int = 0,
    val habitsCompleted: Int = 0,
    val isStreakAchieved: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
