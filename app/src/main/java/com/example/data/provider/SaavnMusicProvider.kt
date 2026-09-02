package com.example.data.provider

import android.os.Build
import android.text.Html
import android.util.Base64
import com.example.data.local.entity.SongEntity
import com.example.data.remote.audius.AudiusApiClient
import com.example.data.remote.audius.AudiusTrackDto
import com.example.data.remote.saavn.JioSaavnDirectSongDto
import com.example.data.remote.saavn.SaavnApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.math.absoluteValue

class SaavnMusicProvider : MusicProvider {
    override val providerName: String = "Full-Length Music Studio (Indian, Pakistani & Global)"
    override val isConfigured: Boolean = true

    private val saavnDesKey = "38346591"

    private val vibrantPalette = listOf(
        "#8B5CF6", "#EC4899", "#3B82F6", "#10B981", "#F59E0B",
        "#6366F1", "#14B8A6", "#F43F5E", "#06B6D4", "#A855F7",
        "#EF4444", "#84CC16", "#D946EF", "#0EA5E9"
    )

    override suspend fun searchTracks(
        query: String,
        filter: MusicSearchFilter,
        page: Int,
        pageSize: Int
    ): Result<List<SongEntity>> = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) {
            return@withContext Result.success(emptyList())
        }

        // 1. First priority: JioSaavn Direct API with 320kbps full-track decrypt
        try {
            val response = SaavnApiClient.service.searchSongsDirect(
                query = cleanQuery,
                page = page + 1,
                limit = pageSize
            )

            if (response.isSuccessful && response.body() != null) {
                val results = response.body()?.results.orEmpty()
                val songs = results
                    .mapNotNull { dto ->
                        val audioUrl = decryptMediaUrl(dto.encryptedMediaUrl)
                        if (!audioUrl.isNullOrBlank() && !dto.song.isNullOrBlank()) {
                            dto.toSongEntity(audioUrl)
                        } else null
                    }

                if (songs.isNotEmpty()) {
                    return@withContext Result.success(songs)
                }
            }
        } catch (e: Exception) {
            // Proceed to Audius fallback
        }

        // 2. Fallback: Audius Global Open Music Network (Full-length streams)
        try {
            val audiusResp = AudiusApiClient.service.searchTracks(
                query = cleanQuery,
                limit = pageSize
            )
            if (audiusResp.isSuccessful && audiusResp.body() != null) {
                val tracks = audiusResp.body()?.data.orEmpty()
                val songs = tracks.mapNotNull { it.toSongEntity() }
                if (songs.isNotEmpty()) {
                    return@withContext Result.success(songs)
                }
            }
        } catch (e: Exception) {
            return@withContext Result.failure(Exception("Could not connect to online music network. Please check internet connection."))
        }

        return@withContext Result.success(emptyList())
    }

    override suspend fun getFeaturedTracks(
        genreOrCategory: String?,
        page: Int,
        pageSize: Int
    ): Result<List<SongEntity>> = withContext(Dispatchers.IO) {
        val term = mapGenreToQuery(genreOrCategory)

        // 1. Try JioSaavn direct for full-length tracks
        try {
            val response = SaavnApiClient.service.searchSongsDirect(
                query = term,
                page = page + 1,
                limit = pageSize
            )

            if (response.isSuccessful && response.body() != null) {
                val results = response.body()?.results.orEmpty()
                val songs = results
                    .mapNotNull { dto ->
                        val audioUrl = decryptMediaUrl(dto.encryptedMediaUrl)
                        if (!audioUrl.isNullOrBlank() && !dto.song.isNullOrBlank()) {
                            dto.toSongEntity(audioUrl)
                        } else null
                    }

                if (songs.isNotEmpty()) {
                    return@withContext Result.success(songs)
                }
            }
        } catch (e: Exception) {
            // Fallback
        }

        // 2. Try Audius Trending
        try {
            val audiusResp = AudiusApiClient.service.getTrendingTracks(
                genre = if (genreOrCategory?.contains("Lo-Fi", true) == true) "Lo-Fi" else null,
                limit = pageSize
            )
            if (audiusResp.isSuccessful && audiusResp.body() != null) {
                val tracks = audiusResp.body()?.data.orEmpty()
                val songs = tracks.mapNotNull { it.toSongEntity() }
                if (songs.isNotEmpty()) {
                    return@withContext Result.success(songs)
                }
            }
        } catch (e: Exception) {
            return@withContext Result.failure(Exception("Unable to load trending songs. Tap retry to reload."))
        }

        return@withContext Result.success(emptyList())
    }

    private fun mapGenreToQuery(genreOrCategory: String?): String {
        return when (genreOrCategory?.lowercase()?.trim()) {
            null, "", "top hits", "trending", "🔥 top hits" -> "Top Bollywood Hindi Pakistani Punjabi Hits"
            "🇮🇳 bollywood", "bollywood", "hindi", "hindi hits" -> "Arijit Singh Pritam Bollywood Top Hits"
            "🇵🇰 pakistani", "pakistan", "coke studio", "pakistani hits" -> "Coke Studio Pakistan Atif Aslam Rahat Fateh Ali Khan"
            "🌾 punjabi", "punjabi", "punjabi hits" -> "Sidhu Moosewala AP Dhillon Karan Aujla Diljit Dosanjh Punjabi Hits"
            "🪕 sufi & ghazals", "sufi", "ghazals", "ghazal" -> "Nusrat Fateh Ali Khan Jagjit Singh Rahat Fateh Ali Khan Sufi"
            "🌙 romantic chill", "romantic", "chillout", "chill" -> "Acoustic Romantic Bollywood Pakistani Songs"
            "🎧 lo-fi beats", "lo-fi", "lofi", "lofi beats" -> "Lo-Fi Hindi Beats Relax Chill"
            "🎤 hip-hop", "hip-hop", "hip hop", "rap" -> "Desi Hip Hop Divine Seedhe Maut Karan Aujla"
            "🌿 indie", "indie" -> "Anuv Jain Prateek Kuhad Indian Indie"
            "✨ global pop", "pop" -> "Global Pop Hits 2024"
            "🎸 rock", "rock" -> "Junoon Strings Pakistani Rock"
            "🎙️ r&b", "r&b", "rnb" -> "R&B Soul Classics"
            "🎹 electronic", "electronic", "edm" -> "Electronic Dance Top Hits"
            "🎷 jazz", "jazz" -> "Jazz Instrumental Melodies"
            "🎻 classical", "classical" -> "Indian Classical Instrumental Sitar Masterpieces"
            else -> genreOrCategory
        }
    }

    /**
     * Decrypts JioSaavn's DES-ECB encrypted media URL into a full-length 320kbps MP4/M4A streaming link.
     */
    private fun decryptMediaUrl(encryptedUrl: String?): String? {
        if (encryptedUrl.isNullOrBlank()) return null
        return try {
            val keySpec = SecretKeySpec(saavnDesKey.toByteArray(StandardCharsets.UTF_8), "DES")
            val cipher = Cipher.getInstance("DES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            val decodedBytes = Base64.decode(encryptedUrl.trim(), Base64.DEFAULT)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            val decryptedUrl = String(decryptedBytes, StandardCharsets.UTF_8).trim()
            
            // Prefer 320kbps maximum quality full-length stream
            decryptedUrl.replace("_96.mp4", "_320.mp4")
                .replace("_160.mp4", "_320.mp4")
        } catch (e: Exception) {
            null
        }
    }

    private fun getHighResSaavnArtwork(rawUrl: String?): String? {
        if (rawUrl.isNullOrBlank()) return null
        return rawUrl.replace("50x50.jpg", "500x500.jpg")
            .replace("150x150.jpg", "500x500.jpg")
    }

    private fun unescapeHtml(text: String?): String {
        if (text == null) return ""
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString().trim()
            } else {
                @Suppress("DEPRECATION")
                Html.fromHtml(text).toString().trim()
            }
        } catch (e: Exception) {
            text.replace("&quot;", "\"")
                .replace("&#039;", "'")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .trim()
        }
    }

    private fun JioSaavnDirectSongDto.toSongEntity(audioUrl: String): SongEntity {
        val cleanTitle = unescapeHtml(song ?: "Unknown Song")
        val cleanArtist = unescapeHtml(
            when {
                !singers.isNullOrBlank() -> singers
                !primaryArtists.isNullOrBlank() -> primaryArtists
                !featuredArtists.isNullOrBlank() -> featuredArtists
                !music.isNullOrBlank() -> music
                else -> "Unknown Artist"
            }
        )
        val cleanAlbum = unescapeHtml(album ?: "Single")
        val artworkUrl = getHighResSaavnArtwork(image)

        val durationSec = duration?.toLongOrNull() ?: 240L
        val durationMs = durationSec * 1000L

        val colorIndex = ((id ?: cleanTitle).hashCode().toLong().absoluteValue % vibrantPalette.size).toInt()
        val colorHex = vibrantPalette[colorIndex]

        return SongEntity(
            id = 0L,
            title = cleanTitle,
            artist = cleanArtist,
            album = cleanAlbum,
            durationMs = durationMs,
            uriString = audioUrl,
            isFavorite = false,
            dateAdded = System.currentTimeMillis(),
            lastPlayedTimestamp = 0L,
            isPresetSong = false,
            albumColorHex = colorHex,
            artworkBase64 = null,
            artworkUrl = artworkUrl,
            isOnline = true,
            source = "saavn_full",
            externalId = id ?: audioUrl,
            externalUrl = permaUrl
        )
    }

    private fun AudiusTrackDto.toSongEntity(): SongEntity? {
        val trackId = id ?: return null
        val titleText = title ?: "Unknown Track"
        val artistText = user?.name ?: "Unknown Artist"
        val streamUrl = "https://discoveryprovider.audius.co/v1/tracks/$trackId/stream?app_name=ai_studio_music"
        val artwork = artwork?.art1000 ?: (artwork?.art480 ?: artwork?.art150)
        val durationMs = (duration ?: 180L) * 1000L

        val colorIndex = (trackId.hashCode().toLong().absoluteValue % vibrantPalette.size).toInt()
        val colorHex = vibrantPalette[colorIndex]

        return SongEntity(
            id = 0L,
            title = titleText,
            artist = artistText,
            album = genre ?: "Audius Original",
            durationMs = durationMs,
            uriString = streamUrl,
            isFavorite = false,
            dateAdded = System.currentTimeMillis(),
            lastPlayedTimestamp = 0L,
            isPresetSong = false,
            albumColorHex = colorHex,
            artworkBase64 = null,
            artworkUrl = artwork,
            isOnline = true,
            source = "audius_full",
            externalId = trackId,
            externalUrl = permalink?.let { "https://audius.co$it" }
        )
    }
}
