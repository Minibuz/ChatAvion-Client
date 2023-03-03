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

/**
 * This class represents a repository for app settings stored in Android DataStore.
 * @param context The Android context.
 */
class SettingsRepository(context: Context) {

    /**
     * The DataStore object that stores the preferences.
     */
    private val dataStore = context.dataStore

    /**
     * The key for the theme preference.
     */
    companion object {
        val THEME_KEY = stringPreferencesKey("theme")

        // Define other preference keys
        val PSEUDO_KEY = stringPreferencesKey("pseudo")
        val PROTOCOL_CHOICE_TRANSACTION_KEY = stringPreferencesKey("protocol_choice")
        val LANGUAGE_KEY = stringPreferencesKey("language")
        val REFRESH_TIME_KEY = longPreferencesKey("refresh_time")
        val HISTORY_LOADING_KEY = intPreferencesKey("history_loading")
    }

    /**
     * Enumeration of the available themes.
     */
    enum class Theme {
        Light, Dark
    }

    /**
     * Enumeration of the available protocols.
     */
    enum class Protocol {
        Http, Dns
    }

    /**
     * Enumeration of the available languages.
     */
    enum class Language {
        French, English
    }

    /**
     * Flow for retrieving the current theme of the app.
     * @return A flow of `Theme` enum.
     */
    val theme: Flow<Theme>
        get() = dataStore.data
            .catch { exception ->
                // Catch IOExceptions and emit an empty preferences object if caught.
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                // Retrieve the theme preference and return the corresponding enum value.
                val themeName = preferences[THEME_KEY] ?: Theme.Light.name
                Theme.valueOf(themeName)
            }

    /**
     * Sets the theme preference in the DataStore.
     * @param theme The theme to set.
     */
    suspend fun setTheme(theme: Theme) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name
        }
    }

    /**
     * Flow for retrieving the current pseudo of the app.
     * @return A flow of `String`.
     */
    val pseudo: Flow<String>
        get() = dataStore.data
            .catch { exception ->
                // Catch IOExceptions and emit an empty preferences object if caught.
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                // Retrieve the pseudo preference and return it.
                preferences[PSEUDO_KEY] ?: ""
            }

    /**
     * Sets the pseudo preference in the DataStore.
     * @param pseudo The pseudo to set.
     */
    suspend fun setPseudo(pseudo: String) {
        dataStore.edit { preferences ->
            preferences[PSEUDO_KEY] = pseudo
        }
    }

    /**
     * Flow for retrieving the current language of the app.
     * @return A flow of `Language`.
     */
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

    /**
     * Sets the Language type preference in the DataStore.
     *
     * @param language The Language type to set
     */
    suspend fun setLanguage(language: Language) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language.name
        }
    }

    /**
     * Flow for retrieving the current refresh time of the app.
     * @return A flow of `Long`.
     */
    val refreshTime: Flow<Long>
        get() = dataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[REFRESH_TIME_KEY] ?: 30L
        }

    /**
     * Sets the refresh time type preference in the DataStore.
     *
     * @param refreshTime The refresh time to set
     */
    suspend fun setRefreshTime(refreshTime: Long) {
        dataStore.edit { preferences ->
            preferences[REFRESH_TIME_KEY] = refreshTime
        }
    }

    /**
     * Flow for retrieving the current history loading value of the app.
     * @return A flow of `Int`.
     */
    val historyLoading: Flow<Int>
        get() = dataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[HISTORY_LOADING_KEY] ?: 10
        }

    /**
     * Sets the history loading type preference in the DataStore.
     *
     * @param historyLoading The history loading number to set
     */
    suspend fun setHistoryLoading(historyLoading: Int) {
        dataStore.edit { preferences ->
            preferences[HISTORY_LOADING_KEY] = historyLoading
        }
    }

    /**
     * Flow for retrieving the current protocol of the app.
     * @return A flow of `Protocol`.
     */
    val protocol: Flow<Protocol>
        get() = dataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            val languageName = preferences[PROTOCOL_CHOICE_TRANSACTION_KEY] ?: Protocol.Http.name
            Protocol.valueOf(languageName)
        }

    /**
     * Sets the Protocol type preference in the DataStore.
     *
     * @param protocol The Protocol type to set
     */
    suspend fun setProtocol(protocol: Protocol) {
        dataStore.edit { preferences ->
            preferences[PROTOCOL_CHOICE_TRANSACTION_KEY] = protocol.name
        }
    }
}