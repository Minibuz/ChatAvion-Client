package fr.chatavion.client.util

import android.content.Context
import androidx.preference.PreferenceManager

object ThemeHelper {
    private const val SELECTED_THEME = "Locale.Helper.Selected.Theme"
    private const val DARK_THEME = "dark"
    private const val LIGHT_THEME = "light"

    fun isDarkThemeEnabled(context: Context): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val theme = preferences.getString(SELECTED_THEME, LIGHT_THEME) ?: LIGHT_THEME
        return theme == DARK_THEME
    }

    fun enableDarkTheme(context: Context, darkThemeEnabled: Boolean){
        persist(context, darkThemeEnabled)
    }

    private fun persist(context: Context, darkThemeEnabled: Boolean){
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.putString(SELECTED_THEME, if (darkThemeEnabled) DARK_THEME else LIGHT_THEME)
        editor.apply()
    }

}