package fr.chatavion.client.util

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment

object Utils {

    fun showInfoToast(text: String, context: Context){
        Toast.makeText(context,
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