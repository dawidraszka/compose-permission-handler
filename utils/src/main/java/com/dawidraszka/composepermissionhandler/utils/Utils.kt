package com.dawidraszka.composepermissionhandler.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import com.dawidraszka.composepermissionhandler.core.PermissionHandlerHostState
import com.dawidraszka.composepermissionhandler.core.PermissionHandlerResult

/**
 * Helper function to show [Snackbar] with built-in open app settings functionality. It might be
 * useful to call when [PermissionHandlerHostState.handlePermissions] returns
 * [PermissionHandlerResult.DENIED].
 *
 * @sample com.dawidraszka.composepermissionhandler.sample.SampleScreen
 *
 * @param message message to be shown in the [Snackbar]
 * @param openSettingsActionLabel label for open app settings action
 * @param context activity context, obtained for instance, by calling
 * [androidx.compose.ui.platform.LocalContext]
 * @param duration duration how long the [Snackbar] is meant to be displayed
 */
suspend fun SnackbarHostState.showAppSettingsSnackbar(
    message: String,
    openSettingsActionLabel: String,
    context: Context,
    duration: SnackbarDuration = SnackbarDuration.Short
) {
    val result = showSnackbar(
        message = message,
        actionLabel = openSettingsActionLabel,
        duration = duration
    )
    if (result == SnackbarResult.ActionPerformed) openAppSettings(context)
}

/**
 * Helper function to open app settings. It is called by [showAppSettingsSnackbar], but you might
 * want to call it yourself if you need more custom [Snackbar] or entirely different mechanism for
 * communicating permission denial to user.
 *
 * @param context activity context, obtained for instance, by calling
 * [androidx.compose.ui.platform.LocalContext]
 */
fun openAppSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    )
    context.startActivity(intent)
}
