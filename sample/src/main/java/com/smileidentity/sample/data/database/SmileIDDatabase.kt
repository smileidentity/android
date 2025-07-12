package com.smileidentity.sample.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.smileidentity.sample.data.database.dao.ConfigDao
import com.smileidentity.sample.data.database.dao.JobsDao
import com.smileidentity.sample.data.database.model.Config
import com.smileidentity.sample.data.database.model.Job

@Database(
    entities = [
        Job::class,
        Config::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(
    JobTypeConverter::class,
    StringListTypeConverter::class,
)
abstract class SmileIDDatabase : RoomDatabase() {
    abstract fun jobsDao(): JobsDao

    abstract fun configDao(): ConfigDao
}
