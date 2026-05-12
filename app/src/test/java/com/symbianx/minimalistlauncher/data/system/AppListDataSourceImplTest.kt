package com.symbianx.minimalistlauncher.data.system

import android.app.Application
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class AppListDataSourceImplTest {
    @Test
    fun `getInstalledApps registers receiver for package changed and replaced broadcasts`() {
        val context = RuntimeEnvironment.getApplication()
        val dataSource = AppListDataSourceImpl(context)

        val shadowApp = shadowOf(context as Application)

        // Launch the flow collection in a separate scope so it stays active
        // (the callbackFlow keeps the receiver registered while collecting)
        val scope = CoroutineScope(Dispatchers.Unconfined)
        scope.launch {
            dataSource.getInstalledApps().collect { /* keep collecting */ }
        }

        val registeredReceivers = shadowApp.registeredReceivers

        val matchingFilters = registeredReceivers.mapNotNull { wrapper ->
            try {
                wrapper.intentFilter
            } catch (e: Exception) {
                null
            }
        }

        fun hasAction(action: String) = matchingFilters.any { filter ->
            (0 until filter.countActions()).any { i -> filter.getAction(i) == action }
        }

        assertTrue(
            "Receiver should listen for ACTION_PACKAGE_CHANGED",
            hasAction(Intent.ACTION_PACKAGE_CHANGED),
        )
        assertTrue(
            "Receiver should listen for ACTION_PACKAGE_REPLACED",
            hasAction(Intent.ACTION_PACKAGE_REPLACED),
        )
        assertTrue(
            "Receiver should listen for ACTION_PACKAGE_ADDED",
            hasAction(Intent.ACTION_PACKAGE_ADDED),
        )
        assertTrue(
            "Receiver should listen for ACTION_PACKAGE_REMOVED",
            hasAction(Intent.ACTION_PACKAGE_REMOVED),
        )

        scope.cancel()
    }
}
