package com.smileidentity.sample.data.database.dao

import BaseDao
import androidx.room.Dao
import androidx.room.Query
import com.smileidentity.sample.data.database.model.Job
import kotlinx.coroutines.flow.Flow

@Dao
interface JobsDao : BaseDao<Job> {

    @Query("SELECT * FROM job")
    fun fetchJobs(): Flow<List<Job>>
}
