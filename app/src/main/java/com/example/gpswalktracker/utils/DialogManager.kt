package com.example.gpswalktracker.utils

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.LayoutInflater
import com.example.gpswalktracker.R
import com.example.gpswalktracker.data.InfoTrackItem
import com.example.gpswalktracker.databinding.DialogForSavingTheTreackBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder


object DialogManager {

    fun showSettingDialog(
        context: Context,
        onClickPositiveButton: () -> Unit,
    ) {
        MaterialAlertDialogBuilder(
            context,
            com.google.android.material.R.style.MaterialAlertDialog_Material3
        )
            .setTitle(context.getString(R.string.notification_permission))
            .setMessage(context.getString(R.string.notification_permission_is_required))
            .setPositiveButton(context.getString(R.string.ok)) { _, _ ->
                onClickPositiveButton()
            }
            .setNegativeButton(context.getString(R.string.no), null)
            .show()
    }

    fun showNotificationPermissionRationale(
        context: Context,
        onClickPositiveButton: () -> Unit,
    ) {

        MaterialAlertDialogBuilder(
            context,
            com.google.android.material.R.style.MaterialAlertDialog_Material3
        )
            .setTitle(context.getString(R.string.alert))
            .setMessage(context.getString(R.string.to_show_notification))
            .setPositiveButton(context.getString(R.string.ok)) { _, _ ->
                if (Build.VERSION.SDK_INT >= 33) {
                    onClickPositiveButton()
                }
            }
            .setNegativeButton(context.getString(R.string.no), null)
            .show()
    }

    fun showGPSEnabledDialog(
        context: Context,
        onClickPositiveButton: () -> Unit,
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


    fun showDialogForSavingTheTrack(
        context: Context,
        infoTrackItem: InfoTrackItem?,
        onClickPositiveButton: () -> Unit,
    ) {
        val builder = AlertDialog.Builder(context)
        val binding =
            DialogForSavingTheTreackBinding.inflate(
                LayoutInflater.from(context),
                null,
                false
            )

        builder
            .setView(binding.root)
        val dialog = builder.create()
        binding.apply {
            setDataForDialogForSavingTheTrack(
                infoTrackItem,
                onClickPositiveButton,
                dialog
            )
        }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }
    @SuppressLint("SetTextI18n")
    private fun DialogForSavingTheTreackBinding.setDataForDialogForSavingTheTrack(
        infoTrackItem: InfoTrackItem?,
        onClickPositiveButton: () -> Unit,
        dialog: AlertDialog,
    ) {
        tvDictance.text = infoTrackItem?.distance
        tvSpeed.text =infoTrackItem?.speed
        tvTime.text =infoTrackItem?.time
        buttonYes.setOnClickListener {
            onClickPositiveButton()
            dialog.dismiss()
        }
        buttonNo.setOnClickListener {
            dialog.dismiss()
        }
    }
}

