package com.example.data.provider

import com.example.data.local.entity.SongEntity

enum class MusicSearchFilter {
    ALL, TITLE, ARTIST, GENRE
}

/**
 * Generic interface for external music providers.
 * Ready for future music service integrations.
 */
interface MusicProvider {
    val providerName: String
    val isConfigured: Boolean

    suspend fun searchTracks(
        query: String,
        filter: MusicSearchFilter = MusicSearchFilter.ALL,
        page: Int = 0,
        pageSize: Int = 20
    ): Result<List<SongEntity>>

    suspend fun getFeaturedTracks(
        genreOrCategory: String? = null,
        page: Int = 0,
        pageSize: Int = 20
    ): Result<List<SongEntity>>
}
