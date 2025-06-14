package com.smileidentity.sample.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.smileidentity.sample.database.dao.JobsDao
import com.smileidentity.sample.database.model.Jobs

@Database(
    entities = [
        Jobs::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class SmileIDDatabase : RoomDatabase() {
    abstract fun jobsDao(): JobsDao
}
