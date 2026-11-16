package com.mediaflow.ai.domain.usecase

import com.mediaflow.ai.domain.model.SmartAlbum
import com.mediaflow.ai.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAlbumsUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke(): Flow<List<SmartAlbum>> = repository.getSmartAlbums()
    suspend fun refresh() = repository.refreshSmartAlbums()
}
