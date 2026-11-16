package com.mediaflow.ai.domain.repository

import com.mediaflow.ai.domain.model.MediaItem
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    fun search(query: String): Flow<List<MediaItem>>
    suspend fun getPopularTags(): List<String>
}
