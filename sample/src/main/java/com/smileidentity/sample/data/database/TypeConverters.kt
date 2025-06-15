package com.smileidentity.sample.data.database

import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.smileidentity.models.JobType

/**
 * Room type converter for JobType enum.
 * Converts between JobType and Int for database storage.
 */
@TypeConverters
class JobTypeConverter {

    /**
     * Converts JobType to Int for database storage
     */
    @TypeConverter
    fun fromJobType(jobType: JobType): Int = jobType.value

    /**
     * Converts Int from database to JobType
     */
    @TypeConverter
    fun toJobType(value: Int): JobType = JobType.fromValue(value)
}

/**
 * Room type converter for livenessImagesUrls
 * Converts between list<string> and string for database storage.
 */
@TypeConverters
class StringListTypeConverter {

    private val delimiter = "|||"

    /**
     * Converts List<String>? to String for database storage
     */
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.joinToString(delimiter)
    }

    /**
     * Converts String from database to List<String>?
     */
    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return if (value.isNullOrEmpty()) {
            null
        } else {
            value.split(delimiter)
        }
    }
}
