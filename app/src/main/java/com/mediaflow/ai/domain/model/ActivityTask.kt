package com.mediaflow.ai.domain.model

enum class TaskStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED
}

enum class TaskType {
    REALTIME_SORT,
    DUPLICATE_SCAN,
    OCR_EXTRACTION,
    FACE_GROUPING,
    SCENE_CLASSIFICATION,
    MEDIA_IMPORT
}

data class ActivityTask(
    val id: String,
    val title: String,
    val subtitle: String,
    val taskType: TaskType,
    val status: TaskStatus,
    val progress: Float = 1.0f,
    val itemsAffected: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
