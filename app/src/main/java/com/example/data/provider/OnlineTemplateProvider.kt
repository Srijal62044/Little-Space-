package com.example.data.provider

import com.example.data.model.BackgroundStyle
import com.example.data.model.PhotoTemplate
import com.example.data.model.TemplateCategory
import com.example.data.model.TemplateSticker
import com.example.data.remote.GeminiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

object OnlineTemplateProvider {

    val availableStickers = listOf(
        TemplateSticker("washi_pink", "Washi Tape", "🎀"),
        TemplateSticker("star_sparkle", "Sparkle", "✨"),
        TemplateSticker("heart_soft", "Soft Heart", "💖"),
        TemplateSticker("postmark", "Airmail Stamp", "📮"),
        TemplateSticker("flower_cherry", "Cherry Blossom", "🌸"),
        TemplateSticker("butterfly", "Butterfly", "🦋"),
        TemplateSticker("pin", "Thumbtack", "📌"),
        TemplateSticker("film_date", "Date Stamp", "📅"),
        TemplateSticker("music_note", "Melody", "🎵"),
        TemplateSticker("coffee", "Warm Vibe", "☕"),
        TemplateSticker("crown", "Pro Gold", "👑"),
        TemplateSticker("party", "Confetti", "🎉")
    )

    private val _onlineCloudTemplates = mutableListOf(
        PhotoTemplate(
            id = "polaroid_vintage",
            name = "Polaroid Memories",
            category = TemplateCategory.VINTAGE,
            description = "Classic instant film frames with washi tape, shadows and handwritten notes.",
            iconEmoji = "📸",
            badge = "Most Popular",
            minPhotos = 1,
            maxPhotos = 6,
            defaultBgStyle = BackgroundStyle.PAPER,
            defaultBgHex = "#FAF7F0",
            accentColorHex = "#D97706",
            downloadCount = "24.5k",
            rating = 4.9f
        ),
        PhotoTemplate(
            id = "filmstrip_35mm",
            name = "35mm Analog Roll",
            category = TemplateCategory.VINTAGE,
            description = "Cinematic 35mm film negatives with sprocket borders, timestamp and grain.",
            iconEmoji = "🎞️",
            badge = "Trending #1",
            minPhotos = 1,
            maxPhotos = 6,
            defaultBgStyle = BackgroundStyle.SOLID,
            defaultBgHex = "#121214",
            accentColorHex = "#F59E0B",
            downloadCount = "31.2k",
            rating = 4.9f
        ),
        PhotoTemplate(
            id = "vogue_editorial",
            name = "Vogue Magazine Cover",
            category = TemplateCategory.EDITORIAL,
            description = "High-fashion magazine cover with bold display typography, price tag & barcode badges.",
            iconEmoji = "📰",
            badge = "Pro Cloud",
            minPhotos = 1,
            maxPhotos = 4,
            defaultBgStyle = BackgroundStyle.BLUR,
            defaultBgHex = "#FFFFFF",
            accentColorHex = "#18181B",
            downloadCount = "19.8k",
            rating = 4.8f
        ),
        PhotoTemplate(
            id = "romantic_scrapbook",
            name = "Pastel Love Scrapbook",
            category = TemplateCategory.SCRAPBOOK,
            description = "Romantic pastel aesthetic with floral accents, torn edges and heart stamps.",
            iconEmoji = "🌸",
            badge = "Wholesome",
            minPhotos = 1,
            maxPhotos = 6,
            defaultBgStyle = BackgroundStyle.GRADIENT,
            defaultBgHex = "#FFF1F2",
            accentColorHex = "#E11D48",
            downloadCount = "15.6k",
            rating = 4.9f
        ),
        PhotoTemplate(
            id = "bento_grid",
            name = "Canva Bento Grid",
            category = TemplateCategory.MINIMAL,
            description = "Clean modern modular grid layout with soft shadows and generous negative space.",
            iconEmoji = "📐",
            badge = "Essential",
            minPhotos = 1,
            maxPhotos = 9,
            defaultBgStyle = BackgroundStyle.BLUR,
            defaultBgHex = "#F8FAFC",
            accentColorHex = "#6366F1",
            downloadCount = "42.1k",
            rating = 5.0f
        ),
        PhotoTemplate(
            id = "neon_cyber",
            name = "Cyberpunk Neon Glow",
            category = TemplateCategory.AESTHETIC,
            description = "Obsidian dark canvas framed by glowing ultraviolet & cyan neon light outlines.",
            iconEmoji = "🌌",
            badge = "Dark Mode",
            minPhotos = 1,
            maxPhotos = 4,
            defaultBgStyle = BackgroundStyle.SOLID,
            defaultBgHex = "#09090B",
            accentColorHex = "#06B6D4",
            downloadCount = "11.4k",
            rating = 4.7f
        ),
        PhotoTemplate(
            id = "travel_postcard",
            name = "Wanderlust Airmail",
            category = TemplateCategory.TRAVEL,
            description = "Vintage travel postcard with airmail borders, circular postal stamps and pin badge.",
            iconEmoji = "✈️",
            badge = "Adventure",
            minPhotos = 1,
            maxPhotos = 4,
            defaultBgStyle = BackgroundStyle.PAPER,
            defaultBgHex = "#FDFBF7",
            accentColorHex = "#2563EB",
            downloadCount = "18.3k",
            rating = 4.9f
        ),
        PhotoTemplate(
            id = "minimal_gallery",
            name = "Museum Fine Art",
            category = TemplateCategory.MINIMAL,
            description = "Spacious museum matting with fine line borders and golden caption plaque.",
            iconEmoji = "🏛️",
            badge = "Minimal",
            minPhotos = 1,
            maxPhotos = 4,
            defaultBgStyle = BackgroundStyle.SOLID,
            defaultBgHex = "#F4F4F5",
            accentColorHex = "#71717A",
            downloadCount = "9.7k",
            rating = 4.8f
        )
    )

