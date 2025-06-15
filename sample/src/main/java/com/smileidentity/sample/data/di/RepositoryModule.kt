package com.smileidentity.sample.data.di

import com.smileidentity.sample.data.repository.JobsDataSource
import com.smileidentity.sample.data.repository.JobsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun provideJobsRepository(repository: JobsDataSource): JobsRepository
}
