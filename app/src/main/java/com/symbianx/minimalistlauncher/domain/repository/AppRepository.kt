package com.symbianx.minimalistlauncher.domain.repository

import com.symbianx.minimalistlauncher.domain.model.App
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing installed applications.
 */
interface AppRepository {
    /**
     * Retrieves all installed user applications.
     *
     * @return Flow emitting list of installed apps
     */
    fun getApps(): Flow<List<App>>
}