    val templates: List<PhotoTemplate>
        get() = _onlineCloudTemplates.toList()

    fun getTemplateById(id: String): PhotoTemplate {
        return _onlineCloudTemplates.firstOrNull { it.id == id } ?: _onlineCloudTemplates.first()
    }

    /**
     * Search and filter online cloud templates like Canva Store
     */
    fun searchOnlineTemplates(
        query: String,
        category: TemplateCategory
    ): List<PhotoTemplate> {
        return _onlineCloudTemplates.filter { template ->
            val matchesCategory = (category == TemplateCategory.ALL || template.category == category)
            val matchesQuery = if (query.isBlank()) true else {
                template.name.contains(query, ignoreCase = true) ||
                template.description.contains(query, ignoreCase = true) ||
                (template.badge?.contains(query, ignoreCase = true) == true)
            }
            matchesCategory && matchesQuery
        }
    }

    /**
     * Canva Magic AI Generator:
     * Generates a brand new dynamic online template layout directly from user prompt!
     */
    suspend fun generateCustomCanvaTemplate(
        promptText: String
    ): PhotoTemplate = withContext(Dispatchers.IO) {
        val prompt = buildString {
            append("You are Canva's Lead Design Architect. ")
            append("Create a brand new Canva-style online photo template based on prompt: '$promptText'. ")
            append("Return strict valid JSON only with keys: ")
            append("{\"name\": \"Template Title (max 4 words)\", ")
            append("\"description\": \"1-2 line aesthetic layout description\", ")
            append("\"category\": \"one of [VINTAGE, EDITORIAL, AESTHETIC, SCRAPBOOK, MINIMAL, TRAVEL]\", ")
            append("\"emoji\": \"1 emoji icon\", ")
            append("\"badge\": \"1 short badge name like 'AI Exclusive' or 'Cloud Pro'\", ")
            append("\"bgHex\": \"#HEX Color for background\", ")
            append("\"accentHex\": \"#HEX Color for primary text/accents\"}")
        }

        val response = GeminiClient.generateText(
            prompt = prompt,
            systemInstruction = "You are an expert graphic designer for Canva. Return clean JSON only without markdown formatting."
        )

        val generatedTemplate = if (response.isSuccess) {
            try {
                val raw = response.getOrNull().orEmpty().trim()
                    .removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                val json = JSONObject(raw)
                val catStr = json.optString("category", "AESTHETIC")
                val categoryEnum = try {
                    TemplateCategory.valueOf(catStr)
                } catch (e: Exception) {
                    TemplateCategory.AESTHETIC
                }

                PhotoTemplate(
                    id = "canva_ai_${System.currentTimeMillis()}",
                    name = json.optString("name", "Canva Magic ${promptText.take(12)}"),
                    category = categoryEnum,
                    description = json.optString("description", "AI-crafted Canva cloud template layout customized for your prompt."),
                    iconEmoji = json.optString("emoji", "✨"),
                    badge = json.optString("badge", "Canva AI"),
                    minPhotos = 1,
                    maxPhotos = 9,
                    defaultBgStyle = BackgroundStyle.GRADIENT,
                    defaultBgHex = json.optString("bgHex", "#FFF1F2"),
                    accentColorHex = json.optString("accentHex", "#E11D48"),
                    isOnline = true,
                    downloadCount = "Just Created",
                    rating = 5.0f
                )
            } catch (e: Exception) {
                null
            }
        } else null

        val finalTemplate = generatedTemplate ?: PhotoTemplate(
            id = "canva_ai_${System.currentTimeMillis()}",
            name = "Aesthetic ${promptText.take(15)}",
            category = TemplateCategory.AESTHETIC,
            description = "Custom Canva Cloud template for '$promptText' with dynamic typography and soft aura.",
            iconEmoji = "✨",
            badge = "Canva AI",
            defaultBgStyle = BackgroundStyle.GRADIENT,
            defaultBgHex = "#FFFBEB",
            accentColorHex = "#D97706"
        )

        _onlineCloudTemplates.add(0, finalTemplate)
        return@withContext finalTemplate
    }

