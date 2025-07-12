package com.smileidentity.sample.data.database.dao

import BaseDao
import androidx.room.Dao
import androidx.room.Query
import com.smileidentity.sample.data.database.model.Config
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfigDao : BaseDao<Config> {

    @Query("SELECT * FROM config")
    fun fetchConfigs(): Flow<List<Config>>
}
