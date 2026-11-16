package com.mediaflow.ai.domain.repository

import com.mediaflow.ai.domain.model.MediaItem
import com.mediaflow.ai.domain.model.MediaType
import com.mediaflow.ai.domain.model.SceneCategory
import com.mediaflow.ai.domain.model.SmartAlbum
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    fun getAllMedia(): Flow<List<MediaItem>>
    fun getFavoriteMedia(): Flow<List<MediaItem>>
    fun getMediaByType(type: MediaType): Flow<List<MediaItem>>
    fun getMediaByCategory(category: SceneCategory): Flow<List<MediaItem>>
    fun getDuplicates(): Flow<List<MediaItem>>
    fun getSmartAlbums(): Flow<List<SmartAlbum>>
    suspend fun getMediaById(id: Long): MediaItem?
    suspend fun toggleFavorite(id: Long, isFavorite: Boolean)
    suspend fun deleteMedia(ids: List<Long>)
    suspend fun syncMediaStore(): Int
    suspend fun refreshSmartAlbums()
}
