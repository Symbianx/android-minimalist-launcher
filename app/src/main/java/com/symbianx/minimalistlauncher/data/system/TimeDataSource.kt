package com.symbianx.minimalistlauncher.data.system

import kotlinx.coroutines.flow.Flow

/**
 * Data source for time tick events from the system.
 */
interface TimeDataSource {
    /**
     * Observes system time tick events.
     * Emits on every minute change, manual time change, and timezone change.
     *
     * @return Flow emitting Unit on each time tick
     */
    fun observeTimeTicks(): Flow<Unit>
}
