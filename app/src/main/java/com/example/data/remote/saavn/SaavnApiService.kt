package com.example.data.remote.saavn

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SaavnApiService {
    @GET("api.php")
    suspend fun searchSongsDirect(
        @Query("__call") call: String = "search.getResults",
        @Query("_format") format: String = "json",
        @Query("_marker") marker: String = "0",
        @Query("cc") cc: String = "in",
        @Query("includeMetaTags") includeMetaTags: String = "1",
        @Query("p") page: Int = 1,
        @Query("n") limit: Int = 40,
        @Query("q") query: String
    ): Response<JioSaavnDirectSearchResponse>
}
