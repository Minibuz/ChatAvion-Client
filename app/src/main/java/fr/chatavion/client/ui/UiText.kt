package fr.chatavion.client.ui

import android.content.Context
import androidx.annotation.StringRes
import fr.chatavion.client.util.LocaleHelper

/**
 * Sealed class for handling different types of UI text.
 */
sealed class UiText {
    /**
     * Represents a string value that can be modified dynamically.
     *
     * @param value The dynamic string value.
     */
    data class DynamicString(val value: String) : UiText()

    /**
     * Represents a string resource with its id and optional arguments.
     *
     * @param resId The string resource id.
     * @param args Optional arguments for the string resource.
     */
    class StringResource(
        @StringRes val resId: Int,
        vararg val args: Any
    ) : UiText()

    /**
     * Returns a string representation of this UiText object.
     * If this UiText is a DynamicString, returns its value.
     * If this UiText is a StringResource, returns a string resource from the given context
     * with the specified resource ID and format arguments.
     * @param context the context to retrieve the string resource from
     * @return a string representation of this UiText object
     */
    fun asString(context: Context): String {
        val c = LocaleHelper.getContext(context)
        return when (this) {
            is DynamicString -> value
            is StringResource -> c.getString(resId, *args)
        }
    }
}
