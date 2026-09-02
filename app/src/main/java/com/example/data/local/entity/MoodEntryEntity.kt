package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_entries")
data class MoodEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mood: String, // Great, Good, Okay, Not great, Low, Tired
    val moodEmoji: String, // 😊, 🙂, 😐, 😕, 😔, 😴
    val supportiveMessage: String,
    val activitySuggestion: String = "",
    val note: String = "",
    val dateString: String, // YYYY-MM-DD
    val timestamp: Long = System.currentTimeMillis()
)
