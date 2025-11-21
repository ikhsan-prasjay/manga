package com.example.mangakomik.ui.theme

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
// 2. API CLIENT
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
// 3. LOGIKA NAVIGASI (DASHBOARD <-> DETAIL)
// ==========================================

@Composable
fun MangaDashboardScreen(
    isDarkTheme: Boolean,
    onThemeChange: () -> Unit,
    onLanguageChange: () -> Unit
) {
    // State: Menyimpan Manga yang sedang diklik
    var selectedManga by remember { mutableStateOf<MangaData?>(null) }

    // Logika Navigasi Sederhana
    if (selectedManga != null) {
        // Tampilkan Halaman Detail
        DetailMangaScreen(
            manga = selectedManga!!,
            onBack = { selectedManga = null } // Tombol kembali ditekan
        )
    } else {
        // Tampilkan Halaman Utama (Dashboard)
        MangaListContent(
            isDarkTheme, onThemeChange, onLanguageChange,
            onMangaClick = { manga -> selectedManga = manga }
        )
    }
}

// ==========================================
// 4. HALAMAN DASHBOARD + SEARCH
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaListContent(
    isDarkTheme: Boolean,
    onThemeChange: () -> Unit,
    onLanguageChange: () -> Unit,
    onMangaClick: (MangaData) -> Unit
) {
    var mangaList by remember { mutableStateOf<List<MangaData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // SEARCH STATE
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

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

    // Filter List berdasarkan Search
    val filteredList = if (searchQuery.isEmpty()) {
        mangaList
    } else {
        mangaList.filter {
            val title = it.attributes.title["en"] ?: it.attributes.title.values.firstOrNull() ?: ""
            title.contains(searchQuery, ignoreCase = true)
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearching) {
                        // Search Bar
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search Manga...") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(stringResource(id = R.string.app_title), fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    // Tombol Search
                    IconButton(onClick = {
                        isSearching = !isSearching
                        if (!isSearching) searchQuery = "" // Reset search saat ditutup
                    }) {
                        Icon(if (isSearching) Icons.Default.Close else Icons.Default.Search, "Search")
                    }
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
            if (isLandscape) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)
                ) {
                    items(filteredList) { manga ->
                        MangaLandscapeCard(manga, onClick = { onMangaClick(manga) })
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 12.dp, top = padding.calculateTopPadding() + 12.dp, end = 12.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
                ) {
                    items(filteredList) { manga ->
                        MangaPortraitCard(manga, onClick = { onMangaClick(manga) })
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. HALAMAN DETAIL (MIRIP GAMBAR MANGAPLUS)
// ==========================================
// ==========================================
// 5. HALAMAN DETAIL (REVISI: CHAPTER STYLE POSTER)
// ==========================================

@Composable
fun DetailMangaScreen(manga: MangaData, onBack: () -> Unit) {
    BackHandler { onBack() }

    val title = manga.attributes.title["en"] ?: manga.attributes.title.values.firstOrNull() ?: "No Title"
    val imageUrl = getImageUrl(manga)
    val synopsis = manga.manualSynopsis ?: manga.attributes.description?.get("en") ?: "Sinopsis belum tersedia."

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // --- HEADER (Sama seperti sebelumnya) ---
        Box(modifier = Modifier.height(350.dp).fillMaxWidth()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(imageUrl).crossfade(true).build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.4f
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.background)))
            )

            IconButton(
                onClick = onBack,
                modifier = Modifier.padding(16.dp).align(Alignment.TopStart).background(Color.Black.copy(0.5f), RequestTheme.shape)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            Row(
                modifier = Modifier.align(Alignment.BottomStart).padding(20.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    modifier = Modifier.width(100.dp).height(150.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(imageUrl).build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("+ Favorit", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // --- SINOPSIS ---
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Ikhtisar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(synopsis, style = MaterialTheme.typography.bodyMedium, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // --- DAFTAR CHAPTER (DESIGN BARU) ---
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
            Text(
                text = "Daftar Bab",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Loop Chapter dengan Design Poster
            for (i in 1..10) {
                // Kita kirim imageUrl ke ChapterItem agar bisa jadi background
                ChapterPosterItem(number = i, imageUrl = imageUrl)
            }
        }
        Spacer(modifier = Modifier.height(50.dp))
    }
}

// Design Item Chapter Baru (Gaya Poster)
@Composable
fun ChapterPosterItem(number: Int, imageUrl: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(85.dp) // Tinggi card diperbesar agar elegan
            .padding(vertical = 6.dp)
            .clickable { },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Background Image (Buram)
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(imageUrl).crossfade(true).build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.3f // Gambar samar-samar saja
            )

            // 2. Gradasi Hitam (Agar teks putih terbaca jelas)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent),
                            startX = 0f,
                            endX = 700f // Gradasi dari kiri ke kanan
                        )
                    )
            )

            // 3. Konten Teks & Icon
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    // Nomor Chapter (Warna Emas)
                    Text(
                        text = "#00$number",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFFFFD700), // Warna Emas Mewah
                        fontWeight = FontWeight.Black,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                    // Judul Chapter
                    Text(
                        text = "The Beginning of Legend",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Icon Play/Baca
                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(50))
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Read",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

// ==========================================
// 6. HELPER CARDS (MODIFIED WITH ONCLICK)
// ==========================================

// Helper RequestTheme (Hanya dummy object biar gak error di copy paste)
object RequestTheme { val shape = RoundedCornerShape(50) }

@Composable
fun MangaPortraitCard(manga: MangaData, onClick: () -> Unit) {
    val title = manga.attributes.title["en"] ?: manga.attributes.title.values.firstOrNull() ?: "No Title"
    val imageUrl = getImageUrl(manga)
    val rating = manga.manualRating ?: remember { String.format("%.1f", Random.nextDouble(4.0, 5.0)) }
    val statusRaw = manga.attributes.status ?: "unknown"
    val isOngoing = statusRaw.equals("ongoing", ignoreCase = true)
    val statusText = if (isOngoing) stringResource(R.string.status_ongoing) else stringResource(R.string.status_completed)
    val badgeColor = if (isOngoing) Color(0xFFFF2E2E) else Color(0xFF00C853)

    Card(
        modifier = Modifier.fillMaxWidth().height(280.dp).clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(imageUrl).crossfade(true).addHeader("User-Agent", "Mozilla/5.0").build(),
                contentDescription = title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
            )
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)), startY = 300f)))
            Box(modifier = Modifier.padding(8.dp).clip(RoundedCornerShape(4.dp)).background(badgeColor).align(Alignment.TopStart).padding(horizontal = 6.dp, vertical = 2.dp)) {
                Text(statusText, color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
            Box(modifier = Modifier.padding(8.dp).clip(RoundedCornerShape(4.dp)).background(Color.Black.copy(alpha = 0.6f)).align(Alignment.TopEnd).padding(horizontal = 6.dp, vertical = 2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(rating, color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun MangaLandscapeCard(manga: MangaData, onClick: () -> Unit) {
    val title = manga.attributes.title["en"] ?: manga.attributes.title.values.firstOrNull() ?: "No Title"
    val imageUrl = getImageUrl(manga)
    val rating = manga.manualRating ?: remember { String.format("%.1f", Random.nextDouble(4.0, 5.0)) }
    val synopsis = manga.manualSynopsis ?: manga.attributes.description?.get("en") ?: "No synopsis available."

    Card(
        modifier = Modifier.fillMaxWidth().height(160.dp).clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(imageUrl).crossfade(true).addHeader("User-Agent", "Mozilla/5.0").build(),
                contentDescription = title,
                modifier = Modifier.width(110.dp).fillMaxHeight(),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "${stringResource(R.string.score)}: $rating ★", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = synopsis, style = MaterialTheme.typography.bodySmall, maxLines = 4, overflow = TextOverflow.Ellipsis, lineHeight = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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