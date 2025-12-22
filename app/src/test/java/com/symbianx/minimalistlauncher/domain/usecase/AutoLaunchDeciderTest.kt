package com.symbianx.minimalistlauncher.domain.usecase

import android.content.Intent
import com.symbianx.minimalistlauncher.domain.model.App
import com.symbianx.minimalistlauncher.domain.model.SearchState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AutoLaunchDeciderTest {
    @Test
    fun eligible_whenEnabled_active_nonBlank_singleResult() {
        val state =
            SearchState(
                isActive = true,
                query = "Maps",
                results =
                    listOf(
                        App(
                            packageName = "com.google.android.apps.maps",
                            label = "Maps",
                            launchIntent = Intent(Intent.ACTION_MAIN).apply { `package` = "com.google.android.apps.maps" },
                        ),
                    ),
            )
        assertTrue(AutoLaunchDecider.isEligible(enabled = true, state = state))
    }

    @Test
    fun notEligible_whenDisabled() {
        val state = SearchState(isActive = true, query = "Maps", results = listOf(dummyApp()))
        assertFalse(AutoLaunchDecider.isEligible(enabled = false, state = state))
    }

    @Test
    fun notEligible_whenInactive() {
        val state = SearchState(isActive = false, query = "Maps", results = listOf(dummyApp()))
        assertFalse(AutoLaunchDecider.isEligible(enabled = true, state = state))
    }

    @Test
    fun notEligible_whenBlankQuery() {
        val state = SearchState(isActive = true, query = "", results = listOf(dummyApp()))
        assertFalse(AutoLaunchDecider.isEligible(enabled = true, state = state))
    }

    @Test
    fun notEligible_whenMultipleResults() {
        val state = SearchState(isActive = true, query = "Ca", results = listOf(dummyApp(), dummyApp()))
        assertFalse(AutoLaunchDecider.isEligible(enabled = true, state = state))
    }

    private fun dummyApp(): App =
        App(
            packageName = "com.example.app",
            label = "Example",
            launchIntent = Intent(Intent.ACTION_MAIN).apply { `package` = "com.example.app" },
        )
}
