package com.k.image.cropview.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

/** Walks the [ContextWrapper] chain to find the host [Activity], or null. */
fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

/**
 * Locks the host Activity to [orientation] while this composable is in the composition, restoring
 * the previous setting on dispose. Pass a value from [android.content.pm.ActivityInfo], e.g.
 * `ActivityInfo.SCREEN_ORIENTATION_PORTRAIT`.
 */
@Composable
fun LockScreenOrientation(orientation: Int) {
    val context = LocalContext.current
    DisposableEffect(orientation) {
        val activity = context.findActivity() ?: return@DisposableEffect onDispose {}
        val original = activity.requestedOrientation
        activity.requestedOrientation = orientation
        onDispose { activity.requestedOrientation = original }
    }
}