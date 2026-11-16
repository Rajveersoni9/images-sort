package com.mediaflow.ai.domain.usecase

import com.mediaflow.ai.domain.repository.AiRepository
import com.mediaflow.ai.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    suspend operator fun invoke(id: Long, isFavorite: Boolean) {
        repository.toggleFavorite(id, isFavorite)
    }
}

class DeleteMediaUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    suspend operator fun invoke(ids: List<Long>) {
        repository.deleteMedia(ids)
    }
}

class SyncMediaStoreUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    suspend operator fun invoke(): Int = repository.syncMediaStore()
}

class RunAiOrganizationUseCase @Inject constructor(
    private val aiRepository: AiRepository
) {
    suspend fun runBatchClassification(): Int = aiRepository.runBatchClassification()
    suspend fun detectDuplicates(): Int = aiRepository.detectDuplicates()
    val isProcessing: Flow<Boolean> = aiRepository.isProcessingFlow
    val notificationBanner: Flow<String?> = aiRepository.realTimeNotificationFlow
}
