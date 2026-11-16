package com.mediaflow.ai.data.repository

import com.mediaflow.ai.data.local.dao.ActivityTaskDao
import com.mediaflow.ai.data.local.entity.ActivityTaskEntity
import com.mediaflow.ai.domain.model.ActivityTask
import com.mediaflow.ai.domain.repository.ActivityRepository
import com.mediaflow.ai.util.SampleDataGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityRepositoryImpl @Inject constructor(
    private val activityTaskDao: ActivityTaskDao
) : ActivityRepository {

    override fun getActivityHistory(): Flow<List<ActivityTask>> {
        return activityTaskDao.getAllTasksFlow().map { entities ->
            if (entities.isEmpty()) {
                activityTaskDao.insertAll(SampleDataGenerator.generateSampleTasks())
            }
            entities.map { entity ->
                ActivityTask(
                    id = entity.id,
                    title = entity.title,
                    subtitle = entity.subtitle,
                    taskType = entity.taskType,
                    status = entity.status,
                    progress = entity.progress,
                    itemsAffected = entity.itemsAffected,
                    timestamp = entity.timestamp
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun logTask(task: ActivityTask) = withContext(Dispatchers.IO) {
        activityTaskDao.insertOrUpdate(
            ActivityTaskEntity(
                id = task.id,
                title = task.title,
                subtitle = task.subtitle,
                taskType = task.taskType,
                status = task.status,
                progress = task.progress,
                itemsAffected = task.itemsAffected,
                timestamp = task.timestamp
            )
        )
    }

    override suspend fun clearHistory() = withContext(Dispatchers.IO) {
        activityTaskDao.clearAll()
    }
}
