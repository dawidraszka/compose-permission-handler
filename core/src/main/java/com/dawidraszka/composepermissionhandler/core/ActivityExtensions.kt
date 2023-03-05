package com.dawidraszka.composepermissionhandler.core

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.core.app.ActivityCompat

internal fun Activity.shouldShowRationale(permissions: List<String>) =
    permissions.any { permission ->
        ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
    }

internal fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    error("Current context isn't an Activity")
}
