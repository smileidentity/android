package com.smileidentity.sample.data.repository

import com.smileidentity.sample.data.database.dao.JobsDao
import com.smileidentity.sample.data.database.model.Job
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

interface JobsRepository {
    suspend fun createJob(jobIds: List<String>)
    fun fetchJobs(): Flow<List<Job?>>
}

@Singleton
class JobsDataSource @Inject constructor(
    private val dao: JobsDao,
) : JobsRepository {

    override suspend fun createJob(jobIds: List<String>) {
        jobIds.map { jobId ->
            dao.insert(Job(jobId = jobId))
        }
    }

    override fun fetchJobs() = dao.fetchJobs()
}
