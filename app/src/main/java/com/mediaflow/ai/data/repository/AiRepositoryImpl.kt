package com.mediaflow.ai.data.repository

import android.net.Uri
import com.mediaflow.ai.ai.AiPipeline
import com.mediaflow.ai.ai.PerceptualHash
import com.mediaflow.ai.data.local.dao.ActivityTaskDao
import com.mediaflow.ai.data.local.dao.AlbumDao
import com.mediaflow.ai.data.local.dao.MediaDao
import com.mediaflow.ai.data.local.entity.ActivityTaskEntity
import com.mediaflow.ai.domain.model.ActivityTask
import com.mediaflow.ai.domain.model.MediaItem
import com.mediaflow.ai.domain.model.TaskStatus
import com.mediaflow.ai.domain.model.TaskType
import com.mediaflow.ai.domain.repository.AiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiRepositoryImpl @Inject constructor(
    private val mediaDao: MediaDao,
    private val albumDao: AlbumDao,
    private val activityTaskDao: ActivityTaskDao,
    private val aiPipeline: AiPipeline
) : AiRepository {

    private val _isProcessing = MutableStateFlow(false)
    override val isProcessingFlow: Flow<Boolean> = _isProcessing.asStateFlow()

    private val _realTimeNotification = MutableStateFlow<String?>(null)
    override val realTimeNotificationFlow: Flow<String?> = _realTimeNotification.asStateFlow()

    override suspend fun analyzeMedia(mediaItem: MediaItem): MediaItem = withContext(Dispatchers.IO) {
        val entity = mediaDao.getMediaById(mediaItem.id) ?: return@withContext mediaItem
        val processed = aiPipeline.processMedia(entity)
        mediaDao.update(processed)
        mediaItem.copy(
            aiTags = processed.aiTags,
            sceneCategory = processed.sceneCategory,
            detectedFacesCount = processed.detectedFacesCount,
            ocrText = processed.ocrText,
            dominantColorHex = processed.dominantColorHex,
            perceptualHash = processed.perceptualHash
        )
    }

    override suspend fun runBatchClassification(): Int = withContext(Dispatchers.IO) {
        val unprocessed = mediaDao.getUnprocessedMedia(limit = 30)
        if (unprocessed.isEmpty()) return@withContext 0

        _isProcessing.value = true
        var count = 0
        try {
            val processedEntities = unprocessed.map { entity ->
                count++
                aiPipeline.processMedia(entity)
            }
            mediaDao.insertAll(processedEntities)

            // Log activity task
            activityTaskDao.insertOrUpdate(
                ActivityTaskEntity(
                    id = UUID.randomUUID().toString(),
                    title = "$count photos organized",
                    subtitle = "AI tagged & sorted into smart albums",
                    taskType = TaskType.REALTIME_SORT,
                    status = TaskStatus.COMPLETED,
                    progress = 1.0f,
                    itemsAffected = count,
                    timestamp = System.currentTimeMillis()
                )
            )

            // Post real-time floating notification
            _realTimeNotification.value = "$count new photos organized"
            delay(4000)
            _realTimeNotification.value = null
        } finally {
            _isProcessing.value = false
        }
        count
    }

    override suspend fun detectDuplicates(): Int = withContext(Dispatchers.IO) {
        val allMedia = mediaDao.getUnprocessedMedia(200)
        var duplicatesFound = 0
        for (i in allMedia.indices) {
            val hash1 = allMedia[i].perceptualHash ?: continue
            for (j in i + 1 until allMedia.size) {
                val hash2 = allMedia[j].perceptualHash ?: continue
                val dist = PerceptualHash.hammingDistance(hash1, hash2)
                if (dist <= 5) {
                    val groupId = "dup_${allMedia[i].id}"
                    mediaDao.update(allMedia[i].copy(isDuplicate = true, duplicateGroupId = groupId))
                    mediaDao.update(allMedia[j].copy(isDuplicate = true, duplicateGroupId = groupId))
                    duplicatesFound++
                }
            }
        }
        if (duplicatesFound > 0) {
            activityTaskDao.insertOrUpdate(
                ActivityTaskEntity(
                    id = UUID.randomUUID().toString(),
                    title = "$duplicatesFound duplicates detected",
                    subtitle = "Found near-identical shots",
                    taskType = TaskType.DUPLICATE_SCAN,
                    status = TaskStatus.COMPLETED,
                    progress = 1.0f,
                    itemsAffected = duplicatesFound,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        duplicatesFound
    }
}
