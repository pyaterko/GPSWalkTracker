package com.example.gpswalktracker.utils

import android.app.AlertDialog
import android.content.Context
import com.example.gpswalktracker.R


object DialogManager {
    fun showGPSEnabledDialog(
        context: Context,
        onClickPositiveButton: () -> Unit
    ) {
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        with(dialog) {
            setTitle(context.getString(R.string.gps_disabled))
            setMessage(context.getString(R.string.is_gps_enabled))
            setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.ok)) { _, _ ->
                onClickPositiveButton()
            }
            setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(R.string.no)) { _, _ ->
              dismiss()
            }
            show()
        }
    }

    fun showDialogLocationPermission(context: Context, onClickPositiveButton: () -> Unit) {
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        with(dialog) {
            setTitle(context.getString(R.string.location_ermission_eeded))
            setMessage(context.getString(R.string.this_app_needs_the_Location_permission))
            setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.ok)) { _, _ ->
                onClickPositiveButton()
            }
            show()
        }
    }
}
