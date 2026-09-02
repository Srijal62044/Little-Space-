package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.squareup.moshi.JsonClass

enum class TemplateCategory(val title: String, val emoji: String) {
    ALL("All Designs", "✨"),
    VINTAGE("Vintage & Film", "🎞️"),
    EDITORIAL("Magazine & Vogue", "📰"),
    AESTHETIC("Aesthetic & Glow", "🌌"),
    SCRAPBOOK("Romantic Scrapbook", "🌸"),
    MINIMAL("Minimal & Grid", "📐"),
    TRAVEL("Travel & Postcard", "✈️")
}

enum class CanvasAspectRatio(val label: String, val ratio: Float, val iconText: String) {
    SQUARE("1:1 Square", 1f, "1:1"),
    PORTRAIT("4:5 Portrait", 4f / 5f, "4:5"),
    STORY("9:16 Story", 9f / 16f, "9:16")
}

enum class BackgroundStyle(val title: String) {
    BLUR("Photo Backdrop"),
    GRADIENT("Pastel Aura"),
    SOLID("Studio Solid"),
    PAPER("Vintage Texture")
}

@JsonClass(generateAdapter = true)
data class PhotoTemplate(
    val id: String,
    val name: String,
    val category: TemplateCategory,
    val description: String,
    val iconEmoji: String,
    val badge: String? = null,
    val minPhotos: Int = 1,
    val maxPhotos: Int = 9,
    val defaultBgStyle: BackgroundStyle = BackgroundStyle.BLUR,
    val defaultBgHex: String = "#FBF8F2",
    val accentColorHex: String = "#E11D48",
    val isOnline: Boolean = true,
    val downloadCount: String = "12.5k",
    val rating: Float = 4.9f
)

data class TemplateSticker(
    val id: String,
    val label: String,
    val emoji: String
)
