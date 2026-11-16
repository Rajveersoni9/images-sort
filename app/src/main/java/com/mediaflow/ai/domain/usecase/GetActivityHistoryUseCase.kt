package com.mediaflow.ai.domain.usecase

import com.mediaflow.ai.domain.model.ActivityTask
import com.mediaflow.ai.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActivityHistoryUseCase @Inject constructor(
    private val repository: ActivityRepository
) {
    operator fun invoke(): Flow<List<ActivityTask>> = repository.getActivityHistory()
    suspend fun clear() = repository.clearHistory()
}
