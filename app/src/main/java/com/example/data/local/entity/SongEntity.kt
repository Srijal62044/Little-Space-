package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val artist: String = "Unknown Artist",
    val album: String = "Local Audio",
    val durationMs: Long = 0L,
    val uriString: String,
    val isFavorite: Boolean = false,
    val dateAdded: Long = System.currentTimeMillis(),
    val lastPlayedTimestamp: Long = 0L,
    val isPresetSong: Boolean = false,
    val albumColorHex: String = "#8B5CF6",
    val artworkBase64: String? = null,
    val artworkUrl: String? = null,
    val isOnline: Boolean = false,
    val source: String = "local",
    val externalId: String? = null,
    val externalUrl: String? = null
)

