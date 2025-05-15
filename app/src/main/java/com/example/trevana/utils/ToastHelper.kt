package com.example.trevana.utils

import android.content.Context
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

object ToastHelper {
    fun showSuccessToast(context: Context, message: String) {
        createColoredToast(context, message, R.color.success_green)
    }

    fun showErrorToast(context: Context, message: String) {
        createColoredToast(context, message, R.color.error_red)
    }

    fun showWarningToast(context: Context, message: String) {
        createColoredToast(context, message, R.color.warning_yellow)
    }

    private fun createColoredToast(context: Context, message: String, @ColorRes colorRes: Int) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).apply {
            view?.setBackgroundColor(ContextCompat.getColor(context, colorRes))
            show()
        }
    }
}