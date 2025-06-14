package com.smileidentity.sample.database.di

import android.content.Context
import androidx.room.Room
import com.smileidentity.sample.database.SmileIDDatabase
import com.smileidentity.sample.database.dao.JobsDao
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
    fun providesDatabase(@ApplicationContext context: Context): SmileIDDatabase =
        Room.databaseBuilder(
            context = context,
            klass = SmileIDDatabase::class.java,
            name = "smileid",
        )
            .build()

    @Provides
    fun providesJobsDao(database: SmileIDDatabase): JobsDao = database.jobsDao()
}
