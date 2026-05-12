package com.symbianx.minimalistlauncher.domain.usecase

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.symbianx.minimalistlauncher.domain.model.App
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LaunchAppUseCaseImplTest {
    private lateinit var context: Context
    private lateinit var packageManager: PackageManager
    private lateinit var useCase: LaunchAppUseCaseImpl

    @Before
    fun setup() {
        packageManager = mock()
        context = mock()
        whenever(context.packageManager).thenReturn(packageManager)
        useCase = LaunchAppUseCaseImpl(context)
    }

    @Test
    fun `execute re-resolves launch intent from PackageManager`() {
        // PackageManager returns a fresh intent for this package
        val freshIntent =
            Intent(Intent.ACTION_MAIN).apply {
                setClassName("com.example.app", "com.example.app.NewActivity")
            }
        whenever(packageManager.getLaunchIntentForPackage("com.example.app"))
            .thenReturn(freshIntent)

        // Create app with a stale/different cached intent
        val staleIntent =
            Intent(Intent.ACTION_MAIN).apply {
                setClassName("com.example.app", "com.example.app.OldActivity")
            }
        val app =
            App(
                packageName = "com.example.app",
                label = "Example",
                launchIntent = staleIntent,
            )

        val result = useCase.execute(app)
        assertTrue("Launch should succeed when PackageManager can resolve the intent", result)
        // Verify startActivity was called with the fresh intent (not the stale cached one)
        val captor = org.mockito.kotlin.argumentCaptor<Intent>()
        verify(context).startActivity(captor.capture())
        assertEquals(
            "com.example.app.NewActivity",
            captor.firstValue.component?.className,
        )
    }

    @Test
    fun `execute returns false when app has no resolvable intent`() {
        // PackageManager returns null - app is uninstalled or has no launcher activity
        whenever(packageManager.getLaunchIntentForPackage("com.uninstalled.app"))
            .thenReturn(null)

        val app =
            App(
                packageName = "com.uninstalled.app",
                label = "Uninstalled",
                launchIntent = Intent(),
            )

        val result = useCase.execute(app)
        assertFalse("Launch should fail when no launch intent can be resolved", result)
    }

    @Test
    fun `execute uses fresh intent even when cached intent differs - Duolingo scenario`() {
        // This simulates the Duolingo scenario where the app changes its launcher activity
        val updatedIntent =
            Intent(Intent.ACTION_MAIN).apply {
                setClassName("com.duolingo", "com.duolingo.SeasonalActivity")
            }
        whenever(packageManager.getLaunchIntentForPackage("com.duolingo"))
            .thenReturn(updatedIntent)

        val originalIntent =
            Intent(Intent.ACTION_MAIN).apply {
                setClassName("com.duolingo", "com.duolingo.OriginalActivity")
            }
        val app =
            App(
                packageName = "com.duolingo",
                label = "Duolingo",
                launchIntent = originalIntent, // stale cached intent
            )

        val result = useCase.execute(app)
        assertTrue("Launch should succeed using the fresh intent from PackageManager", result)
        // Verify startActivity was called with the updated intent (not the stale cached one)
        val captor = org.mockito.kotlin.argumentCaptor<Intent>()
        verify(context).startActivity(captor.capture())
        assertEquals(
            "com.duolingo.SeasonalActivity",
            captor.firstValue.component?.className,
        )
    }

    @Test
    fun `execute returns false when startActivity throws exception`() {
        val intent = Intent(Intent.ACTION_MAIN)
        whenever(packageManager.getLaunchIntentForPackage("com.crashing.app"))
            .thenReturn(intent)
        whenever(context.startActivity(any()))
            .thenThrow(RuntimeException("Activity not found"))

        val app =
            App(
                packageName = "com.crashing.app",
                label = "Crashing",
                launchIntent = Intent(),
            )

        val result = useCase.execute(app)
        assertFalse("Launch should return false when startActivity throws", result)
    }
}
