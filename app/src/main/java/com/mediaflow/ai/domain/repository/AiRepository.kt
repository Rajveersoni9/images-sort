package com.mediaflow.ai.domain.repository

import com.mediaflow.ai.domain.model.MediaItem
import kotlinx.coroutines.flow.Flow

interface AiRepository {
    suspend fun analyzeMedia(mediaItem: MediaItem): MediaItem
    suspend fun runBatchClassification(): Int
    suspend fun detectDuplicates(): Int
    val isProcessingFlow: Flow<Boolean>
    val realTimeNotificationFlow: Flow<String?>
}
