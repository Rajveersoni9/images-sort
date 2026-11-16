package com.mediaflow.ai.di

import com.mediaflow.ai.data.repository.ActivityRepositoryImpl
import com.mediaflow.ai.data.repository.AiRepositoryImpl
import com.mediaflow.ai.data.repository.MediaRepositoryImpl
import com.mediaflow.ai.data.repository.SearchRepositoryImpl
import com.mediaflow.ai.data.repository.SettingsRepositoryImpl
import com.mediaflow.ai.domain.repository.ActivityRepository
import com.mediaflow.ai.domain.repository.AiRepository
import com.mediaflow.ai.domain.repository.MediaRepository
import com.mediaflow.ai.domain.repository.SearchRepository
import com.mediaflow.ai.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMediaRepository(impl: MediaRepositoryImpl): MediaRepository

    @Binds
    @Singleton
    abstract fun bindSearchRepository(impl: SearchRepositoryImpl): SearchRepository

    @Binds
    @Singleton
    abstract fun bindAiRepository(impl: AiRepositoryImpl): AiRepository

    @Binds
    @Singleton
    abstract fun bindActivityRepository(impl: ActivityRepositoryImpl): ActivityRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
