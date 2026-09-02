package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val icon: String = "🌱",
    val category: String = "Wellness", // Health, Study, Routine, Mind
    val frequency: String = "Daily",
    val targetPerDay: Int = 1,
    val reminderTime: String = "09:00",
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "habit_logs",
    primaryKeys = ["habitId", "dateString"]
)
data class HabitLogEntity(
    val habitId: Long,
    val dateString: String, // YYYY-MM-DD
    val isCompleted: Boolean = true,
    val completedCount: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)
