package com.mediaflow.ai.domain.usecase

import com.mediaflow.ai.domain.model.MediaItem
import com.mediaflow.ai.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchMediaUseCase @Inject constructor(
    private val repository: SearchRepository
) {
    operator fun invoke(query: String): Flow<List<MediaItem>> = repository.search(query)
    suspend fun getPopularTags(): List<String> = repository.getPopularTags()
}
