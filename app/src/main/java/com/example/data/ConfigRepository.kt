package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.priyStudyConfigDataStore: DataStore<Preferences> by
  preferencesDataStore(name = "priy_study_config")

interface ConfigRepository {
  val configFlow: Flow<IntegrationConfig>

  suspend fun saveConfig(config: IntegrationConfig)

  suspend fun resetConfig()
}

class DataStoreConfigRepository(private val context: Context) : ConfigRepository {

  private object Keys {
    val SIGMA_PACKAGE_NAME = stringPreferencesKey("sigma_package_name")
    val SIGMA_DEEP_LINK_URI = stringPreferencesKey("sigma_deep_link_uri")
    val OFFICIAL_WEBSITE_URL = stringPreferencesKey("official_website_url")
    val EXTERNAL_SHORTENER_URL = stringPreferencesKey("external_shortener_url")
  }

  override val configFlow: Flow<IntegrationConfig> =
    context.priyStudyConfigDataStore.data
      .catch { exception ->
        if (exception is IOException) {
          emit(emptyPreferences())
        } else {
          throw exception
        }
      }
      .map { prefs ->
        IntegrationConfig(
          sigmaPackageName =
            prefs[Keys.SIGMA_PACKAGE_NAME] ?: IntegrationConfig.DEFAULT_SIGMA_PACKAGE_NAME,
          sigmaDeepLinkUri =
            prefs[Keys.SIGMA_DEEP_LINK_URI] ?: IntegrationConfig.DEFAULT_SIGMA_DEEP_LINK_URI,
          officialWebsiteUrl =
            prefs[Keys.OFFICIAL_WEBSITE_URL] ?: IntegrationConfig.DEFAULT_OFFICIAL_WEBSITE_URL,
          externalShortenerUrl =
            prefs[Keys.EXTERNAL_SHORTENER_URL] ?: IntegrationConfig.DEFAULT_EXTERNAL_SHORTENER_URL,
        )
      }

  override suspend fun saveConfig(config: IntegrationConfig) {
    context.priyStudyConfigDataStore.edit { prefs ->
      prefs[Keys.SIGMA_PACKAGE_NAME] = config.sigmaPackageName.trim()
      prefs[Keys.SIGMA_DEEP_LINK_URI] = config.sigmaDeepLinkUri.trim()
      prefs[Keys.OFFICIAL_WEBSITE_URL] = config.officialWebsiteUrl.trim()
      prefs[Keys.EXTERNAL_SHORTENER_URL] = config.externalShortenerUrl.trim()
    }
  }

  override suspend fun resetConfig() {
    context.priyStudyConfigDataStore.edit { it.clear() }
  }
}
