package com.symbianx.minimalistlauncher.domain.usecase

import android.content.Context
import android.content.Intent
import android.util.Log
import com.symbianx.minimalistlauncher.domain.model.App

/**
 * Implementation of [LaunchAppUseCase] using Android Context.
 *
 * Re-resolves the launch intent at launch time to handle apps that dynamically
 * change their launcher activity (e.g., Duolingo adding seasonal emoji to labels
 * and swapping activity-aliases).
 */
class LaunchAppUseCaseImpl(
    private val context: Context,
) : LaunchAppUseCase {
    override fun execute(app: App): Boolean =
        try {
            // Re-resolve the launch intent fresh from PackageManager to handle apps
            // that change their launcher activity component at runtime.
            val resolvedIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)

            if (resolvedIntent == null) {
                Log.w("LaunchAppUseCase", "No launch intent found for ${app.packageName}, app may have been uninstalled")
                return false
            }

            resolvedIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(resolvedIntent)
            true
        } catch (e: Exception) {
            Log.e("LaunchAppUseCase", "Failed to launch ${app.packageName}: ${e.message}")
            false
        }
}
