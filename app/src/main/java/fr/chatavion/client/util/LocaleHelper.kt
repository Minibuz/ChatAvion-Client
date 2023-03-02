package fr.chatavion.client.util

import android.content.Context
import androidx.preference.PreferenceManager
import java.util.*

object LocaleHelper {
    private const val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"
    const val ENGLISH = "en"
    const val FRENCH = "fr"

    fun getLanguage(context: Context): String {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getString(SELECTED_LANGUAGE, "en")?: "en"
    }

    fun getContext(context: Context): Context{
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val lang = preferences.getString(SELECTED_LANGUAGE, "en")?: "en"
        return setLocale(context, lang)
    }

    fun setLocale(context: Context, language: String): Context {
        persist(context, language)
        return updateResources(context, language)
    }

    private fun persist(context: Context, language: String) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.putString(SELECTED_LANGUAGE, language)
        editor.apply()
    }

    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return context.createConfigurationContext(configuration)
    }

}