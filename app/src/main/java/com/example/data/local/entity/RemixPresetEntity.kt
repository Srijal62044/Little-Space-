package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remix_presets")
data class RemixPresetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val isCustom: Boolean = true,
    val bass: Float = 0f,       // -100 to +100
    val treble: Float = 0f,     // -100 to +100
    val vocal: Float = 0f,      // -100 to +100
    val reverb: Float = 0f,     // 0 to 100
    val echoDelay: Float = 0f,  // 0 to 100
    val speed: Float = 1.0f,    // 0.5 to 2.0
    val pitch: Float = 1.0f,    // 0.5 to 2.0
    val volume: Float = 100f,   // 0 to 100
    val balance: Float = 0f     // -100 (left) to +100 (right)
)
