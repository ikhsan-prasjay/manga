package com.example.mangakomik

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.core.os.LocaleListCompat
import com.example.mangakomik.ui.theme.MangaDashboardScreen
import com.example.mangakomik.ui.theme.MangakomikTheme

// Gunakan AppCompatActivity agar fitur ganti bahasa lebih mudah
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // State untuk Tema (Default mengikuti sistem)
            var isDarkTheme by remember { mutableStateOf(false) }
            // Cek sistem pertama kali
            val systemDark = isSystemInDarkTheme()
            LaunchedEffect(Unit) { isDarkTheme = systemDark }

            MangakomikTheme(darkTheme = isDarkTheme) {
                MangaDashboardScreen(
                    isDarkTheme = isDarkTheme,
                    onThemeChange = { isDarkTheme = !isDarkTheme }, // Tombol ditekan, tema berubah
                    onLanguageChange = { switchLanguage() }         // Tombol ditekan, bahasa berubah
                )
            }
        }
    }

    // Fungsi Pintar Ganti Bahasa
    private fun switchLanguage() {
        // Cek bahasa sekarang
        val currentLocale = AppCompatDelegate.getApplicationLocales()[0]?.language

        // Jika Inggris ganti Indonesia, Jika Indonesia ganti Inggris
        val newLocale = if (currentLocale == "in") "en" else "in"

        // Terapkan perubahan
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(newLocale))
    }
}