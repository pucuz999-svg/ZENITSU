package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.SystemTelemetryRepository
import com.example.data.UserPreferencesRepository
import com.example.model.FpsGraphicsPreset
import com.example.model.SensitivityValues
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Zenitsu_2.0", appName)
    }

    @Test
    fun `telemetry repository snapshots valid device metrics`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = SystemTelemetryRepository(context)
        val telemetry = repo.snapshotTelemetry()
        assertTrue(telemetry.cpuCores >= 1)
        assertTrue(telemetry.currentRefreshRateHz >= 60)
        assertTrue(telemetry.performanceScore in 18..99)
        assertTrue(telemetry.readinessChecks.isNotEmpty())
        assertTrue(telemetry.jvmHeapMaxMb > 0f)
    }

    @Test
    fun `performance booster clears cache files and completes memory optimization`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val tempFile = File(context.cacheDir, "stale_shader_buffer.tmp")
        tempFile.writeBytes(ByteArray(4096) { 1 })
        assertTrue(tempFile.exists())

        val repo = SystemTelemetryRepository(context)
        val progressEvents = mutableListOf<Float>()
        val result = repo.runRealSystemOptimization(
            forceMaxRefreshRate = true,
            adaptiveBatteryPause = true,
        ) { progress, _, _ ->
            progressEvents.add(progress)
        }

        assertFalse(tempFile.exists())
        assertTrue(result.cacheClearedKb >= 4L)
        assertTrue(result.maxRefreshRateApplied)
        assertTrue(result.completedSteps.size >= 4)
        assertTrue(progressEvents.last() == 1f)
    }

    @Test
    fun `all engine features default to active on installation and scribd profile persists`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = UserPreferencesRepository(context)

        assertTrue(prefs.forceMaxRefreshRate.first())
        assertTrue(prefs.adaptiveBatteryPause.first())
        assertTrue(prefs.floatingFpsOverlayEnabled.first())
        assertTrue(prefs.thunderSoundEnabled.first())

        prefs.setThunderSoundEnabled(false)
        assertFalse(prefs.thunderSoundEnabled.first())
        prefs.setThunderSoundEnabled(true)
        assertTrue(prefs.thunderSoundEnabled.first())

        val sens = SensitivityValues(192, 188, 174, 158, 118, 165, 45, 520)
        val fps = FpsGraphicsPreset("Smooth", "High", 90, false, "Normal", "Classic", true, "Max FPS")
        prefs.saveActiveAppliedProfile("Motorola Edge 50 Pro (Scribd)", sens, fps, isCustomized = true)

        val loaded = prefs.activeAppliedProfile.first()
        assertEquals("Motorola Edge 50 Pro (Scribd)", loaded?.sourceName)
        assertEquals(192, loaded?.sensitivity?.general)
        assertEquals(90, loaded?.fpsPreset?.targetFps)
        assertTrue(loaded?.isCustomized == true)

        prefs.clearActiveAppliedProfile()
        assertNull(prefs.activeAppliedProfile.first())
    }

    @Test
    fun `thunder boost sound raw asset exists and player cleans up safely`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val rawBytes = context.resources.openRawResource(R.raw.thunder_boost).use { it.readBytes() }
        assertTrue(rawBytes.size > 100_000)
        assertEquals("RIFF", String(rawBytes.sliceArray(0..3)))
        assertEquals("WAVE", String(rawBytes.sliceArray(8..11)))

        val player = com.example.audio.ThunderSoundPlayer(context)
        player.playThunder()
        player.release()
    }
}
