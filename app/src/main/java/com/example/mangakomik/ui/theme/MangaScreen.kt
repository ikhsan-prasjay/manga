package com.example.mangakomik.ui.theme

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.mangakomik.R
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import kotlin.random.Random

// ==========================================
// 1. DATA MODEL
// ==========================================

data class MangaDexResponse(val data: List<MangaData>)

data class MangaData(
    val id: String,
    val attributes: MangaAttributes,
    val relationships: List<MangaRelationship> = emptyList(),
    val manualImageUrl: String? = null,
    val manualRating: String? = null,
    val manualSynopsis: String? = null
)

data class MangaAttributes(
    val title: Map<String, String>,
    val status: String?,
    val description: Map<String, String>?
)

data class MangaRelationship(
    val type: String,
    val attributes: CoverAttributes? = null
)

data class CoverAttributes(val fileName: String?)

// Data Manual 5 Anime Top (Link OpenLibrary agar aman)
fun getPinnedManga(): List<MangaData> {
    return listOf(
        MangaData("aot",
            MangaAttributes(mapOf("en" to "Attack on Titan"), "completed", null),
            manualImageUrl = "https://covers.openlibrary.org/b/isbn/9781612620244-L.jpg",
            manualRating = "5.0",
            manualSynopsis = "Eren Yeager vows to cleanse the earth of the giant humanoid Titans that have brought humanity to the brink of extinction."
        ),
        MangaData("onepiece",
            MangaAttributes(mapOf("en" to "One Piece"), "ongoing", null),
            manualImageUrl = "https://covers.openlibrary.org/b/isbn/9781569319017-L.jpg",
            manualRating = "5.0",
            manualSynopsis = "Monkey D. Luffy and his pirate crew explore a fantasy world of endless oceans and islands in search of the world's ultimate treasure known as 'One Piece'."
        ),
        MangaData("berserk",
            MangaAttributes(mapOf("en" to "Berserk"), "ongoing", null),
            manualImageUrl = "https://covers.openlibrary.org/b/isbn/9781593070205-L.jpg",
            manualRating = "4.9",
            manualSynopsis = "Guts, a former mercenary now known as the 'Black Swordsman,' is out for revenge. There's a mark on his neck that draws demons to him."
        ),
        MangaData("chainsaw",
            MangaAttributes(mapOf("en" to "Chainsaw Man"), "ongoing", null),
            manualImageUrl = "https://covers.openlibrary.org/b/isbn/9781974709939-L.jpg",
            manualRating = "4.9",
            manualSynopsis = "Denji has a simple dream—to live a happy and peaceful life, spending time with a girl he likes. But reality is harsh."
        ),
        MangaData("naruto",
            MangaAttributes(mapOf("en" to "Naruto"), "completed", null),
            manualImageUrl = "https://covers.openlibrary.org/b/isbn/9781569319000-L.jpg",
            manualRating = "4.8",
            manualSynopsis = "Naruto Uzumaki wants to be the best ninja in the land. He's done very well so far, but with the looming danger posed by the Akatsuki organization..."
        )
    )
}

// ==========================================
// 2. API CONFIGURATION
// ==========================================

interface MangaDexApiService {
    @GET("manga")
    suspend fun getMangaList(
        @Query("limit") limit: Int = 30,
        @Query("includes[]") include: String = "cover_art",
        @Query("order[followedCount]") order: String = "desc",
        @Query("contentRating[]") rating: String = "safe"
    ): MangaDexResponse
}

object RetrofitClient {
    val instance: MangaDexApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.mangadex.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MangaDexApiService::class.java)
    }
}

