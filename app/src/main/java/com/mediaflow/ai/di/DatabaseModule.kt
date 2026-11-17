package com.mediaflow.ai.di

import android.content.Context
import androidx.room.Room
import com.mediaflow.ai.data.local.MediaFlowDatabase
import com.mediaflow.ai.data.local.dao.ActivityTaskDao
import com.mediaflow.ai.data.local.dao.AlbumDao
import com.mediaflow.ai.data.local.dao.MediaDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMediaFlowDatabase(
        @ApplicationContext context: Context
    ): MediaFlowDatabase {
        return Room.databaseBuilder(
            context,
            MediaFlowDatabase::class.java,
            "mediaflow.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideMediaDao(db: MediaFlowDatabase): MediaDao = db.mediaDao()

    @Provides
    fun provideAlbumDao(db: MediaFlowDatabase): AlbumDao = db.albumDao()

    @Provides
    fun provideActivityTaskDao(db: MediaFlowDatabase): ActivityTaskDao = db.activityTaskDao()
}
