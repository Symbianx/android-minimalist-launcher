package com.symbianx.minimalistlauncher.ui.home

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.symbianx.minimalistlauncher.domain.model.App
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HomeViewModelTest {
    @Test
    fun createAppInfoIntent_buildsSettingsIntent_withPackageUri() {
        val app =
            App(
                packageName = "com.example.app",
                label = "Example",
                launchIntent = Intent(Intent.ACTION_MAIN).apply { `package` = "com.example.app" },
            )

        val intent = HomeViewModel.createAppInfoIntent(app)
        assertEquals(
            "Expected ACTION_APPLICATION_DETAILS_SETTINGS",
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            intent.action,
        )
        val expectedUri = Uri.fromParts("package", app.packageName, null)
        assertEquals("Expected intent data to be package Uri", expectedUri, intent.data)
    }
}
