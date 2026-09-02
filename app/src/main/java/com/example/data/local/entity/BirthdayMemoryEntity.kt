package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "birthday_memories")
data class BirthdayMemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val emoji: String = "🌸",
    val title: String = "Memories & Milestones",
    val caption: String = "A wonderful moment celebrating happiness and growth.",
    val imageUrl: String = "",
    val dateTag: String = "Special Moment",
    val orderIndex: Int = 0
)
