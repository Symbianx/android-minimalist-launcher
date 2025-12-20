package com.symbianx.minimalistlauncher.domain.usecase

import com.symbianx.minimalistlauncher.domain.model.SearchState

object AutoLaunchDecider {
    fun isEligible(enabled: Boolean, state: SearchState): Boolean {
        if (!enabled) return false
        if (!state.isActive) return false
        if (state.query.isBlank()) return false
        return state.results.size == 1
    }
}
