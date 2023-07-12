package com.smileidentity.sample.repo

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.smileidentity.SmileID
import com.smileidentity.models.Config
import com.smileidentity.sample.SmileIDApplication
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Singleton wrapper to allow for typed usage of [androidx.datastore.core.DataStore].
 */
object DataStoreRepository {
    /**
     * The main [androidx.datastore.core.DataStore] instance for the app, aptly named "main".
     */
    private val dataStore = PreferenceDataStoreFactory.create {
        SmileIDApplication.appContext.preferencesDataStoreFile("main")
    }

    /**
     * Caution! Deletes all preferences
     */
    internal suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    /**
     * Get the Smile [Config] from [dataStore], if one has been manually set. (It may not be set if
     * the sample app is utilizing the assets/smile_config.json file instead).
     *
     * If no config is set, this returns null
     */
    fun getConfig(): Flow<Config?> = dataStore.data.map {
        it[Keys.config]?.let { SmileID.moshi.adapter(Config::class.java).fromJson(it) }
    }

    /**
     * Save the Smile [Config] which was set at runtime
     */
    suspend fun setConfig(config: Config) {
        dataStore.edit {
            it[Keys.config] = SmileID.moshi.adapter(Config::class.java).toJson(config)
        }
    }

    /**
     * Clear the Smile [Config] which was set at runtime (thereby falling back to reading from
     * assets)
     */
    suspend fun clearConfig() {
        dataStore.edit {
            it.remove(Keys.config)
        }
    }

    /**
     * The set of keys to be used with [dataStore].
     */
    private object Keys {
        val config = stringPreferencesKey("config")
    }
}
