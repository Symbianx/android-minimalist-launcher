package com.symbianx.minimalistlauncher.data.repository

import com.symbianx.minimalistlauncher.data.system.AppListDataSource
import com.symbianx.minimalistlauncher.domain.model.App
import com.symbianx.minimalistlauncher.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implementation of [AppRepository] that delegates to [AppListDataSource].
 */
class AppRepositoryImpl(
    private val appListDataSource: AppListDataSource,
) : AppRepository {
    override fun getApps(): Flow<List<App>> = appListDataSource.getInstalledApps()
}
