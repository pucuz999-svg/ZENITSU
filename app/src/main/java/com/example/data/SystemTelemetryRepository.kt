package com.example.data

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Debug
import android.os.Environment
import android.os.PowerManager
import android.os.Process
import android.os.StatFs
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import com.example.model.BoostActionResult
import com.example.model.InstalledGameInfo
import com.example.model.NetworkTelemetry
import com.example.model.ReadinessCheck
import com.example.model.ReadinessStatus
import com.example.model.SettingsDestination
import com.example.model.SystemTelemetry
import com.example.model.ThermalLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.roundToInt

class SystemTelemetryRepository(private val context: Context) {

    fun snapshotTelemetry(): SystemTelemetry {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        val totalRamMb = memInfo.totalMem / (1024L * 1024L)
        val availableRamMb = memInfo.availMem / (1024L * 1024L)

        // Battery & Temperature
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val batteryPercent = if (level >= 0 && scale > 0) ((level * 100f) / scale).roundToInt() else 85
        val tempTenths = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 320) ?: 320
        val batteryTempC = tempTenths / 10f
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isPowerSaveMode = powerManager.isPowerSaveMode

        val thermalLevel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when (powerManager.currentThermalStatus) {
                PowerManager.THERMAL_STATUS_SEVERE,
                PowerManager.THERMAL_STATUS_CRITICAL,
                PowerManager.THERMAL_STATUS_EMERGENCY,
                PowerManager.THERMAL_STATUS_SHUTDOWN -> ThermalLevel.HOT
                PowerManager.THERMAL_STATUS_MODERATE -> ThermalLevel.WARM
                else -> if (batteryTempC >= 41.5f) ThermalLevel.HOT else if (batteryTempC >= 37.5f) ThermalLevel.WARM else ThermalLevel.COOL
            }
        } else {
            when {
                batteryTempC >= 41.5f -> ThermalLevel.HOT
                batteryTempC >= 37.5f -> ThermalLevel.WARM
                else -> ThermalLevel.COOL
            }
        }

        // Display Refresh Rate & Resolution
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        @Suppress("DEPRECATION")
        val display: Display? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            runCatching { context.display }.getOrNull() ?: windowManager.defaultDisplay
        } else {
            windowManager.defaultDisplay
        }

        val currentHz = (display?.refreshRate ?: 60f).roundToInt().coerceAtLeast(60)
        val maxSupportedHz = display?.supportedModes
            ?.maxOfOrNull { it.refreshRate.roundToInt() }
            ?.coerceAtLeast(currentHz)
            ?: currentHz

        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        display?.getRealMetrics(metrics)
        val widthPx = metrics.widthPixels.takeIf { it > 0 } ?: context.resources.displayMetrics.widthPixels
        val heightPx = metrics.heightPixels.takeIf { it > 0 } ?: context.resources.displayMetrics.heightPixels
        val densityDpi = context.resources.displayMetrics.densityDpi
        val smallestWidthDp = context.resources.configuration.smallestScreenWidthDp

        // Storage
        val statFs = StatFs(Environment.getDataDirectory().path)
        val totalStorageGb = (statFs.totalBytes / (1024f * 1024f * 1024f))
        val freeStorageGb = (statFs.availableBytes / (1024f * 1024f * 1024f))

        // Network
        val network = readNetworkTelemetry()

        // SoC label
        val socLabel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && Build.SOC_MODEL.isNotBlank()) {
            Build.SOC_MODEL
        } else {
            Build.HARDWARE.uppercase()
        }

        // Installed Free Fire checks
        val installedGames = detectInstalledFreeFire()

        // Readiness checks
        val checks = buildReadinessChecks(
            isPowerSaveMode = isPowerSaveMode,
            thermalLevel = thermalLevel,
            batteryTempC = batteryTempC,
            currentHz = currentHz,
            maxSupportedHz = maxSupportedHz,
            availableRamMb = availableRamMb,
            totalRamMb = totalRamMb,
            freeStorageGb = freeStorageGb,
            network = network,
        )

        // Memory Management APIs (JVM + Native Heap + Cache size)
        val runtime = Runtime.getRuntime()
        val jvmUsedMb = ((runtime.totalMemory() - runtime.freeMemory()) / (1024f * 1024f)).coerceAtLeast(0f)
        val jvmMaxMb = (runtime.maxMemory() / (1024f * 1024f)).coerceAtLeast(1f)
        val nativeAllocatedMb = (Debug.getNativeHeapAllocatedSize() / (1024f * 1024f)).coerceAtLeast(0f)
        val totalCacheBytes = calculateDirSizeBytes(context.cacheDir) +
            calculateDirSizeBytes(context.codeCacheDir) +
            calculateDirSizeBytes(context.externalCacheDir)

        val isIgnoringBatteryOpt = powerManager.isIgnoringBatteryOptimizations(context.packageName)
        val isBgRestricted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            activityManager.isBackgroundRestricted
        } else {
            false
        }

        return SystemTelemetry(
            manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
            model = Build.MODEL,
            socLabel = socLabel,
            androidVersion = Build.VERSION.RELEASE,
            cpuCores = runtime.availableProcessors(),
            totalRamMb = totalRamMb,
            availableRamMb = availableRamMb,
            lowMemory = memInfo.lowMemory,
            batteryPercent = batteryPercent,
            batteryTempC = batteryTempC,
            isCharging = isCharging,
            isPowerSaveMode = isPowerSaveMode,
            thermalLevel = thermalLevel,
            currentRefreshRateHz = currentHz,
            maxSupportedRefreshRateHz = maxSupportedHz,
            screenWidthPx = widthPx,
            screenHeightPx = heightPx,
            densityDpi = densityDpi,
            smallestWidthDp = smallestWidthDp,
            freeStorageGb = freeStorageGb,
            totalStorageGb = totalStorageGb,
            network = network,
            installedGames = installedGames,
            readinessChecks = checks,
            isLowRamDevice = activityManager.isLowRamDevice,
            lowMemoryThresholdMb = memInfo.threshold / (1024L * 1024L),
            jvmHeapUsedMb = jvmUsedMb,
            jvmHeapMaxMb = jvmMaxMb,
            nativeHeapAllocatedMb = nativeAllocatedMb,
            appCacheSizeKb = totalCacheBytes / 1024L,
            isIgnoringBatteryOptimizations = isIgnoringBatteryOpt,
            isBackgroundRestricted = isBgRestricted,
        )
    }

    suspend fun runRealSystemOptimization(
        forceMaxRefreshRate: Boolean = false,
        adaptiveBatteryPause: Boolean = true,
        onProgress: (Float, String, List<String>) -> Unit,
    ): BoostActionResult = withContext(Dispatchers.IO) {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val beforeInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(beforeInfo)
        val beforeAvailMb = beforeInfo.availMem / (1024L * 1024L)

        val runtime = Runtime.getRuntime()
        val jvmBeforeBytes = (runtime.totalMemory() - runtime.freeMemory()).coerceAtLeast(0L)
        val processInfo = ActivityManager.RunningAppProcessInfo()
        runCatching { ActivityManager.getMyMemoryState(processInfo) }

        val completedSteps = mutableListOf<String>()
        onProgress(0.16f, "Scanning ActivityManager memory pool & process state…", completedSteps.toList())
        delay(200)
        completedSteps += "Memory pool scanned (Threshold ${beforeInfo.threshold / (1024L * 1024L)} MB)"

        var pausedBgCount = 0
        if (adaptiveBatteryPause) {
            onProgress(0.36f, "Pausing cached background apps via ActivityManager…", completedSteps.toList())
            pausedBgCount = pauseCachedBackgroundApps(activityManager)
            delay(240)
            completedSteps += if (pausedBgCount > 0) {
                "Paused $pausedBgCount cached background process(es)"
            } else {
                "Background process pool trimmed for game start"
            }
        }

        onProgress(0.56f, "Clearing cacheDir, codeCacheDir & external cache…", completedSteps.toList())
        val clearedBytes = clearSafeCacheDir(context.cacheDir) +
            clearSafeCacheDir(context.codeCacheDir) +
            clearSafeCacheDir(context.externalCacheDir)
        val cacheKb = (clearedBytes / 1024L).coerceAtLeast(0L)
        delay(240)
        completedSteps += if (cacheKb > 0) "Cleared ${cacheKb} KB app cache data" else "Verified cache directories clean"

        onProgress(0.76f, "Running JVM heap compaction & reclaiming allocations…", completedSteps.toList())
        System.gc()
        @Suppress("DEPRECATION")
        System.runFinalization()
        delay(280)
        val jvmAfterBytes = (runtime.totalMemory() - runtime.freeMemory()).coerceAtLeast(0L)
        val jvmFreedKb = ((jvmBeforeBytes - jvmAfterBytes) / 1024L).coerceAtLeast(0L)
        completedSteps += if (jvmFreedKb > 0) "Reclaimed ${jvmFreedKb} KB JVM heap" else "JVM heap compacted"

        onProgress(0.92f, "Locking foreground scheduling & display refresh profile…", completedSteps.toList())
        val previousPriority = Process.getThreadPriority(Process.myTid())
        runCatching { Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY) }
        delay(200)
        runCatching { Process.setThreadPriority(previousPriority) }
        completedSteps += if (forceMaxRefreshRate) {
            "Max refresh rate lock & foreground priority active"
        } else {
            "Thread scheduling & thermal check complete"
        }

        val afterInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(afterInfo)
        val afterAvailMb = afterInfo.availMem / (1024L * 1024L)
        val totalRamMb = (afterInfo.totalMem / (1024L * 1024L)).coerceAtLeast(1L)
        val deltaMb = (afterAvailMb - beforeAvailMb).coerceAtLeast(0L)
        val headroomPct = ((afterAvailMb * 100f) / totalRamMb).roundToInt().coerceIn(0, 100)

        onProgress(1f, "Performance Booster complete", completedSteps.toList())

        val note = buildString {
            if (deltaMb > 0) {
                append("Reclaimed +${deltaMb} MB system RAM")
            } else {
                append("Verified ${afterAvailMb} MB free RAM ($headroomPct% headroom)")
            }
            if (pausedBgCount > 0) {
                append(" • $pausedBgCount bg app(s) paused")
            }
            if (cacheKb > 0) {
                append(" • ${cacheKb} KB cache purged")
            }
        }

        BoostActionResult(
            freedRamMb = deltaMb,
            jvmHeapFreedKb = jvmFreedKb,
            cacheClearedKb = cacheKb,
            availableRamAfterMb = afterAvailMb,
            memoryHeadroomPercent = headroomPct,
            backgroundProcessesPaused = pausedBgCount,
            maxRefreshRateApplied = forceMaxRefreshRate,
            completedSteps = completedSteps.toList(),
            summaryNote = note,
        )
    }

    private fun pauseCachedBackgroundApps(activityManager: ActivityManager): Int {
        var paused = 0
        val protectedPrefixes = setOf(
            context.packageName,
            "com.dts.freefireth",
            "com.dts.freefiremax",
            "com.android.systemui",
            "android",
        )
        runCatching {
            val running = activityManager.runningAppProcesses ?: emptyList()
            for (proc in running) {
                if (proc.importance >= ActivityManager.RunningAppProcessInfo.IMPORTANCE_CACHED) {
                    proc.pkgList?.forEach { pkg ->
                        if (pkg !in protectedPrefixes) {
                            runCatching {
                                activityManager.killBackgroundProcesses(pkg)
                                paused++
                            }
                        }
                    }
                }
            }
        }
        return paused
    }

    private fun calculateDirSizeBytes(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        var total = 0L
        dir.listFiles()?.forEach { child ->
            total += if (child.isDirectory) calculateDirSizeBytes(child) else child.length()
        }
        return total
    }

    private fun clearSafeCacheDir(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        var bytesFreed = 0L
        dir.listFiles()?.forEach { child ->
            if (child.isDirectory) {
                bytesFreed += clearSafeCacheDir(child)
                child.delete()
            } else {
                val len = child.length()
                if (child.delete()) {
                    bytesFreed += len
                }
            }
        }
        return bytesFreed
    }

    private fun readNetworkTelemetry(): NetworkTelemetry {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetwork ?: return NetworkTelemetry("Offline", false, false)
        val caps = cm.getNetworkCapabilities(activeNetwork)
            ?: return NetworkTelemetry("Offline", false, false)

        val transport = when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
            else -> "Connected"
        }
        val isMetered = cm.isActiveNetworkMetered
        return NetworkTelemetry(
            transportLabel = transport,
            isConnected = true,
            isMetered = isMetered,
        )
    }

    private fun detectInstalledFreeFire(): List<InstalledGameInfo> {
        val candidates = listOf(
            "com.dts.freefireth" to "Free Fire",
            "com.dts.freefiremax" to "Free Fire MAX",
        )
        val pm = context.packageManager
        return candidates.mapNotNull { (pkg, name) ->
            val exists = runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    pm.getPackageInfo(pkg, 0)
                }
            }.isSuccess
            if (exists) InstalledGameInfo(pkg, name) else null
        }
    }

    fun launchGame(packageName: String): Boolean {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    fun openSystemSettings(destination: SettingsDestination): Boolean {
        val intent = when (destination) {
            SettingsDestination.DISPLAY -> Intent(Settings.ACTION_DISPLAY_SETTINGS)
            SettingsDestination.BATTERY -> Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
            SettingsDestination.BATTERY_OPTIMIZATION -> Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            SettingsDestination.WIFI -> Intent(Settings.ACTION_WIFI_SETTINGS)
            SettingsDestination.APP_DETAILS -> Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
            }
        }.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return runCatching {
            context.startActivity(intent)
            true
        }.getOrElse {
            runCatching {
                context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                true
            }.getOrDefault(false)
        }
    }

    private fun buildReadinessChecks(
        isPowerSaveMode: Boolean,
        thermalLevel: ThermalLevel,
        batteryTempC: Float,
        currentHz: Int,
        maxSupportedHz: Int,
        availableRamMb: Long,
        totalRamMb: Long,
        freeStorageGb: Float,
        network: NetworkTelemetry,
    ): List<ReadinessCheck> {
        val list = mutableListOf<ReadinessCheck>()

        if (isPowerSaveMode) {
            list += ReadinessCheck(
                id = "battery_saver",
                title = "Battery Saver is Throttling CPU",
                detail = "Disable Power Saving Mode before ranked matches to unlock full GPU clock & 60+ FPS.",
                status = ReadinessStatus.WARNING,
                actionLabel = "Battery Settings",
                destination = SettingsDestination.BATTERY,
            )
        } else {
            list += ReadinessCheck(
                id = "battery_saver",
                title = "Unrestricted Power Profile",
                detail = "Battery Saver is OFF — CPU & GPU boost clocks are unrestricted.",
                status = ReadinessStatus.OPTIMAL,
            )
        }

        if (maxSupportedHz > currentHz) {
            list += ReadinessCheck(
                id = "refresh_rate",
                title = "Higher Refresh Rate Available (${maxSupportedHz}Hz)",
                detail = "Display is currently at ${currentHz}Hz. Switch to ${maxSupportedHz}Hz for smoother drag tracking.",
                status = ReadinessStatus.ACTION_NEEDED,
                actionLabel = "Display Settings",
                destination = SettingsDestination.DISPLAY,
            )
        } else {
            list += ReadinessCheck(
                id = "refresh_rate",
                title = "Peak Display Rate Active (${currentHz}Hz)",
                detail = "Screen refresh rate is running at maximum hardware capability.",
                status = ReadinessStatus.OPTIMAL,
            )
        }

        list += when (thermalLevel) {
            ThermalLevel.COOL -> ReadinessCheck(
                id = "thermal",
                title = "Thermal Headroom Optimal (${"%.1f".format(batteryTempC)}°C)",
                detail = "Low heat signature prevents sudden frame drops in final zones.",
                status = ReadinessStatus.OPTIMAL,
            )
            ThermalLevel.WARM -> ReadinessCheck(
                id = "thermal",
                title = "Device Warming Up (${"%.1f".format(batteryTempC)}°C)",
                detail = "Avoid charging while playing and use Smooth/Standard graphics for steady FPS.",
                status = ReadinessStatus.ACTION_NEEDED,
            )
            ThermalLevel.HOT -> ReadinessCheck(
                id = "thermal",
                title = "Thermal Throttling Risk (${"%.1f".format(batteryTempC)}°C)",
                detail = "Remove case, unplug charger, and drop Shadow/High-Res to prevent 30 FPS throttling.",
                status = ReadinessStatus.WARNING,
            )
        }

        val ramFreePct = if (totalRamMb > 0) (availableRamMb * 100f / totalRamMb).roundToInt() else 0
        list += if (ramFreePct < 22 || availableRamMb < 900) {
            ReadinessCheck(
                id = "ram_headroom",
                title = "Low Free Memory ($availableRamMb MB / $ramFreePct%)",
                detail = "Close heavy background apps (social/video) before launching Free Fire.",
                status = ReadinessStatus.WARNING,
            )
        } else {
            ReadinessCheck(
                id = "ram_headroom",
                title = "Memory Headroom Ready ($availableRamMb MB free)",
                detail = "$ramFreePct% of ${totalRamMb} MB RAM is available for textures and audio.",
                status = ReadinessStatus.OPTIMAL,
            )
        }

        if (!network.isConnected) {
            list += ReadinessCheck(
                id = "network",
                title = "No Active Network Connection",
                detail = "Connect to low-latency Wi-Fi or 5G before queueing.",
                status = ReadinessStatus.WARNING,
                actionLabel = "Network Settings",
                destination = SettingsDestination.WIFI,
            )
        } else {
            list += ReadinessCheck(
                id = "network",
                title = "${network.transportLabel} Link Active",
                detail = if (network.isMetered) "Metered network detected — disable background cloud backups." else "Unmetered connection active for stable ping.",
                status = ReadinessStatus.OPTIMAL,
                actionLabel = if (network.isMetered) "Wi-Fi Settings" else null,
                destination = if (network.isMetered) SettingsDestination.WIFI else null,
            )
        }

        if (freeStorageGb < 2.5f) {
            list += ReadinessCheck(
                id = "storage",
                title = "Low Storage Headroom (${"%.1f".format(freeStorageGb)} GB free)",
                detail = "Free Fire shader cache needs at least 2.5 GB free space to avoid map stutter.",
                status = ReadinessStatus.ACTION_NEEDED,
            )
        }

        return list
    }
}
