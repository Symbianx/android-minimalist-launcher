package com.symbianx.minimalistlauncher.domain.usecase

import android.content.Context
import com.symbianx.minimalistlauncher.domain.model.App

/**
 * Implementation of [LaunchAppUseCase] using Android Context.
 */
class LaunchAppUseCaseImpl(
    private val context: Context
) : LaunchAppUseCase {

    override fun execute(app: App): Boolean {
        return try {
            context.startActivity(app.launchIntent.apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            true
        } catch (e: Exception) {
            false
        }
    }
}
