package com.mediaflow.ai.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mediaflow.ai.domain.model.MediaType
import com.mediaflow.ai.domain.model.SceneCategory

@Entity(
    tableName = "media_items",
    indices = [
        Index(value = ["dateTaken"]),
        Index(value = ["mediaType"]),
        Index(value = ["isFavorite"]),
        Index(value = ["sceneCategory"]),
        Index(value = ["perceptualHash"]),
        Index(value = ["isDuplicate"])
    ]
)
data class MediaEntity(
    @PrimaryKey
    val id: Long,
    val uriString: String,
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
    val perceptualHash: Long? = null,
    val isProcessedByAi: Boolean = false
)
