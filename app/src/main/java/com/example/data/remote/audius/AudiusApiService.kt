package com.example.data.remote.audius

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface AudiusApiService {
    @GET("v1/tracks/search")
    suspend fun searchTracks(
        @Query("query") query: String,
        @Query("app_name") appName: String = "ai_studio_music",
        @Query("limit") limit: Int = 30
    ): Response<AudiusTracksResponse>

    @GET("v1/tracks/trending")
    suspend fun getTrendingTracks(
        @Query("genre") genre: String? = null,
        @Query("app_name") appName: String = "ai_studio_music",
        @Query("limit") limit: Int = 30
    ): Response<AudiusTracksResponse>
}
