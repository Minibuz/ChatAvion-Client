package fr.chatavion.client.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed class UiText {
    data class DynamicString(val value: String): UiText()
    class StringResource(
        @StringRes val resId: Int,
        vararg val args: Any
    ): UiText()

    /**
     * To get res strings inside composable
     */
    @Composable
    fun asString(): String {
        return  when(this) {
            is DynamicString -> value
            is StringResource -> stringResource(resId, *args)
        }
    }

    /**
     * To get res string outside composable
     */
    fun asString(context: Context): String {
        return when(this) {
            is DynamicString -> value
            is StringResource -> context.getString(resId, *args)
        }
    }
}
