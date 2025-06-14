package com.smileidentity.sample.database.dao

import BaseDao
import androidx.room.Dao
import com.smileidentity.sample.database.model.Jobs

@Dao
interface JobsDao : BaseDao<Jobs>
