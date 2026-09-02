package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Priyanka",
    val wakeTime: String = "07:30",
    val sleepTime: String = "23:00",
    val importantActivities: String = "Morning tea, Study / Work session, Reading, Evening walk",
    val targetHabits: String = "Drink enough water, Read, Walk, Sleep on time",
    val reminderType: String = "Gentle", // Gentle, Strict
    val currentGoals: String = "Stay consistent, balanced routine, excel in daily goals",
    val favoriteTheme: String = "Rose Blush", // Rose Blush, Matcha Sage, Lavender Dream, Warm Peach, Cozy Latte, Twilight
    val morningGreeting: String = "Good morning, Priyanka 🌷",
    val isOnboardingCompleted: Boolean = false,
    val isDarkMode: Boolean = false,
    val followSystemTheme: Boolean = true
)
