package com.mediaflow.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mediaflow.ai.data.local.entity.ActivityTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityTaskDao {
    @Query("SELECT * FROM activity_tasks ORDER BY timestamp DESC")
    fun getAllTasksFlow(): Flow<List<ActivityTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(task: ActivityTaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<ActivityTaskEntity>)

    @Query("DELETE FROM activity_tasks")
    suspend fun clearAll()
}
