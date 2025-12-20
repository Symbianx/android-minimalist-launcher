package com.symbianx.minimalistlauncher.domain.usecase

import android.content.Intent
import com.symbianx.minimalistlauncher.domain.model.App
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SearchAppsUseCaseTest {

    private lateinit var useCase: SearchAppsUseCase
    private lateinit var testApps: List<App>

    @Before
    fun setup() {
        useCase = SearchAppsUseCaseImpl()
        
        // Create test apps
        testApps = listOf(
            App("com.android.chrome", "Chrome", Intent(), false),
            App("com.android.calculator", "Calculator", Intent(), false),
            App("com.google.android.calendar", "Calendar", Intent(), false),
            App("com.android.camera", "Camera", Intent(), false),
            App("com.spotify.music", "Spotify", Intent(), false),
            App("com.whatsapp", "WhatsApp", Intent(), false),
        )
    }

    @Test
    fun `execute with empty query returns empty list`() {
        val result = useCase.execute(testApps, "")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `execute with blank query returns empty list`() {
        val result = useCase.execute(testApps, "   ")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `execute with single character query returns matches`() {
        val result = useCase.execute(testApps, "c")
        assertEquals(4, result.size) // Chrome, Calculator, Calendar, Camera
        assertTrue(result.all { it.label.contains("c", ignoreCase = true) })
    }

    @Test
    fun `execute with specific query returns exact match`() {
        val result = useCase.execute(testApps, "Chrome")
        assertEquals(1, result.size)
        assertEquals("Chrome", result[0].label)
    }

    @Test
    fun `execute is case insensitive`() {
        val resultLower = useCase.execute(testApps, "chrome")
        val resultUpper = useCase.execute(testApps, "CHROME")
        val resultMixed = useCase.execute(testApps, "ChRoMe")
        
        assertEquals(resultLower.size, resultUpper.size)
        assertEquals(resultLower.size, resultMixed.size)
        assertEquals(1, resultLower.size)
    }

    @Test
    fun `execute with partial match returns results`() {
        val result = useCase.execute(testApps, "Cal")
        assertEquals(2, result.size) // Calculator, Calendar
    }

    @Test
    fun `execute returns results sorted alphabetically`() {
        val result = useCase.execute(testApps, "ca")
        assertEquals(3, result.size)
        assertEquals("Calculator", result[0].label)
        assertEquals("Calendar", result[1].label)
        assertEquals("Camera", result[2].label)
    }

    @Test
    fun `execute with no matches returns empty list`() {
        val result = useCase.execute(testApps, "xyz123")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `execute does not limit results`() {
        // Create 100 apps
        val manyApps = (1..100).map { 
            App("com.test.app$it", "TestApp$it", Intent(), false)
        }
        
        val result = useCase.execute(manyApps, "TestApp")
        assertEquals(100, result.size) // No limit in current implementation
    }

    @Test
    fun `execute with empty app list returns empty list`() {
        val result = useCase.execute(emptyList(), "chrome")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `execute does not automatically filter system apps`() {
        val appsWithSystem = testApps + App("com.android.system", "System", Intent(), true)
        val result = useCase.execute(appsWithSystem, "s")
        
        // Current implementation does not filter system apps - that's done at repository level
        assertTrue(result.any { it.label == "Spotify" })
        assertTrue(result.any { it.label == "System" })
    }
}
