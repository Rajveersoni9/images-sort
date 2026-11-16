package com.mediaflow.ai.domain.model

import android.net.Uri

enum class SmartAlbumType {
    ALL,
    FAVORITES,
    VIDEOS,
    PEOPLE,
    TRAVEL,
    NATURE,
    ANIMALS,
    CARS,
    FOOD,
    PETS,
    DOCUMENTS,
    SCREENSHOTS,
    WALLPAPERS,
    MEMES,
    SELFIES,
    DUPLICATES
}

data class SmartAlbum(
    val id: String,
    val title: String,
    val type: SmartAlbumType,
    val coverUri: Uri?,
    val itemCount: Int,
    val description: String,
    val gradientColors: Pair<String, String> = Pair("#7C3AED", "#2563EB")
)
