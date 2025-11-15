package com.example.mangakomik.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mangakomik.R
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// --- DATA & API (Sama seperti sebelumnya) ---
data class MangaResponse(val data: List<MangaData>)
data class MangaData(val mal_id: Int, val title: String, val score: Double?, val members: Int?, val images: MangaImages, val synopsis: String?)
data class MangaImages(val jpg: JpgImage)
data class JpgImage(val image_url: String)

interface JikanApiService {
    @GET("top/manga")
    suspend fun getTopManga(): MangaResponse
}

object RetrofitClient {
    val instance: JikanApiService by lazy {
        Retrofit.Builder().baseUrl("https://api.jikan.moe/v4/")
            .addConverterFactory(GsonConverterFactory.create()).build().create(JikanApiService::class.java)
    }
}

// --- TAMPILAN KEREN ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaDashboardScreen(
    isDarkTheme: Boolean,
    onThemeChange: () -> Unit,
    onLanguageChange: () -> Unit
) {
    var mangaList by remember { mutableStateOf<List<MangaData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            mangaList = RetrofitClient.instance.getTopManga().data
            isLoading = false
        } catch (e: Exception) { e.printStackTrace(); isLoading = false }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.app_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Tombol Ganti Bahasa
                    IconButton(onClick = onLanguageChange) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = "Switch Language")
                    }
                    // Tombol Ganti Tema (Siang/Malam)
                    IconButton(onClick = onThemeChange) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Switch Theme"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                contentPadding = padding,
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)
            ) {
                items(mangaList) { manga ->
                    BeautifulMangaCard(manga)
                }
            }
        }
    }
}

@Composable
fun BeautifulMangaCard(manga: MangaData) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(160.dp), // Tinggi fix agar rapi
        elevation = CardDefaults.cardElevation(6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Gambar Cover
            AsyncImage(
                model = manga.images.jpg.image_url,
                contentDescription = null,
                modifier = Modifier
                    .weight(0.35f)
                    .fillMaxHeight(),
                contentScale = ContentScale.Crop
            )

            // Informasi Komik
            Column(
                modifier = Modifier
                    .weight(0.65f)
                    .padding(12.dp)
            ) {
                // Judul
                Text(
                    text = manga.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Rating Bintang
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107), // Warna Emas
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${manga.score ?: "N/A"}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(${manga.members ?: 0} ${stringResource(R.string.members)})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Sinopsis Singkat
                Text(
                    text = manga.synopsis ?: stringResource(R.string.synopsis_unavailable),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )
            }
        }
    }
}