// ==========================================
// 3. LOGIKA UTAMA (PORTRAIT VS LANDSCAPE)
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaDashboardScreen(
    isDarkTheme: Boolean,
    onThemeChange: () -> Unit,
    onLanguageChange: () -> Unit
) {
    var mangaList by remember { mutableStateOf<List<MangaData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // DETEKSI ORIENTASI LAYAR
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.instance.getMangaList()
            mangaList = getPinnedManga() + response.data
            isLoading = false
        } catch (e: Exception) {
            e.printStackTrace()
            mangaList = getPinnedManga()
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.app_title), fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onLanguageChange) { Icon(Icons.Default.Language, "Bahasa") }
                    IconButton(onClick = onThemeChange) {
                        Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, "Tema")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {

            // --- LOGIKA SWITCH LAYOUT ---
            if (isLandscape) {
                // TAMPILAN LANDSCAPE (Pakai LazyColumn / List ke Bawah)
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    items(mangaList) { manga ->
                        MangaLandscapeCard(manga)
                    }
                }
            } else {
                // TAMPILAN PORTRAIT (Pakai LazyVerticalGrid / Kotak-kotak 2 Kolom)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(
                        start = 12.dp,
                        top = padding.calculateTopPadding() + 12.dp,
                        end = 12.dp,
                        bottom = 12.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    items(mangaList) { manga ->
                        MangaPortraitCard(manga)
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. DESAIN KARTU PORTRAIT (POSTER TEGAK)
// ==========================================
@Composable
fun MangaPortraitCard(manga: MangaData) {
    val title = manga.attributes.title["en"] ?: manga.attributes.title.values.firstOrNull() ?: "No Title"
    val imageUrl = getImageUrl(manga)
    val rating = manga.manualRating ?: remember { String.format("%.1f", Random.nextDouble(4.0, 5.0)) }
    val views = remember { "${Random.nextInt(100, 999)}K" }
    val statusRaw = manga.attributes.status ?: "unknown"
    val isOngoing = statusRaw.equals("ongoing", ignoreCase = true)
    val statusText = if (isOngoing) stringResource(R.string.status_ongoing) else stringResource(R.string.status_completed)
    val badgeColor = if (isOngoing) Color(0xFFFF2E2E) else Color(0xFF00C853)

    Card(
        modifier = Modifier.fillMaxWidth().height(280.dp).clickable { },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(imageUrl).crossfade(true).addHeader("User-Agent", "Mozilla/5.0").build(),
                contentDescription = title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
            )
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)), startY = 300f)))

            // Badge Status
            Box(modifier = Modifier.padding(8.dp).clip(RoundedCornerShape(4.dp)).background(badgeColor).align(Alignment.TopStart).padding(horizontal = 6.dp, vertical = 2.dp)) {
                Text(statusText, color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }

            // Rating
            Box(modifier = Modifier.padding(8.dp).clip(RoundedCornerShape(4.dp)).background(Color.Black.copy(alpha = 0.6f)).align(Alignment.TopEnd).padding(horizontal = 6.dp, vertical = 2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(rating, color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }

            // Info Bawah
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Visibility, null, tint = Color.LightGray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("$views ${stringResource(R.string.views_count)}", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                }
            }
        }
    }
}

// ==========================================
// 5. DESAIN KARTU LANDSCAPE (LIST + SINOPSIS)
// ==========================================
@Composable
fun MangaLandscapeCard(manga: MangaData) {
    val title = manga.attributes.title["en"] ?: manga.attributes.title.values.firstOrNull() ?: "No Title"
    val imageUrl = getImageUrl(manga)
    val rating = manga.manualRating ?: remember { String.format("%.1f", Random.nextDouble(4.0, 5.0)) }
    val synopsis = manga.manualSynopsis ?: manga.attributes.description?.get("en") ?: "No synopsis available."

    Card(
        modifier = Modifier.fillMaxWidth().height(160.dp).clickable { },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Gambar (Kiri)
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(imageUrl).crossfade(true).addHeader("User-Agent", "Mozilla/5.0").build(),
                contentDescription = title,
                modifier = Modifier.width(110.dp).fillMaxHeight(),
                contentScale = ContentScale.Crop
            )

            // Info (Kanan)
            Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${stringResource(R.string.score)}: $rating ★",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = synopsis,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Helper URL
@Composable
fun getImageUrl(manga: MangaData): String {
    return if (manga.manualImageUrl != null) {
        manga.manualImageUrl
    } else {
        val coverFileName = manga.relationships.find { it.type == "cover_art" }?.attributes?.fileName
        if (coverFileName != null) "https://uploads.mangadex.org/covers/${manga.id}/$coverFileName.256.jpg" else ""
    }
}