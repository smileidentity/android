package com.smileidentity.sample.data.database.dao

import BaseDao
import androidx.room.Dao
import androidx.room.Query
import com.smileidentity.sample.data.database.model.Jobs
import kotlinx.coroutines.flow.Flow

@Dao
interface JobsDao : BaseDao<Jobs> {

    @Query("SELECT * FROM jobs")
    fun fetchJobs(): Flow<List<Jobs>>
}
