package com.symbianx.minimalistlauncher.data.system

import kotlinx.coroutines.flow.Flow

/**
 * Data source for battery information.
 */
interface BatteryDataSource {
    /**
     * Observes battery percentage changes.
     *
     * @return Flow emitting battery percentage (0-100) and charging state
     */
    fun observeBatteryStatus(): Flow<Pair<Int, Boolean>>
}
