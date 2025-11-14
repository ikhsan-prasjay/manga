package com.example.mangakomik.ui.theme

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// Struktur Data Manga
data class MangaResponse(val data: List<MangaData>)
data class MangaData(
    val mal_id: Int,
    val title: String,
    val score: Double?,
    val images: MangaImages
)
data class MangaImages(val jpg: JpgImage)
data class JpgImage(val image_url: String)

// Penghubung API
interface JikanApiService {
    @GET("top/manga")
    suspend fun getTopManga(): MangaResponse
}

object RetrofitClient {
    val instance: JikanApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.jikan.moe/v4/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(JikanApiService::class.java)
    }
}