package com.smileidentity.sample.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.smileidentity.models.JobType

@Entity(indices = [Index(value = arrayOf("id"), unique = true)])
data class Jobs(
    @PrimaryKey
    val id: String,
    val jobType: JobType,
    val timestamp: String,
    val userId: String,
    val jobId: String,
    val jobComplete: Boolean = false,
    val jobSuccess: Boolean = false,
    val code: String? = null,
    val resultCode: String? = null,
    val smileJobId: String? = null,
    val resultText: String? = null,
    val selfieImageUrl: String? = null,
    val livenessImagesUrl: List<String>? = emptyList(),
    val documentFrontImageUrl: String? = null,
    val documentBackImageUrl: String? = null,
)
