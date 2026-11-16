package com.mediaflow.ai.domain.model

import android.net.Uri

enum class MediaType {
    IMAGE,
    VIDEO
}

enum class SceneCategory {
    NATURE,
    TRAVEL,
    DOCUMENT,
    SCREENSHOT,
    FOOD,
    PEOPLE,
    PETS,
    CARS,
    MEMES,
    ARCHITECTURE,
    NIGHT_SKY,
    WALLPAPER,
    GENERAL
}

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val path: String,
    val displayName: String,
    val mimeType: String,
    val mediaType: MediaType,
    val dateAdded: Long,
    val dateTaken: Long,
    val durationMs: Long = 0L,
    val sizeBytes: Long,
    val width: Int = 0,
    val height: Int = 0,
    val isFavorite: Boolean = false,
    val isDuplicate: Boolean = false,
    val duplicateGroupId: String? = null,
    val aiTags: List<String> = emptyList(),
    val detectedFacesCount: Int = 0,
    val ocrText: String? = null,
    val sceneCategory: SceneCategory = SceneCategory.GENERAL,
    val dominantColorHex: String = "#6750A4",
    val perceptualHash: Long? = null
)
