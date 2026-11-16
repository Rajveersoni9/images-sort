package com.mediaflow.ai.data.local

import androidx.room.TypeConverter
import com.mediaflow.ai.domain.model.MediaType
import com.mediaflow.ai.domain.model.SceneCategory
import com.mediaflow.ai.domain.model.SmartAlbumType
import com.mediaflow.ai.domain.model.TaskStatus
import com.mediaflow.ai.domain.model.TaskType

class Converters {
    @TypeConverter
    fun fromMediaType(value: MediaType): String = value.name

    @TypeConverter
    fun toMediaType(value: String): MediaType = runCatching { MediaType.valueOf(value) }.getOrDefault(MediaType.IMAGE)

    @TypeConverter
    fun fromSceneCategory(value: SceneCategory): String = value.name

    @TypeConverter
    fun toSceneCategory(value: String): SceneCategory = runCatching { SceneCategory.valueOf(value) }.getOrDefault(SceneCategory.GENERAL)

    @TypeConverter
    fun fromSmartAlbumType(value: SmartAlbumType): String = value.name

    @TypeConverter
    fun toSmartAlbumType(value: String): SmartAlbumType = runCatching { SmartAlbumType.valueOf(value) }.getOrDefault(SmartAlbumType.ALL)

    @TypeConverter
    fun fromTaskStatus(value: TaskStatus): String = value.name

    @TypeConverter
    fun toTaskStatus(value: String): TaskStatus = runCatching { TaskStatus.valueOf(value) }.getOrDefault(TaskStatus.COMPLETED)

    @TypeConverter
    fun fromTaskType(value: TaskType): String = value.name

    @TypeConverter
    fun toTaskType(value: String): TaskType = runCatching { TaskType.valueOf(value) }.getOrDefault(TaskType.REALTIME_SORT)

    @TypeConverter
    fun fromStringList(value: List<String>): String = value.joinToString(separator = "|||")

    @TypeConverter
    fun toStringList(value: String): List<String> = if (value.isBlank()) emptyList() else value.split("|||")
}
