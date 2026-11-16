package com.mediaflow.ai.domain.repository

import com.mediaflow.ai.domain.model.ActivityTask
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {
    fun getActivityHistory(): Flow<List<ActivityTask>>
    suspend fun logTask(task: ActivityTask)
    suspend fun clearHistory()
}
