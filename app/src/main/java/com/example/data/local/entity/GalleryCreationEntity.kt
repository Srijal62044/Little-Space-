package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "gallery_creations")
@JsonClass(generateAdapter = true)
data class GalleryCreationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val caption: String = "",
    val templateId: String,
    val templateName: String,
    val photoUrisJson: String, // Comma-separated or JSON list of internal file paths
    val renderedImagePath: String? = null,
    val aspectRatio: String = "1:1", // "1:1", "9:16", "4:5"
    val backgroundColorHex: String = "#FFFDF8",
    val bgStyle: String = "BLUR", // "BLUR", "GRADIENT", "SOLID", "PAPER"
    val stickerBadge: String? = null,
    val dateStampText: String? = null,
    val locationText: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)
