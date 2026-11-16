package com.mediaflow.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mediaflow.ai.data.local.entity.MediaEntity
import com.mediaflow.ai.domain.model.MediaType
import com.mediaflow.ai.domain.model.SceneCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("SELECT * FROM media_items ORDER BY dateTaken DESC")
    fun getAllMediaFlow(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE id = :id LIMIT 1")
    suspend fun getMediaById(id: Long): MediaEntity?

    @Query("SELECT * FROM media_items WHERE isFavorite = 1 ORDER BY dateTaken DESC")
    fun getFavoriteMediaFlow(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE mediaType = :type ORDER BY dateTaken DESC")
    fun getMediaByTypeFlow(type: MediaType): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE sceneCategory = :category ORDER BY dateTaken DESC")
    fun getMediaBySceneCategoryFlow(category: SceneCategory): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE isDuplicate = 1 ORDER BY duplicateGroupId, dateTaken DESC")
    fun getDuplicatesFlow(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE isProcessedByAi = 0 ORDER BY dateAdded DESC LIMIT :limit")
    suspend fun getUnprocessedMedia(limit: Int = 50): List<MediaEntity>

    @Query("""
        SELECT * FROM media_items 
        WHERE displayName LIKE '%' || :query || '%' 
           OR aiTags LIKE '%' || :query || '%' 
           OR ocrText LIKE '%' || :query || '%'
        ORDER BY dateTaken DESC
    """)
    fun searchMedia(query: String): Flow<List<MediaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(media: MediaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mediaList: List<MediaEntity>)

    @Update
    suspend fun update(media: MediaEntity)

    @Query("UPDATE media_items SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("DELETE FROM media_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM media_items WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM media_items")
    suspend fun getMediaCount(): Int

    @Query("SELECT COUNT(*) FROM media_items WHERE isFavorite = 1")
    suspend fun getFavoritesCount(): Int

    @Query("SELECT COUNT(*) FROM media_items WHERE mediaType = 'VIDEO'")
    suspend fun getVideosCount(): Int

    @Query("SELECT COUNT(*) FROM media_items WHERE sceneCategory = :category")
    suspend fun getCategoryCount(category: SceneCategory): Int
}
