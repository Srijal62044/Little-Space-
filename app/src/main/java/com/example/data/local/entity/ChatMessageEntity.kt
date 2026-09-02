package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // "user" or "pia"
    val text: String,
    val isSchedulePlan: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
