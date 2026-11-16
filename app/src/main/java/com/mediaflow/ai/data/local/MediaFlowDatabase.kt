package com.mediaflow.ai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mediaflow.ai.data.local.dao.ActivityTaskDao
import com.mediaflow.ai.data.local.dao.AlbumDao
import com.mediaflow.ai.data.local.dao.MediaDao
import com.mediaflow.ai.data.local.entity.ActivityTaskEntity
import com.mediaflow.ai.data.local.entity.AlbumEntity
import com.mediaflow.ai.data.local.entity.MediaEntity

@Database(
    entities = [
        MediaEntity::class,
        AlbumEntity::class,
        ActivityTaskEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MediaFlowDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao
    abstract fun albumDao(): AlbumDao
    abstract fun activityTaskDao(): ActivityTaskDao
}
