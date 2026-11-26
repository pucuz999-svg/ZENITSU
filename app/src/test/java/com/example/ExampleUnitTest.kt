package com.example

import com.example.data.DeviceCatalogRepository
import com.example.model.DpiCalculationResult
import com.example.model.NetworkTelemetry
import com.example.model.PlatformBrand
import com.example.model.PlayStyleMode
import com.example.model.SensitivityValues
import com.example.model.SystemTelemetry
import com.example.model.ThermalLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun deviceCatalog_coversAllMajorPlatforms() {
        val repo = DeviceCatalogRepository()
        val devices = repo.getAllCuratedDevices()
        assertTrue(devices.size >= 45)
        assertTrue(devices.any { it.brand == PlatformBrand.SAMSUNG })
        assertTrue(devices.any { it.brand == PlatformBrand.XIAOMI_POCO })
        assertTrue(devices.count { it.brand == PlatformBrand.MOTOROLA } >= 12)
        assertTrue(devices.any { it.brand == PlatformBrand.IPHONE_IPAD })
        assertTrue(devices.any { it.brand == PlatformBrand.ASUS_REDMAGIC })
        assertTrue(devices.any { it.brand == PlatformBrand.EMULATOR_PC })
    }

    @Test
    fun detectedDeviceProfile_producesValidSensitivityAndFps() {
        val repo = DeviceCatalogRepository()
        val mockTelemetry = SystemTelemetry(
            manufacturer = "Motorola",
            model = "Edge 50 Pro",
            socLabel = "Snapdragon 7 Gen 3",
            androidVersion = "15",
            cpuCores = 8,
            totalRamMb = 12288,
            availableRamMb = 6200,
            lowMemory = false,
            batteryPercent = 82,
            batteryTempC = 33.4f,
            isCharging = false,
            isPowerSaveMode = false,
            thermalLevel = ThermalLevel.COOL,
            currentRefreshRateHz = 144,
            maxSupportedRefreshRateHz = 144,
            screenWidthPx = 1220,
            screenHeightPx = 2712,
            densityDpi = 446,
            smallestWidthDp = 411,
            freeStorageGb = 64f,
            totalStorageGb = 128f,
            network = NetworkTelemetry("Wi-Fi", isConnected = true, isMetered = false),
            installedGames = emptyList(),
            readinessChecks = emptyList(),
        )

        val profile = repo.buildDetectedDeviceProfile(mockTelemetry)
        assertTrue(profile.isDetectedCurrentDevice)
        assertTrue(profile.baseSensitivity.general in 100..200)
        assertTrue(profile.baseSensitivity.redDot in 100..200)
        assertEquals(90, profile.fpsPreset.targetFps)

        val smoothSuggestion = repo.suggestBestSmoothGameplay(
            telemetry = mockTelemetry,
            priority = com.example.model.SmoothPlayPriority.ZERO_LAG_SQUAD,
            targetDevice = profile,
        )
        assertEquals("Smooth", smoothSuggestion.recommendedFps.graphicsQuality)
        assertTrue(smoothSuggestion.hardwareNote.isNotBlank())
        assertTrue(smoothSuggestion.similarDevices.isNotEmpty())
        assertTrue(smoothSuggestion.recommendedSensitivity.general in 80..200)
    }

    @Test
    fun sensitivityTuning_scalesByPlaystyleAndDpi() {
        val base = SensitivityValues(185, 180, 165, 150, 112, 158, 48, 500)
        val oneTap = base.tunedFor(PlayStyleMode.ONE_TAP_DRAG)
        val sniper = base.tunedFor(PlayStyleMode.SNIPER_PRECISION)

        assertTrue(oneTap.general > sniper.general)
        assertTrue(oneTap.redDot > base.redDot)
        assertTrue(sniper.sniperScope < base.sniperScope)

        val lowDpi = DpiCalculationResult.calculate(currentDpi = 411, targetDpi = 400, displayHz = 90, thumbSpeedLevel = 2)
        val highDpi = DpiCalculationResult.calculate(currentDpi = 411, targetDpi = 640, displayHz = 90, thumbSpeedLevel = 2)
        assertTrue(lowDpi.sensitivity.general > highDpi.sensitivity.general)
    }

    @Test
    fun samsungCatalog_coversAllSeriesWithPreferencesAndDetection() {
        val repo = DeviceCatalogRepository()
        val samsungDevices = repo.getAllCuratedDevices().filter { it.brand == PlatformBrand.SAMSUNG }
        assertTrue(samsungDevices.size >= 24)
        assertTrue(samsungDevices.any { it.id == "samsung_s25_ultra" })
        assertTrue(samsungDevices.any { it.id == "samsung_z_fold_6" })
        assertTrue(samsungDevices.any { it.id == "samsung_a54" })
        assertTrue(samsungDevices.any { it.id == "samsung_m35_m34" })
        assertTrue(samsungDevices.any { it.id == "samsung_tab_s9_fe" })
        assertTrue(
            samsungDevices.all {
                it.preferredTouchSetting.isNotBlank() &&
                    it.dragTechnique.isNotBlank() &&
                    it.baseSensitivity.general in 120..200 &&
                    it.baseSensitivity.redDot in 120..200
            },
        )

        val a54Telemetry = SystemTelemetry(
            manufacturer = "Samsung",
            model = "SM-A546B",
            socLabel = "Exynos 1380",
            androidVersion = "14",
            cpuCores = 8,
            totalRamMb = 8192,
            availableRamMb = 4200,
            lowMemory = false,
            batteryPercent = 80,
            batteryTempC = 34f,
            isCharging = false,
            isPowerSaveMode = false,
            thermalLevel = ThermalLevel.COOL,
            currentRefreshRateHz = 120,
            maxSupportedRefreshRateHz = 120,
            screenWidthPx = 1080,
            screenHeightPx = 2340,
            densityDpi = 403,
            smallestWidthDp = 411,
            freeStorageGb = 64f,
            totalStorageGb = 128f,
            network = NetworkTelemetry("Wi-Fi", isConnected = true, isMetered = false),
            installedGames = emptyList(),
            readinessChecks = emptyList(),
        )
        val detectedA54 = repo.buildDetectedDeviceProfile(a54Telemetry)
        assertTrue(detectedA54.isDetectedCurrentDevice)
        assertEquals(196, detectedA54.baseSensitivity.general)
        assertEquals(500, detectedA54.baseSensitivity.recommendedDpi)
        assertTrue(detectedA54.preferredTouchSetting.contains("Touch Sensitivity"))
    }
}
