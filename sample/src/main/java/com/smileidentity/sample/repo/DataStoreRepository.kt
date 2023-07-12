package com.smileidentity.sample.repo

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.smileidentity.SmileID
import com.smileidentity.models.Config
import com.smileidentity.sample.SmileIDApplication
import com.smileidentity.sample.model.Job
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

private typealias PartnerId = String
private typealias IsProduction = Boolean
private typealias JobsDataStoreMapKey = Pair<PartnerId, IsProduction>

/**
 * Singleton wrapper to allow for typed usage of [androidx.datastore.core.DataStore].
 */
object DataStoreRepository {
    /**
     * The main [androidx.datastore.core.DataStore] instance for the app, aptly named "main".
     */
    private val mainDataStore = PreferenceDataStoreFactory.create {
        SmileIDApplication.appContext.preferencesDataStoreFile(name = "main")
    }

    /**
     * Each partner+environment combination gets its own [DataStore] instance, so that we can
     * update the status of a single job without having to read and write the entire set of jobs.
     */
    private val jobsDataStores = with(mutableMapOf<JobsDataStoreMapKey, DataStore<Preferences>>()) {
        withDefault {
            getOrPut(it) {
                Timber.v("Initializing DataStore for partnerId=${it.first} production=${it.second}")
                PreferenceDataStoreFactory.create {
                    val name = "jobs_${it.first}_${it.second}"
                    SmileIDApplication.appContext.preferencesDataStoreFile(name = name)
                }
            }
        }
    }

    private val jobsAdapter = SmileID.moshi.adapter(Job::class.java)

    /**
     * To provide atomicity guarantees for situations where an update first requires an independent
     * read of the data, modifying it, and then writing it back. (i.e. inserting a single value into
     * an existing set). We use [Mutex] because the alternative, [synchronized], blocks the *entire*
     * thread, and multiple coroutines could be running on the same thread.
     *
     * Theoretically, for best performance, we should use a mutex per [DataStore] instance, but
     * concurrent transactions across different [DataStore] instances are extremely unlikely
     */
    private val mutex = Mutex()

    /**
     * Caution! Deletes all preferences
     */
    internal suspend fun clear() {
        mainDataStore.edit { it.clear() }
    }

    /**
     * Get the Smile [Config] from [mainDataStore], if one has been manually set. (It may not be set if
     * the sample app is utilizing the assets/smile_config.json file instead).
     *
     * If no config is set, this returns null
     */
    fun getConfig(): Flow<Config?> = mainDataStore.data.map {
        it[Keys.config]?.let { SmileID.moshi.adapter(Config::class.java).fromJson(it) }
    }

    /**
     * Save the Smile [Config] which was set at runtime
     */
    suspend fun setConfig(config: Config) {
        mainDataStore.edit {
            it[Keys.config] = SmileID.moshi.adapter(Config::class.java).toJson(config)
        }
    }

    /**
     * Clear the Smile [Config] which was set at runtime (thereby falling back to reading from
     * assets)
     */
    suspend fun clearConfig() {
        mainDataStore.edit { it.remove(Keys.config) }
    }

    fun getJobs(partnerId: String, isProduction: Boolean) =
        jobDataStore(partnerId, isProduction).data.map {
            val completed = it[Keys.jobs] ?: emptySet()
            completed.mapNotNull {
                jobsAdapter.fromJson(it) ?: run {
                    Timber.e("Failed to parse completed job: $it")
                    null
                }
            }.sortedBy(Job::timestamp).toImmutableList()
        }

    suspend fun addJob(partnerId: String, isProduction: Boolean, job: Job) {
        val dataStore = jobDataStore(partnerId, isProduction)
        mutex.withLock {
            // The !! is safe since the Map provides implicit default values
            val completed = dataStore.data.first()[Keys.jobs] ?: emptySet()
            dataStore.edit { it[Keys.jobs] = completed + jobsAdapter.toJson(job) }
        }
    }

    suspend fun clearJobs(partnerId: String, isProduction: Boolean) {
        val dataStore = jobDataStore(partnerId, isProduction)
        mutex.withLock {
            dataStore.edit { it.remove(Keys.jobs) }
        }
    }

    /**
     * The [DataStore] instance for the given partner+environment combination
     */
    private fun jobDataStore(partnerId: String, isProduction: Boolean): DataStore<Preferences> {
        return jobsDataStores.getValue(partnerId to isProduction)
    }

    /**
     * The set of keys to be used with [mainDataStore].
     */
    private object Keys {
        val config = stringPreferencesKey("config")
        val jobs = stringSetPreferencesKey("jobs")
    }
}
