package com.smileidentity.sample.data.repository

import com.smileidentity.sample.data.database.dao.ConfigDao
import com.smileidentity.sample.data.database.model.ConfigModel
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

interface ConfigRepository {
    suspend fun createConfig(configModel: ConfigModel)
    fun fetchConfigs(): Flow<List<ConfigModel?>>
}

@Singleton
class ConfigDataSource @Inject constructor(
    private val dao: ConfigDao,
) : ConfigRepository {

    override suspend fun createConfig(configModel: ConfigModel) = dao.insert(item = configModel)

    override fun fetchConfigs() = dao.fetchConfigs()
}
