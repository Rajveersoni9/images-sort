package com.mediaflow.ai.domain.usecase

import com.mediaflow.ai.domain.model.MediaItem
import com.mediaflow.ai.domain.model.MediaType
import com.mediaflow.ai.domain.model.SceneCategory
import com.mediaflow.ai.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMediaUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    fun getAll(): Flow<List<MediaItem>> = repository.getAllMedia()
    fun getFavorites(): Flow<List<MediaItem>> = repository.getFavoriteMedia()
    fun getVideos(): Flow<List<MediaItem>> = repository.getMediaByType(MediaType.VIDEO)
    fun getByCategory(category: SceneCategory): Flow<List<MediaItem>> = repository.getMediaByCategory(category)
    fun getDuplicates(): Flow<List<MediaItem>> = repository.getDuplicates()
    suspend fun getById(id: Long): MediaItem? = repository.getMediaById(id)
}
