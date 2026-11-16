package com.mediaflow.ai.data.repository

import android.net.Uri
import com.mediaflow.ai.ai.PythonBridge
import com.mediaflow.ai.data.local.dao.MediaDao
import com.mediaflow.ai.domain.model.MediaItem
import com.mediaflow.ai.domain.repository.SearchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val mediaDao: MediaDao,
    private val pythonBridge: PythonBridge
) : SearchRepository {

    override fun search(query: String): Flow<List<MediaItem>> {
        return mediaDao.searchMedia(query.trim()).map { list ->
            val mappedItems = list.map { entity ->
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

            // Attempt Python Semantic Ranker if available
            val jsonArray = JSONArray()
            mappedItems.forEach { item ->
                val obj = JSONObject()
                obj.put("id", item.id)
                obj.put("displayName", item.displayName)
                obj.put("sceneCategory", item.sceneCategory.name)
                obj.put("aiTags", JSONArray(item.aiTags))
                obj.put("ocrText", item.ocrText ?: "")
                jsonArray.put(obj)
            }

            val rankedIds = pythonBridge.rankSearchResults(query, jsonArray.toString())
            if (!rankedIds.isNullOrEmpty()) {
                val itemMap = mappedItems.associateBy { it.id }
                rankedIds.mapNotNull { itemMap[it] }
            } else {
                mappedItems
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
