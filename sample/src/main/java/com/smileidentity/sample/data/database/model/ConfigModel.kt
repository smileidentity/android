package com.smileidentity.sample.data.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    indices = [
        Index(value = arrayOf("partnerId"), unique = true),
    ],
)
data class ConfigModel(
    @PrimaryKey
    val partnerId: String,
    val prodAuthToken: String?,
    val testAuthToken: String?,
    val prodLambdaUrl: String,
    val testLambdaUrl: String,
)
