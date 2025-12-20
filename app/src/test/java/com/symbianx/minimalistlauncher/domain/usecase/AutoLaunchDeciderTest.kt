package com.symbianx.minimalistlauncher.domain.usecase

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoLaunchDeciderTest {
    @Test
    fun eligible_whenEnabled_active_nonBlank_singleResult() {
        assertTrue(AutoLaunchDecider.isEligible(enabled = true, isActive = true, query = "Maps", resultsCount = 1))
    }

    @Test
    fun notEligible_whenDisabled() {
        assertFalse(AutoLaunchDecider.isEligible(enabled = false, isActive = true, query = "Maps", resultsCount = 1))
    }

    @Test
    fun notEligible_whenInactive() {
        assertFalse(AutoLaunchDecider.isEligible(enabled = true, isActive = false, query = "Maps", resultsCount = 1))
    }

    @Test
    fun notEligible_whenBlankQuery() {
        assertFalse(AutoLaunchDecider.isEligible(enabled = true, isActive = true, query = "", resultsCount = 1))
    }

    @Test
    fun notEligible_whenMultipleResults() {
        assertFalse(AutoLaunchDecider.isEligible(enabled = true, isActive = true, query = "Ca", resultsCount = 2))
    }
}
