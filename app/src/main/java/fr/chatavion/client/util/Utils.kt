package fr.chatavion.client.util

import android.content.Context
import android.widget.Toast

object Utils {

    fun showInfoToast(text: String, context: Context) {
        Toast.makeText(
            context,
            text,
            Toast.LENGTH_SHORT
        ).show()
    }

    fun showErrorToast(text: String, context: Context){
        Toast.makeText(context,
            "ERROR: "+text,
            Toast.LENGTH_LONG
        ).show()
    }
}