package fr.chatavion.client.util

import android.content.Context
import android.widget.Toast

/**
 * A utility class providing methods for displaying Toast messages.
 */
object Utils {

    /**
     * Shows a short duration Toast message with the given text.
     * @param text the text to display in the Toast
     * @param context the Context in which to display the Toast
     */
    fun showInfoToast(text: String, context: Context) {
        Toast.makeText(
            context,
            text,
            Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * Shows a long duration Toast message with the given text preceded by "ERROR: ".
     * @param text the text to display in the Toast
     * @param context the Context in which to display the Toast
     */
    fun showErrorToast(text: String, context: Context) {
        Toast.makeText(
            context,
            "ERROR: $text",
            Toast.LENGTH_LONG
        ).show()
    }
}