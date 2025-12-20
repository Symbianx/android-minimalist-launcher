package com.symbianx.minimalistlauncher.data.system

import com.symbianx.minimalistlauncher.domain.model.App
import kotlinx.coroutines.flow.Flow

/**
 * Data source for retrieving installed applications from the system.
 */
interface AppListDataSource {
    /**
     * Retrieves all installed user applications (excludes system apps).
     *
     * @return Flow emitting list of installed apps
     */
    fun getInstalledApps(): Flow<List<App>>
}
