package com.mediaflow.ai.data.repository

import android.net.Uri
import com.mediaflow.ai.data.local.dao.MediaDao
import com.mediaflow.ai.domain.model.MediaItem
import com.mediaflow.ai.domain.repository.SearchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val mediaDao: MediaDao
) : SearchRepository {

    override fun search(query: String): Flow<List<MediaItem>> {
        return mediaDao.searchMedia(query.trim()).map { list ->
            list.map { entity ->
                MediaItem(
                    id = entity.id,
                    uri = Uri.parse(entity.uriString),
                    path = entity.path,
                    displayName = entity.displayName,
                    mimeType = entity.mimeType,
                    mediaType = entity.mediaType,
                    dateAdded = entity.dateAdded,
                    dateTaken = entity.dateTaken,
                    durationMs = entity.durationMs,
                    sizeBytes = entity.sizeBytes,
                    width = entity.width,
                    height = entity.height,
                    isFavorite = entity.isFavorite,
                    isDuplicate = entity.isDuplicate,
                    duplicateGroupId = entity.duplicateGroupId,
                    aiTags = entity.aiTags,
                    detectedFacesCount = entity.detectedFacesCount,
                    ocrText = entity.ocrText,
                    sceneCategory = entity.sceneCategory,
                    dominantColorHex = entity.dominantColorHex,
                    perceptualHash = entity.perceptualHash
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getPopularTags(): List<String> {
        return listOf(
            "Dog", "Sunset", "Red Car", "Beach", "Birthday",
            "Passport", "Mountain", "Laptop", "Blue Shirt", "Night Sky",
            "Receipt", "Selfie", "Nature", "Burger", "Coffee"
        )
    }
}
