package com.symbianx.minimalistlauncher.domain.usecase

import android.content.Context
import com.symbianx.minimalistlauncher.domain.model.App

/**
 * Use case for launching an application.
 */
interface LaunchAppUseCase {
    /**
     * Launches the specified application.
     *
     * @param app The app to launch
     * @return true if launch succeeded, false otherwise
     */
    fun execute(app: App): Boolean
}
