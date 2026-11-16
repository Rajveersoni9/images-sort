package com.mediaflow.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mediaflow.ai.domain.model.TaskStatus
import com.mediaflow.ai.domain.model.TaskType

@Entity(tableName = "activity_tasks")
data class ActivityTaskEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val subtitle: String,
    val taskType: TaskType,
    val status: TaskStatus,
    val progress: Float,
    val itemsAffected: Int,
    val timestamp: Long
)
