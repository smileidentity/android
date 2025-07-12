package com.smileidentity.sample.data.database.dao

import BaseDao
import androidx.room.Dao
import androidx.room.Query
import com.smileidentity.sample.data.database.model.ConfigModel
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfigDao : BaseDao<ConfigModel> {

    @Query("SELECT * FROM configmodel")
    fun fetchConfigs(): Flow<List<ConfigModel>>
}
