package com.example.data.remote.audius

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AudiusTracksResponse(
    @Json(name = "data") val data: List<AudiusTrackDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class AudiusTrackDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "duration") val duration: Long? = null,
    @Json(name = "genre") val genre: String? = null,
    @Json(name = "user") val user: AudiusUserDto? = null,
    @Json(name = "artwork") val artwork: AudiusArtworkDto? = null,
    @Json(name = "permalink") val permalink: String? = null
)

@JsonClass(generateAdapter = true)
data class AudiusUserDto(
    @Json(name = "name") val name: String? = null,
    @Json(name = "handle") val handle: String? = null
)

@JsonClass(generateAdapter = true)
data class AudiusArtworkDto(
    @Json(name = "150x150") val art150: String? = null,
    @Json(name = "480x480") val art480: String? = null,
    @Json(name = "1000x1000") val art1000: String? = null
)
