package com.example.data.remote.saavn

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class JioSaavnDirectSearchResponse(
    @Json(name = "total") val total: Any? = null,
    @Json(name = "start") val start: Any? = null,
    @Json(name = "results") val results: List<JioSaavnDirectSongDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class JioSaavnDirectSongDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "song") val song: String? = null,
    @Json(name = "album") val album: String? = null,
    @Json(name = "year") val year: String? = null,
    @Json(name = "music") val music: String? = null,
    @Json(name = "singers") val singers: String? = null,
    @Json(name = "primary_artists") val primaryArtists: String? = null,
    @Json(name = "featured_artists") val featuredArtists: String? = null,
    @Json(name = "image") val image: String? = null,
    @Json(name = "label") val label: String? = null,
    @Json(name = "language") val language: String? = null,
    @Json(name = "duration") val duration: String? = null,
    @Json(name = "encrypted_media_url") val encryptedMediaUrl: String? = null,
    @Json(name = "perma_url") val permaUrl: String? = null
)
