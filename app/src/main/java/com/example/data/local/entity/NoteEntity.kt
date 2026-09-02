package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val category: String = "Ideas", // Ideas, Reminders, Shopping, Random thoughts, Remember
    val colorTag: String = "Blush", // Blush, Sage, Lavender, Peach, Honey, Sky
    val isPinned: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
