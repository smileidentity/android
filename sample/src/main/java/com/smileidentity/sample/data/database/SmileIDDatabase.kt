package com.smileidentity.sample.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.smileidentity.sample.data.database.dao.JobsDao
import com.smileidentity.sample.data.database.model.Jobs

@Database(
    entities = [
        Jobs::class,
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
}
