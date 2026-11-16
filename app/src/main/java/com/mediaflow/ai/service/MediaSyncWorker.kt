package com.mediaflow.ai.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mediaflow.ai.domain.usecase.RunAiOrganizationUseCase
import com.mediaflow.ai.domain.usecase.SyncMediaStoreUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class MediaSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncMediaStoreUseCase: SyncMediaStoreUseCase,
    private val runAiOrganizationUseCase: RunAiOrganizationUseCase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            syncMediaStoreUseCase()
            runAiOrganizationUseCase.runBatchClassification()
            runAiOrganizationUseCase.detectDuplicates()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
