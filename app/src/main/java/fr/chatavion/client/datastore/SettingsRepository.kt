package fr.chatavion.client.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val THEME_KEY = stringPreferencesKey("theme")
        val PSEUDO_KEY = stringPreferencesKey("pseudo")
        val DNS_TYPE_TRANSACTION_KEY = stringPreferencesKey("dns_type_transaction")
        val PROTOCOL_KEY = stringPreferencesKey("protocol")
        val LANGUAGE_KEY = stringPreferencesKey("language")
        val REFRESH_TIME_KEY = longPreferencesKey("refresh_time")
        val HISTORY_LOADING_KEY = intPreferencesKey("history_loading")
        val ENCODING_KEY = stringPreferencesKey("encoding")
    }

    enum class Theme {
        Light, Dark
    }

    enum class Protocol {
        Dns, Http
    }

    enum class Language {
        French, English
    }


    val theme: Flow<Theme>
        get() = dataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            val themeName = preferences[THEME_KEY] ?: Theme.Light.name
            Theme.valueOf(themeName)
        }

    suspend fun setTheme(theme: Theme) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name
        }
    }

    val pseudo: Flow<String>
        get() = dataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[PSEUDO_KEY] ?: ""
        }

    suspend fun setPseudo(pseudo: String) {
        dataStore.edit { preferences ->
            preferences[PSEUDO_KEY] = pseudo
        }
    }

    val dnsTypeTransaction: Flow<String>
        get() = dataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[DNS_TYPE_TRANSACTION_KEY] ?: ""
        }

    suspend fun setDnsTypeTransaction(dnsTypeTransaction: String) {
        dataStore.edit { preferences ->
            preferences[DNS_TYPE_TRANSACTION_KEY] = dnsTypeTransaction
        }
    }

    val protocol: Flow<Protocol>
        get() = dataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            val protocolName = preferences[PROTOCOL_KEY] ?: Protocol.Dns.name
            Protocol.valueOf(protocolName)
        }

    suspend fun setProtocol(protocol: Protocol) {
        dataStore.edit { preferences ->
            preferences[PROTOCOL_KEY] = protocol.name
        }
    }

    val language: Flow<Language>
        get() = dataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            val languageName = preferences[LANGUAGE_KEY] ?: Language.French.name
            Language.valueOf(languageName)
        }

    suspend fun setLanguage(language: Language) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language.name
        }
    }

    val refreshTime: Flow<Long>
        get() = dataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[REFRESH_TIME_KEY] ?: 0L
        }


    suspend fun setRefreshTime(refreshTime: Long) {
        dataStore.edit { preferences ->
            preferences[REFRESH_TIME_KEY] = refreshTime
        }
    }


    val historyLoading: Flow<Int>
        get() = dataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[HISTORY_LOADING_KEY] ?: 0
        }

    suspend fun setHistoryLoading(historyLoading: Int) {
        dataStore.edit { preferences ->
            preferences[HISTORY_LOADING_KEY] = historyLoading
        }
    }

    val encoding: Flow<String>
        get() = dataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[ENCODING_KEY] ?: ""
        }

    suspend fun setEncoding(encoding: String) {
        dataStore.edit { preferences ->
            preferences[ENCODING_KEY] = encoding
        }
    }
}

