package com.smileidentity.sample.data.repository

import com.smileidentity.sample.data.database.dao.ConfigDao
import com.smileidentity.sample.data.database.model.Config
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

interface ConfigRepository {
    suspend fun createConfig(config: Config)
    fun fetchConfigs(): Flow<List<Config?>>
}

@Singleton
class ConfigDataSource @Inject constructor(
    private val dao: ConfigDao,
) : ConfigRepository {

    override suspend fun createConfig(config: Config) = dao.insert(item = config)

    override fun fetchConfigs() = dao.fetchConfigs()
}
