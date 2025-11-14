package com.example.mangakomik.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// --- BAGIAN 1: DATA MODEL & API ---
// Kita taruh di sini agar tidak perlu import-import lagi
data class MangaResponse(val data: List<MangaData>)
data class MangaData(
    val mal_id: Int,
    val title: String,
    val score: Double?,
    val images: MangaImages
)
data class MangaImages(val jpg: JpgImage)
data class JpgImage(val image_url: String)

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

// --- BAGIAN 2: TAMPILAN (UI) ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaDashboardScreen() {
    var mangaList by remember { mutableStateOf<List<MangaData>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            mangaList = RetrofitClient.instance.getTopManga().data
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manga Popular") }
            )
        }
    ) { paddingValues ->
        LazyColumn(contentPadding = paddingValues) {
            items(mangaList) { manga ->
                MangaCard(manga)
            }
        }
    }
}

@Composable
fun MangaCard(manga: MangaData) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp).height(120.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row {
            AsyncImage(
                model = manga.images.jpg.image_url,
                contentDescription = null,
                modifier = Modifier.width(80.dp).fillMaxHeight(),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(text = manga.title, style = MaterialTheme.typography.titleMedium)
                Text(text = "Score: ${manga.score ?: "-"}")
            }
        }
    }
}