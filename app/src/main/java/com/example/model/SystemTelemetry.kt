package com.example.model

enum class ThermalLevel(val label: String) {
    COOL("Cool"),
    WARM("Warm"),
    HOT("Throttling"),
}

enum class ReadinessStatus {
    OPTIMAL,
    ACTION_NEEDED,
    WARNING,
}

enum class SettingsDestination {
    DISPLAY,
    BATTERY,
    BATTERY_OPTIMIZATION,
    WIFI,
    APP_DETAILS,
}

data class ReadinessCheck(
    val id: String,
    val title: String,
    val detail: String,
    val status: ReadinessStatus,
    val actionLabel: String? = null,
    val destination: SettingsDestination? = null,
)

data class NetworkTelemetry(
    val transportLabel: String,
    val isConnected: Boolean,
    val isMetered: Boolean,
)

data class InstalledGameInfo(
    val packageName: String,
    val displayName: String,
)

data class SystemTelemetry(
    val manufacturer: String,
    val model: String,
    val socLabel: String,
    val androidVersion: String,
    val cpuCores: Int,
    val totalRamMb: Long,
    val availableRamMb: Long,
    val lowMemory: Boolean,
    val batteryPercent: Int,
    val batteryTempC: Float,
    val isCharging: Boolean,
    val isPowerSaveMode: Boolean,
    val thermalLevel: ThermalLevel,
    val currentRefreshRateHz: Int,
    val maxSupportedRefreshRateHz: Int,
    val screenWidthPx: Int,
    val screenHeightPx: Int,
    val densityDpi: Int,
    val smallestWidthDp: Int,
    val freeStorageGb: Float,
    val totalStorageGb: Float,
    val network: NetworkTelemetry,
    val installedGames: List<InstalledGameInfo>,
    val readinessChecks: List<ReadinessCheck>,
    val isLowRamDevice: Boolean = false,
    val lowMemoryThresholdMb: Long = 0L,
    val jvmHeapUsedMb: Float = 0f,
    val jvmHeapMaxMb: Float = 1f,
    val nativeHeapAllocatedMb: Float = 0f,
    val appCacheSizeKb: Long = 0L,
    val isIgnoringBatteryOptimizations: Boolean = false,
    val isBackgroundRestricted: Boolean = false,
) {
    val usedRamPercent: Float
        get() = if (totalRamMb > 0) ((totalRamMb - availableRamMb).toFloat() / totalRamMb).coerceIn(0f, 1f) else 0f

    val usedStoragePercent: Float
        get() = if (totalStorageGb > 0f) ((totalStorageGb - freeStorageGb) / totalStorageGb).coerceIn(0f, 1f) else 0f

    val performanceScore: Int
        get() {
            val ramHeadroom = ((availableRamMb.toFloat() / totalRamMb.coerceAtLeast(1L)) * 100f).coerceIn(0f, 100f)
            val thermalPenalty = when (thermalLevel) {
                ThermalLevel.COOL -> 0
                ThermalLevel.WARM -> 12
                ThermalLevel.HOT -> 28
            }
            val saverPenalty = if (isPowerSaveMode) 18 else 0
            val hzBonus = when {
                currentRefreshRateHz >= 120 -> 15
                currentRefreshRateHz >= 90 -> 10
                else -> 0
            }
            val netBonus = if (network.isConnected) 8 else -10
            val base = (55 + ramHeadroom * 0.35f + hzBonus + netBonus - thermalPenalty - saverPenalty).toInt()
            return base.coerceIn(18, 99)
        }
}

sealed interface BoostState {
    data object Idle : BoostState
    data class Optimizing(
        val progress: Float,
        val statusStep: String,
        val completedSteps: List<String> = emptyList(),
    ) : BoostState
    data class Completed(
        val freedRamMb: Long,
        val jvmHeapFreedKb: Long,
        val cacheClearedKb: Long,
        val availableRamAfterMb: Long,
        val memoryHeadroomPercent: Int,
        val backgroundProcessesPaused: Int = 0,
        val maxRefreshRateApplied: Boolean = false,
        val completedSteps: List<String>,
        val timestampMs: Long,
        val summaryNote: String,
    ) : BoostState
}

data class BoostActionResult(
    val freedRamMb: Long,
    val jvmHeapFreedKb: Long,
    val cacheClearedKb: Long,
    val availableRamAfterMb: Long,
    val memoryHeadroomPercent: Int,
    val backgroundProcessesPaused: Int = 0,
    val maxRefreshRateApplied: Boolean = false,
    val completedSteps: List<String>,
    val summaryNote: String,
)
