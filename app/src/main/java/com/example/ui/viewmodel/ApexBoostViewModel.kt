package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DeviceCatalogRepository
import com.example.data.SystemTelemetryRepository
import com.example.data.UserPreferencesRepository
import com.example.model.ActiveAppliedProfile
import com.example.model.BoostState
import com.example.model.CalibrationSummary
import com.example.model.DeviceSensitivityProfile
import com.example.model.DeviceSmoothSuggestion
import com.example.model.DpiCalculationResult
import com.example.model.DragAttemptResult
import com.example.model.DragOutcome
import com.example.model.FpsGraphicsPreset
import com.example.model.PlatformBrand
import com.example.model.PlayStyleMode
import com.example.model.SavedCalibratedProfile
import com.example.model.SensitivityValues
import com.example.model.SettingsDestination
import com.example.model.SmoothPlayPriority
import com.example.model.SystemTelemetry
import com.example.model.TargetDistance
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class ApexBoostViewModel(application: Application) : AndroidViewModel(application) {

    private val telemetryRepo = SystemTelemetryRepository(application)
    private val catalogRepo = DeviceCatalogRepository()
    private val prefsRepo = UserPreferencesRepository(application)

    private val _telemetry = MutableStateFlow(telemetryRepo.snapshotTelemetry())
    val telemetry: StateFlow<SystemTelemetry> = _telemetry.asStateFlow()

    private val _boostState = MutableStateFlow<BoostState>(BoostState.Idle)
    val boostState: StateFlow<BoostState> = _boostState.asStateFlow()

    // Live Choreographer FPS & Frame-Time Samples (ms)
    private val _liveFps = MutableStateFlow(60)
    val liveFps: StateFlow<Int> = _liveFps.asStateFlow()

    private val _frameTimeHistoryMs = MutableStateFlow<List<Float>>(emptyList())
    val frameTimeHistoryMs: StateFlow<List<Float>> = _frameTimeHistoryMs.asStateFlow()

    val latestFrameTimeMs: StateFlow<Float> = combine(_frameTimeHistoryMs, _telemetry) { history, tel ->
        history.lastOrNull() ?: (1000f / tel.currentRefreshRateHz.coerceAtLeast(60))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 16.6f)

    // Device Sensitivity Explorer state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedBrand = MutableStateFlow(PlatformBrand.ALL)
    val selectedBrand: StateFlow<PlatformBrand> = _selectedBrand.asStateFlow()

    private val _selectedPlayStyle = MutableStateFlow(PlayStyleMode.ONE_TAP_DRAG)
    val selectedPlayStyle: StateFlow<PlayStyleMode> = _selectedPlayStyle.asStateFlow()

    val favoriteDeviceIds: StateFlow<Set<String>> = prefsRepo.favoriteDeviceIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val forceMaxRefreshRate: StateFlow<Boolean> = prefsRepo.forceMaxRefreshRate
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val adaptiveBatteryPause: StateFlow<Boolean> = prefsRepo.adaptiveBatteryPause
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val floatingFpsOverlayEnabled: StateFlow<Boolean> = prefsRepo.floatingFpsOverlayEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val thunderSoundEnabled: StateFlow<Boolean> = prefsRepo.thunderSoundEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val detectedDeviceProfile: StateFlow<DeviceSensitivityProfile> = combine(_telemetry) { (tel) ->
        catalogRepo.buildDetectedDeviceProfile(tel)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        catalogRepo.buildDetectedDeviceProfile(_telemetry.value),
    )

    val activeAppliedProfile: StateFlow<ActiveAppliedProfile> = combine(
        prefsRepo.activeAppliedProfile,
        detectedDeviceProfile,
    ) { saved, detected ->
        saved ?: ActiveAppliedProfile(
            sourceName = "${detected.deviceName} (Scribd Auto)",
            sensitivity = detected.baseSensitivity,
            fpsPreset = detected.fpsPreset,
            isCustomized = false,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        run {
            val detected = catalogRepo.buildDetectedDeviceProfile(_telemetry.value)
            ActiveAppliedProfile(
                sourceName = "${detected.deviceName} (Scribd Auto)",
                sensitivity = detected.baseSensitivity,
                fpsPreset = detected.fpsPreset,
                isCustomized = false,
            )
        },
    )

    val allDevicesCatalog: StateFlow<List<DeviceSensitivityProfile>> = combine(detectedDeviceProfile) { (detected) ->
        listOf(detected) + catalogRepo.getAllCuratedDevices()
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        listOf(catalogRepo.buildDetectedDeviceProfile(_telemetry.value)) + catalogRepo.getAllCuratedDevices(),
    )

    // Smooth Gameplay Device Suggestion Advisor
    private val _smoothPriority = MutableStateFlow(SmoothPlayPriority.MAX_FPS)
    val smoothPriority: StateFlow<SmoothPlayPriority> = _smoothPriority.asStateFlow()

    private val _selectedSuggestionDeviceId = MutableStateFlow<String?>(null)
    val selectedSuggestionDeviceId: StateFlow<String?> = _selectedSuggestionDeviceId.asStateFlow()

    val smoothSuggestion: StateFlow<DeviceSmoothSuggestion> = combine(
        _telemetry,
        detectedDeviceProfile,
        _smoothPriority,
        _selectedSuggestionDeviceId,
    ) { tel, detected, priority, targetId ->
        val target = if (targetId == null || targetId == detected.id) {
            detected
        } else {
            catalogRepo.getAllCuratedDevices().firstOrNull { it.id == targetId } ?: detected
        }
        catalogRepo.suggestBestSmoothGameplay(tel, priority, target)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        catalogRepo.suggestBestSmoothGameplay(
            _telemetry.value,
            SmoothPlayPriority.MAX_FPS,
            catalogRepo.buildDetectedDeviceProfile(_telemetry.value),
        ),
    )

    val filteredDevices: StateFlow<List<DeviceSensitivityProfile>> = combine(
        detectedDeviceProfile,
        _searchQuery,
        _selectedBrand,
        favoriteDeviceIds,
    ) { detected, query, brand, favorites ->
        val allDevices = listOf(detected) + catalogRepo.getAllCuratedDevices()
        val q = query.trim().lowercase()
        allDevices.filter { device ->
            val brandMatches = when (brand) {
                PlatformBrand.ALL -> true
                PlatformBrand.DETECTED -> device.isDetectedCurrentDevice
                PlatformBrand.SAVED -> favorites.contains(device.id)
                else -> device.brand == brand
            }
            val queryMatches = q.isEmpty() ||
                device.deviceName.lowercase().contains(q) ||
                device.platformBadge.lowercase().contains(q) ||
                device.brand.label.lowercase().contains(q) ||
                device.screenSpec.lowercase().contains(q) ||
                device.preferredTouchSetting.lowercase().contains(q)
            brandMatches && queryMatches
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        listOf(catalogRepo.buildDetectedDeviceProfile(_telemetry.value)) + catalogRepo.getAllCuratedDevices(),
    )

    // Touch Sensitivity Calibrator state
    private val _targetDistance = MutableStateFlow(TargetDistance.MID_30M)
    val targetDistance: StateFlow<TargetDistance> = _targetDistance.asStateFlow()

    private val _dragAttempts = MutableStateFlow<List<DragAttemptResult>>(emptyList())
    val dragAttempts: StateFlow<List<DragAttemptResult>> = _dragAttempts.asStateFlow()

    val calibrationSummary: StateFlow<CalibrationSummary?> = combine(
        _dragAttempts,
        detectedDeviceProfile,
    ) { attempts, detected ->
        if (attempts.isEmpty()) null else computeCalibrationSummary(attempts, detected)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val savedCalibratedProfile: StateFlow<SavedCalibratedProfile?> = prefsRepo.savedCalibratedProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // DPI Calculator & Checklist state
    private val _dpiTargetInput = MutableStateFlow(480)
    val dpiTargetInput: StateFlow<Int> = _dpiTargetInput.asStateFlow()

    private val _thumbSpeedLevel = MutableStateFlow(2)
    val thumbSpeedLevel: StateFlow<Int> = _thumbSpeedLevel.asStateFlow()

    val dpiCalculation: StateFlow<DpiCalculationResult> = combine(
        _telemetry,
        _dpiTargetInput,
        _thumbSpeedLevel,
    ) { tel, targetDpi, thumbSpeed ->
        DpiCalculationResult.calculate(
            currentDpi = tel.smallestWidthDp.coerceAtLeast(320),
            targetDpi = targetDpi,
            displayHz = tel.maxSupportedRefreshRateHz,
            thumbSpeedLevel = thumbSpeed,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DpiCalculationResult.calculate(
            currentDpi = _telemetry.value.smallestWidthDp.coerceAtLeast(320),
            targetDpi = 480,
            displayHz = _telemetry.value.maxSupportedRefreshRateHz,
            thumbSpeedLevel = 2,
        ),
    )

    val completedChecklistIds: StateFlow<Set<String>> = prefsRepo.completedChecklistIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    init {
        startTelemetryPolling()
        viewModelScope.launch {
            prefsRepo.savedDpiTarget.collect { savedDpi ->
                if (savedDpi != null) {
                    _dpiTargetInput.value = savedDpi
                } else {
                    _dpiTargetInput.value = (_telemetry.value.smallestWidthDp + 90).coerceIn(420, 620)
                }
            }
        }
        viewModelScope.launch {
            prefsRepo.savedThumbSpeed.collect { speed ->
                _thumbSpeedLevel.value = speed
            }
        }
    }

    private fun startTelemetryPolling() {
        viewModelScope.launch {
            while (isActive) {
                _telemetry.value = telemetryRepo.snapshotTelemetry()
                delay(1800L)
            }
        }
    }

    fun refreshTelemetryNow() {
        _telemetry.value = telemetryRepo.snapshotTelemetry()
    }

    fun recordFrameTimeSample(frameTimeMs: Float, currentFps: Int) {
        _liveFps.value = currentFps
        val updated = (_frameTimeHistoryMs.value + frameTimeMs).takeLast(36)
        _frameTimeHistoryMs.value = updated
    }

    fun setForceMaxRefreshRate(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepo.setForceMaxRefreshRate(enabled)
        }
    }

    fun setAdaptiveBatteryPause(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepo.setAdaptiveBatteryPause(enabled)
        }
    }

    fun setFloatingFpsOverlay(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepo.setFloatingFpsOverlay(enabled)
        }
    }

    fun setThunderSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepo.setThunderSoundEnabled(enabled)
        }
    }

    fun selectSmoothPriority(priority: SmoothPlayPriority) {
        _smoothPriority.value = priority
    }

    fun selectSuggestionDevice(deviceId: String?) {
        _selectedSuggestionDeviceId.value = deviceId
    }

    fun applyScribdPreset(
        sourceName: String,
        sensitivity: SensitivityValues,
        fpsPreset: FpsGraphicsPreset,
        isCustomized: Boolean = false,
    ) {
        viewModelScope.launch {
            prefsRepo.saveActiveAppliedProfile(
                sourceName = sourceName,
                sensitivity = sensitivity,
                fpsPreset = fpsPreset,
                isCustomized = isCustomized,
            )
        }
    }

    fun updateActiveSensitivity(updated: SensitivityValues) {
        val current = activeAppliedProfile.value
        applyScribdPreset(
            sourceName = current.sourceName.removeSuffix(" (Scribd Auto)").let { "$it (Custom)" },
            sensitivity = updated,
            fpsPreset = current.fpsPreset,
            isCustomized = true,
        )
    }

    fun updateActiveFpsPreset(updated: FpsGraphicsPreset) {
        val current = activeAppliedProfile.value
        applyScribdPreset(
            sourceName = current.sourceName,
            sensitivity = current.sensitivity,
            fpsPreset = updated,
            isCustomized = true,
        )
    }

    fun resetActiveToScribdAuto() {
        viewModelScope.launch {
            prefsRepo.clearActiveAppliedProfile()
        }
    }

    fun activateAllFeatures() {
        viewModelScope.launch {
            prefsRepo.setForceMaxRefreshRate(true)
            prefsRepo.setAdaptiveBatteryPause(true)
            prefsRepo.setFloatingFpsOverlay(true)
            prefsRepo.setThunderSoundEnabled(true)
            val suggestion = smoothSuggestion.value
            prefsRepo.saveActiveAppliedProfile(
                sourceName = "${suggestion.targetDevice.deviceName} (${suggestion.priority.label})",
                sensitivity = suggestion.recommendedSensitivity,
                fpsPreset = suggestion.recommendedFps,
                isCustomized = false,
            )
            runOptimization()
        }
    }

    fun runOptimization() {
        if (_boostState.value is BoostState.Optimizing) return
        viewModelScope.launch {
            val maxHz = forceMaxRefreshRate.value
            val bgPause = adaptiveBatteryPause.value
            _boostState.value = BoostState.Optimizing(0.1f, "Inspecting active memory pool…", emptyList())
            val result = telemetryRepo.runRealSystemOptimization(
                forceMaxRefreshRate = maxHz,
                adaptiveBatteryPause = bgPause,
            ) { progress, step, steps ->
                _boostState.value = BoostState.Optimizing(progress, step, steps)
            }
            _telemetry.value = telemetryRepo.snapshotTelemetry()
            _boostState.value = BoostState.Completed(
                freedRamMb = result.freedRamMb,
                jvmHeapFreedKb = result.jvmHeapFreedKb,
                cacheClearedKb = result.cacheClearedKb,
                availableRamAfterMb = result.availableRamAfterMb,
                memoryHeadroomPercent = result.memoryHeadroomPercent,
                backgroundProcessesPaused = result.backgroundProcessesPaused,
                maxRefreshRateApplied = result.maxRefreshRateApplied,
                completedSteps = result.completedSteps,
                timestampMs = System.currentTimeMillis(),
                summaryNote = result.summaryNote,
            )
        }
    }

    fun prepareAndLaunchGame(packageName: String, onLaunchResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (adaptiveBatteryPause.value || forceMaxRefreshRate.value) {
                val result = telemetryRepo.runRealSystemOptimization(
                    forceMaxRefreshRate = forceMaxRefreshRate.value,
                    adaptiveBatteryPause = adaptiveBatteryPause.value,
                ) { progress, step, steps ->
                    _boostState.value = BoostState.Optimizing(progress, step, steps)
                }
                _telemetry.value = telemetryRepo.snapshotTelemetry()
                _boostState.value = BoostState.Completed(
                    freedRamMb = result.freedRamMb,
                    jvmHeapFreedKb = result.jvmHeapFreedKb,
                    cacheClearedKb = result.cacheClearedKb,
                    availableRamAfterMb = result.availableRamAfterMb,
                    memoryHeadroomPercent = result.memoryHeadroomPercent,
                    backgroundProcessesPaused = result.backgroundProcessesPaused,
                    maxRefreshRateApplied = result.maxRefreshRateApplied,
                    completedSteps = result.completedSteps,
                    timestampMs = System.currentTimeMillis(),
                    summaryNote = result.summaryNote,
                )
            }
            onLaunchResult(telemetryRepo.launchGame(packageName))
        }
    }

    fun launchGame(packageName: String): Boolean = telemetryRepo.launchGame(packageName)

    fun openSystemSettings(destination: SettingsDestination): Boolean =
        telemetryRepo.openSystemSettings(destination)

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectBrand(brand: PlatformBrand) {
        _selectedBrand.value = brand
    }

    fun selectPlayStyle(mode: PlayStyleMode) {
        _selectedPlayStyle.value = mode
    }

    fun toggleFavoriteDevice(deviceId: String) {
        viewModelScope.launch {
            prefsRepo.toggleFavoriteDevice(deviceId)
        }
    }

    fun selectTargetDistance(distance: TargetDistance) {
        _targetDistance.value = distance
    }

    fun recordDragAttempt(
        outcome: DragOutcome,
        velocityPxPerMs: Float,
        angleDeviationDeg: Float,
        dragDistanceDp: Float,
        sampleRateHz: Int,
    ) {
        val attempt = DragAttemptResult(
            id = System.currentTimeMillis(),
            distance = _targetDistance.value,
            outcome = outcome,
            velocityPxPerMs = velocityPxPerMs,
            angleDeviationDeg = angleDeviationDeg,
            dragDistanceDp = dragDistanceDp,
            sampleRateHz = sampleRateHz,
        )
        _dragAttempts.value = (_dragAttempts.value + attempt).takeLast(12)
    }

    fun resetCalibrationSession() {
        _dragAttempts.value = emptyList()
    }

    fun saveCalibratedProfile() {
        val summary = calibrationSummary.value ?: return
        viewModelScope.launch {
            prefsRepo.saveCalibratedProfile(
                sensitivity = summary.recommendedSensitivity,
                headshotRatePercent = summary.headshotRatePercent,
                measuredTouchHz = summary.measuredTouchHz,
            )
        }
    }

    fun updateDpiCalculator(targetDpi: Int, thumbSpeedLevel: Int) {
        _dpiTargetInput.value = targetDpi.coerceIn(360, 800)
        _thumbSpeedLevel.value = thumbSpeedLevel.coerceIn(1, 3)
        viewModelScope.launch {
            prefsRepo.saveDpiCalculatorInputs(_dpiTargetInput.value, _thumbSpeedLevel.value)
        }
    }

    fun toggleChecklistItem(itemId: String) {
        viewModelScope.launch {
            prefsRepo.toggleChecklistItem(itemId)
        }
    }

    private fun computeCalibrationSummary(
        attempts: List<DragAttemptResult>,
        detected: DeviceSensitivityProfile,
    ): CalibrationSummary {
        val total = attempts.size
        val headshots = attempts.count { it.outcome == DragOutcome.HEADSHOT }
        val overshoots = attempts.count { it.outcome == DragOutcome.OVERSHOT }
        val undershoots = attempts.count { it.outcome == DragOutcome.UNDERSHOT }
        val hsRate = ((headshots * 100f) / total).roundToInt()

        val avgVel = attempts.map { it.velocityPxPerMs }.average().toFloat()
        val avgAngle = attempts.map { it.angleDeviationDeg }.average().toFloat()
        val avgHz = attempts.map { it.sampleRateHz }.average().roundToInt().coerceAtLeast(60)

        // Adjust baseline sensitivity according to actual drag bias & thumb velocity
        val biasDelta = (undershoots - overshoots) * 4
        val speedAdjust = when {
            avgVel > 4.2f -> -6
            avgVel < 1.8f -> 7
            else -> 0
        }

        val base = detected.baseSensitivity
        val recommended = SensitivityValues(
            general = (base.general + biasDelta + speedAdjust).coerceIn(120, 200),
            redDot = (base.redDot + biasDelta + speedAdjust).coerceIn(115, 200),
            scope2x = (base.scope2x + (biasDelta / 2)).coerceIn(110, 195),
            scope4x = (base.scope4x + (biasDelta / 2)).coerceIn(95, 190),
            sniperScope = base.sniperScope,
            freeLook = base.freeLook,
            fireButtonPercent = when {
                avgAngle > 18f -> (base.fireButtonPercent + 3).coerceIn(44, 62)
                avgVel > 4.0f -> (base.fireButtonPercent - 2).coerceIn(40, 58)
                else -> base.fireButtonPercent
            },
            recommendedDpi = base.recommendedDpi,
        )

        val verdict = when {
            total < 5 -> "Perform ${5 - total} more drags for full calibration lock."
            hsRate >= 70 && avgAngle <= 12f -> "Pro Drag Accuracy! Your swipe line and velocity are tournament-ready."
            overshoots > undershoots -> "Fast flick style detected — sensitivity toned slightly for cleaner head locks."
            undershoots > overshoots -> "Controlled drag style detected — General & Red Dot boosted for easier head snaps."
            avgAngle > 16f -> "Slight curved drag detected — enlarged Fire Button slightly to straighten vertical flicks."
            else -> "Balanced drag calibration locked for ${detected.deviceName}."
        }

        return CalibrationSummary(
            attemptsCount = total,
            headshotRatePercent = hsRate,
            avgVelocityPxPerMs = avgVel,
            avgAngleDeviationDeg = avgAngle,
            measuredTouchHz = avgHz,
            recommendedSensitivity = recommended,
            profileVerdict = verdict,
        )
    }
}
