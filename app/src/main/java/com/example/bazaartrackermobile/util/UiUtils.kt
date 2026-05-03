package com.example.bazaartrackermobile.util

import android.content.Context
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

object UiUtils {

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun showSnackbar(view: View, message: String, actionText: String? = null, action: (() -> Unit)? = null) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        if (actionText != null && action != null) {
            snackbar.setAction(actionText) { action() }
        }
        snackbar.show()
    }
}

fun View.snackbar(message: String, actionText: String? = null, action: (() -> Unit)? = null) {
    UiUtils.showSnackbar(this, message, actionText, action)
}