    /**
     * AI Dynamic Creative Stylist:
     * Generates a poetic title, evocative caption, matching background tone, and suggested template.
     */
    suspend fun generateAiStyling(
        photoCount: Int,
        userVibeOrOccasion: String?
    ): AiStylingResult = withContext(Dispatchers.IO) {
        val prompt = buildString {
            append("You are a creative photo designer and visual art curator for Canva. ")
            append("A user selected $photoCount photos with theme/mood: '${userVibeOrOccasion ?: "Sweet moments and memories"}'. ")
            append("Suggest a creative presentation. Output valid JSON only with keys: ")
            append("{\"title\": \"A short elegant title (max 4 words)\", ")
            append("\"caption\": \"A beautiful, heartfelt 1-2 sentence quote or story caption\", ")
            append("\"suggestedTemplateId\": \"one of [polaroid_vintage, filmstrip_35mm, vogue_editorial, romantic_scrapbook, bento_grid, neon_cyber, travel_postcard, minimal_gallery]\", ")
            append("\"colorHex\": \"#HEXColor matching the vibe\", ")
            append("\"sticker\": \"one of [✨, 💖, 🌸, 📮, 🦋, 📌, 📅, 🎀, 👑, 🎉]\"}")
        }

        val aiResponse = GeminiClient.generateText(
            prompt = prompt,
            systemInstruction = "You are an aesthetic visual collage and typography stylist. Return strict JSON only without markdown code blocks."
        )

        if (aiResponse.isSuccess) {
            try {
                val raw = aiResponse.getOrNull().orEmpty().trim()
                    .removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                val json = JSONObject(raw)
                return@withContext AiStylingResult(
                    title = json.optString("title", "Cherished Moments ✨"),
                    caption = json.optString("caption", "Every picture tells a quiet little story that words cannot hold."),
                    templateId = json.optString("suggestedTemplateId", "polaroid_vintage"),
                    colorHex = json.optString("colorHex", "#FFF1F2"),
                    stickerEmoji = json.optString("sticker", "✨")
                )
            } catch (e: Exception) {
                // Fallback
            }
        }

        val fallbackSuggestions = listOf(
            AiStylingResult(
                title = "Golden Moments ✨",
                caption = "Wrapped in quiet smiles and unforgettable sunsets.",
                templateId = "polaroid_vintage",
                colorHex = "#FFFBEB",
                stickerEmoji = "✨"
            ),
            AiStylingResult(
                title = "Love & Laughter 💖",
                caption = "The best things in life are the people we love and the memories we make.",
                templateId = "romantic_scrapbook",
                colorHex = "#FFF1F2",
                stickerEmoji = "💖"
            ),
            AiStylingResult(
                title = "Cinematic Chapter 🎞️",
                caption = "Captured on 35mm dreams and timeless frames.",
                templateId = "filmstrip_35mm",
                colorHex = "#18181B",
                stickerEmoji = "📅"
            ),
            AiStylingResult(
                title = "Editorial Edition 📰",
                caption = "Style, grace, and an unapologetic celebration of now.",
                templateId = "vogue_editorial",
                colorHex = "#FFFFFF",
                stickerEmoji = "🦋"
            )
        )
        return@withContext fallbackSuggestions.random()
    }
}

data class AiStylingResult(
    val title: String,
    val caption: String,
    val templateId: String,
    val colorHex: String,
    val stickerEmoji: String
)
