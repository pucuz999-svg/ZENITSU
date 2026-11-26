package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.ActiveAppliedProfile
import com.example.model.BoostState
import com.example.model.DeviceSensitivityProfile
import com.example.model.DeviceSmoothSuggestion
import com.example.model.FpsGraphicsPreset
import com.example.model.ReadinessCheck
import com.example.model.ReadinessStatus
import com.example.model.SensitivityValues
import com.example.model.SettingsDestination
import com.example.model.SmoothPlayPriority
import com.example.model.SystemTelemetry
import com.example.model.ThermalLevel
import com.example.ui.components.ActiveScribdProfileEditorCard
import com.example.ui.components.BestSmoothSuggestionsCard
import com.example.ui.components.FpsPresetSummaryGrid
import com.example.ui.components.FramePacingSparkline
import com.example.ui.components.MiniSpecChip
import com.example.ui.components.ResponsiveScreenContainer
import com.example.ui.components.StatusPill
import com.example.ui.components.TacticalCardSurface
import com.example.ui.components.TacticalSectionHeader
import com.example.ui.theme.ApexOrange
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.CyanTelemetry
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.OptimalGreen
import com.example.ui.theme.TacticalCard
import com.example.ui.theme.TacticalCardBright
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.ThermalAmber
import com.example.ui.theme.ThunderInk
import kotlin.math.roundToInt

@Composable
fun BoostHubScreen(
    telemetry: SystemTelemetry,
    detectedProfile: DeviceSensitivityProfile,
    boostState: BoostState,
    liveFps: Int,
    frameTimeHistoryMs: List<Float>,
    forceMaxRefreshRate: Boolean,
    adaptiveBatteryPause: Boolean,
    floatingFpsOverlayEnabled: Boolean,
    thunderSoundEnabled: Boolean,
    smoothSuggestion: DeviceSmoothSuggestion,
    allDevices: List<DeviceSensitivityProfile>,
    activeProfile: ActiveAppliedProfile,
    onToggleForceMaxRefreshRate: (Boolean) -> Unit,
    onToggleAdaptiveBatteryPause: (Boolean) -> Unit,
    onToggleFloatingFpsOverlay: (Boolean) -> Unit,
    onToggleThunderSound: (Boolean) -> Unit,
    onPreviewThunderSound: () -> Unit,
    onSelectSmoothPriority: (SmoothPlayPriority) -> Unit,
    onSelectSuggestionDevice: (String?) -> Unit,
    onApplyScribdPreset: (String, SensitivityValues, FpsGraphicsPreset) -> Unit,
    onUpdateActiveSensitivity: (SensitivityValues) -> Unit,
    onUpdateActiveFpsPreset: (FpsGraphicsPreset) -> Unit,
    onResetActiveToAuto: () -> Unit,
    onActivateAllFeatures: () -> Unit,
    onOptimizeClick: () -> Unit,
    onLaunchGame: (String) -> Unit,
    onOpenSettings: (SettingsDestination) -> Unit,
    onNavigateToDevices: () -> Unit,
    onPresetCopied: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            ResponsiveScreenContainer {
                HeroPerformanceBanner(
                    telemetry = telemetry,
                    boostState = boostState,
                    onOptimizeClick = onOptimizeClick,
                    onActivateAllFeatures = onActivateAllFeatures,
                )
            }
        }

        item {
            ResponsiveScreenContainer {
                BoostActionResultBanner(boostState = boostState)
            }
        }

        item {
            ResponsiveScreenContainer {
                GameModeControlsCard(
                    telemetry = telemetry,
                    forceMaxRefreshRate = forceMaxRefreshRate,
                    adaptiveBatteryPause = adaptiveBatteryPause,
                    floatingFpsOverlayEnabled = floatingFpsOverlayEnabled,
                    thunderSoundEnabled = thunderSoundEnabled,
                    onToggleForceMaxRefreshRate = onToggleForceMaxRefreshRate,
                    onToggleAdaptiveBatteryPause = onToggleAdaptiveBatteryPause,
                    onToggleFloatingFpsOverlay = onToggleFloatingFpsOverlay,
                    onToggleThunderSound = onToggleThunderSound,
                    onPreviewThunderSound = onPreviewThunderSound,
                    onOpenSettings = onOpenSettings,
                )
            }
        }

        item {
            ResponsiveScreenContainer {
                BestSmoothSuggestionsCard(
                    suggestion = smoothSuggestion,
                    allDevices = allDevices,
                    onSelectPriority = onSelectSmoothPriority,
                    onSelectDevice = onSelectSuggestionDevice,
                    onApplyPreset = onApplyScribdPreset,
                    onPresetCopied = onPresetCopied,
                )
            }
        }

        item {
            ResponsiveScreenContainer {
                ActiveScribdProfileEditorCard(
                    activeProfile = activeProfile,
                    maxRefreshRateHz = telemetry.maxSupportedRefreshRateHz,
                    onUpdateSensitivity = onUpdateActiveSensitivity,
                    onUpdateFpsPreset = onUpdateActiveFpsPreset,
                    onResetAuto = onResetActiveToAuto,
                    onPresetCopied = onPresetCopied,
                )
            }
        }

        item {
            ResponsiveScreenContainer {
                TacticalSectionHeader(
                    title = stringResource(R.string.section_frame_pacing),
                    icon = Icons.Rounded.Speed,
                    accentColor = CyanTelemetry,
                    trailing = {
                        StatusPill(
                            text = "$liveFps FPS LIVE",
                            color = if (liveFps >= 55) OptimalGreen else ThermalAmber,
                        )
                    },
                )
                Spacer(modifier = Modifier.height(6.dp))
                TacticalCardSurface {
                    val avgMs = if (frameTimeHistoryMs.isNotEmpty()) {
                        frameTimeHistoryMs.average().toFloat()
                    } else {
                        1000f / telemetry.currentRefreshRateHz.coerceAtLeast(60)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = "${"%.1f".format(avgMs)} ms frame time",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                            )
                            Text(
                                text = "Display ${telemetry.currentRefreshRateHz}Hz (Peak ${telemetry.maxSupportedRefreshRateHz}Hz)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                            )
                        }
                        Text(
                            text = "Target <${"%.1f".format(1000f / telemetry.currentRefreshRateHz.coerceAtLeast(60))}ms",
                            style = MaterialTheme.typography.labelSmall,
                            color = OptimalGreen,
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    FramePacingSparkline(
                        frameTimesMs = frameTimeHistoryMs,
                        targetFrameMs = 1000f / telemetry.currentRefreshRateHz.coerceAtLeast(60),
                    )
                }
            }
        }

        item {
            ResponsiveScreenContainer {
                TacticalSectionHeader(
                    title = stringResource(R.string.section_live_telemetry),
                    icon = Icons.Rounded.Memory,
                    accentColor = ApexOrange,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        TelemetryTile(
                            icon = Icons.Rounded.Memory,
                            label = "FREE RAM",
                            value = "${telemetry.availableRamMb} MB",
                            subtext = "of ${telemetry.totalRamMb} MB (${100 - (telemetry.usedRamPercent * 100).roundToInt()}% free)",
                            accent = if (telemetry.lowMemory) CriticalRed else CyanTelemetry,
                            progress = 1f - telemetry.usedRamPercent,
                            modifier = Modifier.weight(1f),
                        )
                        val thermalColor = when (telemetry.thermalLevel) {
                            ThermalLevel.COOL -> OptimalGreen
                            ThermalLevel.WARM -> ThermalAmber
                            ThermalLevel.HOT -> CriticalRed
                        }
                        TelemetryTile(
                            icon = if (telemetry.isCharging) Icons.Rounded.BatteryChargingFull else Icons.Rounded.Thermostat,
                            label = "THERMAL & BATTERY",
                            value = "${"%.1f".format(telemetry.batteryTempC)}°C • ${telemetry.batteryPercent}%",
                            subtext = "${telemetry.thermalLevel.label} ${if (telemetry.isCharging) "• Charging" else ""}",
                            accent = thermalColor,
                            progress = telemetry.batteryPercent / 100f,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        TelemetryTile(
                            icon = Icons.Rounded.Tv,
                            label = "DISPLAY & DPI",
                            value = "${telemetry.currentRefreshRateHz} Hz • ${telemetry.smallestWidthDp}dp",
                            subtext = "${telemetry.screenWidthPx}×${telemetry.screenHeightPx} • ${telemetry.densityDpi} DPI",
                            accent = ApexOrange,
                            modifier = Modifier.weight(1f),
                        )
                        TelemetryTile(
                            icon = if (telemetry.network.isConnected) Icons.Rounded.Wifi else Icons.Rounded.Storage,
                            label = "LINK & STORAGE",
                            value = telemetry.network.transportLabel,
                            subtext = "${"%.1f".format(telemetry.freeStorageGb)} GB free / ${"%.0f".format(telemetry.totalStorageGb)} GB",
                            accent = if (telemetry.network.isConnected) OptimalGreen else ThermalAmber,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        item {
            ResponsiveScreenContainer {
                TacticalSectionHeader(
                    title = stringResource(R.string.section_recommended_fps),
                    icon = Icons.Rounded.RocketLaunch,
                    accentColor = OptimalGreen,
                    trailing = {
                        TextButton(
                            onClick = onNavigateToDevices,
                            modifier = Modifier.testTag("view_device_sens_button"),
                        ) {
                            Text("Sensitivity (${detectedProfile.baseSensitivity.general})")
                        }
                    },
                )
                Spacer(modifier = Modifier.height(6.dp))
                TacticalCardSurface(borderColor = OptimalGreen.copy(alpha = 0.35f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = detectedProfile.deviceName,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = "${telemetry.cpuCores} Cores • ${telemetry.socLabel}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                            )
                        }
                        StatusPill(
                            text = "${detectedProfile.fpsPreset.targetFps} FPS TARGET",
                            color = OptimalGreen,
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    FpsPresetSummaryGrid(fps = detectedProfile.fpsPreset)
                }
            }
        }

        item {
            ResponsiveScreenContainer {
                TacticalSectionHeader(
                    title = stringResource(R.string.section_system_checks),
                    icon = Icons.Rounded.Settings,
                    accentColor = ThermalAmber,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    telemetry.readinessChecks.forEach { check ->
                        ReadinessCheckCard(
                            check = check,
                            onOpenSettings = onOpenSettings,
                        )
                    }
                }
            }
        }

        item {
            ResponsiveScreenContainer {
                GameLauncherCard(
                    telemetry = telemetry,
                    forceMaxRefreshRate = forceMaxRefreshRate,
                    adaptiveBatteryPause = adaptiveBatteryPause,
                    onLaunchGame = onLaunchGame,
                    onTestPreLaunchBoost = onOptimizeClick,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun HeroPerformanceBanner(
    telemetry: SystemTelemetry,
    boostState: BoostState,
    onOptimizeClick: () -> Unit,
    onActivateAllFeatures: () -> Unit,
) {
    val pulseTransition = rememberInfiniteTransition(label = "boostPulse")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseAlpha",
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = TacticalCard,
        border = BorderStroke(1.dp, ApexOrange.copy(alpha = 0.55f)),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.bg_hero_thunder_banner),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .clip(MaterialTheme.shapes.extraLarge),
                alpha = 0.55f,
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                ObsidianBg.copy(alpha = 0.92f),
                                ObsidianBg.copy(alpha = 0.68f),
                                ObsidianBg.copy(alpha = 0.50f),
                            ),
                        ),
                    ),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.weight(1f).padding(end = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Surface(
                            modifier = Modifier.size(50.dp),
                            shape = CircleShape,
                            color = ObsidianBg,
                            border = BorderStroke(1.5.dp, ApexOrange),
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_apex_crosshair_logo),
                                contentDescription = "Zenitsu_2.0 Thunder Logo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.hero_subtitle),
                                style = MaterialTheme.typography.labelSmall,
                                color = ApexOrange,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "${telemetry.manufacturer} ${telemetry.model}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    // Readiness Score Ring
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(ObsidianBg.copy(alpha = 0.8f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        val score = telemetry.performanceScore
                        val scoreColor = when {
                            score >= 78 -> OptimalGreen
                            score >= 55 -> ThermalAmber
                            else -> CriticalRed
                        }
                        CircularProgressIndicator(
                            progress = { score / 100f },
                            modifier = Modifier.size(60.dp),
                            color = scoreColor,
                            trackColor = BorderSubtle,
                            strokeWidth = 5.dp,
                            strokeCap = StrokeCap.Round,
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$score",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "SCORE",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Live Memory Management Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MiniSpecChip(
                        label = "JVM HEAP",
                        value = "${"%.1f".format(telemetry.jvmHeapUsedMb)} MB",
                        color = CyanTelemetry,
                        modifier = Modifier.weight(1f),
                    )
                    MiniSpecChip(
                        label = "NATIVE HEAP",
                        value = "${"%.1f".format(telemetry.nativeHeapAllocatedMb)} MB",
                        color = OptimalGreen,
                        modifier = Modifier.weight(1f),
                    )
                    MiniSpecChip(
                        label = "CACHE POOL",
                        value = "${telemetry.appCacheSizeKb} KB",
                        color = ApexOrange,
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                val isRunning = boostState is BoostState.Optimizing
                Button(
                    onClick = onOptimizeClick,
                    enabled = !isRunning,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("optimize_system_button"),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ApexOrange,
                        contentColor = ThunderInk,
                        disabledContainerColor = ApexOrange.copy(alpha = 0.55f),
                        disabledContentColor = ThunderInk,
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = Color.White.copy(alpha = if (isRunning) pulseAlpha else 0.35f),
                    ),
                ) {
                    if (isRunning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = ThunderInk,
                            strokeWidth = 2.5.dp,
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = boostState.statusStep,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Bolt,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (boostState is BoostState.Completed) {
                                "RE-RUN PERFORMANCE BOOSTER"
                            } else {
                                stringResource(R.string.boost_action_run)
                            },
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onActivateAllFeatures,
                    enabled = !isRunning,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("activate_all_features_button"),
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, CyanTelemetry.copy(alpha = 0.65f)),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.RocketLaunch,
                        contentDescription = null,
                        tint = CyanTelemetry,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ACTIVATE ALL ENGINE FEATURES & SCRIBD",
                        style = MaterialTheme.typography.labelLarge,
                        color = CyanTelemetry,
                    )
                }

                if (boostState is BoostState.Optimizing) {
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { boostState.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(999.dp)),
                        color = CyanTelemetry,
                        trackColor = TacticalCardBright,
                    )
                    if (boostState.completedSteps.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            boostState.completedSteps.forEach { stepLabel ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = OptimalGreen,
                                        modifier = Modifier.size(13.dp),
                                    )
                                    Text(
                                        text = stepLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BoostActionResultBanner(boostState: BoostState) {
    AnimatedVisibility(visible = boostState is BoostState.Completed) {
        if (boostState is BoostState.Completed) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("boost_report_card"),
                shape = MaterialTheme.shapes.large,
                color = OptimalGreen.copy(alpha = 0.10f),
                border = BorderStroke(1.dp, OptimalGreen.copy(alpha = 0.5f)),
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = OptimalGreen,
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "⚡ THUNDER BOOSTER COMPLETE • ${boostState.availableRamAfterMb} MB FREE",
                                style = MaterialTheme.typography.labelLarge,
                                color = OptimalGreen,
                            )
                            Text(
                                text = boostState.summaryNote,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MiniSpecChip(
                            label = "RAM DELTA",
                            value = "+${boostState.freedRamMb} MB",
                            color = OptimalGreen,
                            modifier = Modifier.weight(1f),
                        )
                        MiniSpecChip(
                            label = "HEAP FREED",
                            value = "${boostState.jvmHeapFreedKb} KB",
                            color = CyanTelemetry,
                            modifier = Modifier.weight(1f),
                        )
                        MiniSpecChip(
                            label = "CACHE PURGED",
                            value = "${boostState.cacheClearedKb} KB",
                            color = ApexOrange,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (boostState.completedSteps.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            boostState.completedSteps.forEach { step ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = OptimalGreen,
                                        modifier = Modifier.size(14.dp),
                                    )
                                    Text(
                                        text = step,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TelemetryTile(
    icon: ImageVector,
    label: String,
    value: String,
    subtext: String,
    accent: Color,
    modifier: Modifier = Modifier,
    progress: Float? = null,
) {
    TacticalCardSurface(
        modifier = modifier,
        borderColor = accent.copy(alpha = 0.3f),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtext,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (progress != null) {
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = accent,
                trackColor = TacticalCardBright,
            )
        }
    }
}

@Composable
private fun ReadinessCheckCard(
    check: ReadinessCheck,
    onOpenSettings: (SettingsDestination) -> Unit,
) {
    val statusColor = when (check.status) {
        ReadinessStatus.OPTIMAL -> OptimalGreen
        ReadinessStatus.ACTION_NEEDED -> ThermalAmber
        ReadinessStatus.WARNING -> CriticalRed
    }
    val statusIcon = when (check.status) {
        ReadinessStatus.OPTIMAL -> Icons.Rounded.CheckCircle
        ReadinessStatus.ACTION_NEEDED, ReadinessStatus.WARNING -> Icons.Rounded.Warning
    }

    TacticalCardSurface(borderColor = statusColor.copy(alpha = 0.32f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = statusIcon,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(20.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = check.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = check.detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
                if (check.actionLabel != null && check.destination != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { onOpenSettings(check.destination) },
                        modifier = Modifier.testTag("settings_${check.id}"),
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.6f)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = check.actionLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = statusColor,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GameModeControlsCard(
    telemetry: SystemTelemetry,
    forceMaxRefreshRate: Boolean,
    adaptiveBatteryPause: Boolean,
    floatingFpsOverlayEnabled: Boolean,
    thunderSoundEnabled: Boolean,
    onToggleForceMaxRefreshRate: (Boolean) -> Unit,
    onToggleAdaptiveBatteryPause: (Boolean) -> Unit,
    onToggleFloatingFpsOverlay: (Boolean) -> Unit,
    onToggleThunderSound: (Boolean) -> Unit,
    onPreviewThunderSound: () -> Unit,
    onOpenSettings: (SettingsDestination) -> Unit,
) {
    TacticalSectionHeader(
        title = "GAME MODE ENGINE CONTROLS",
        icon = Icons.Rounded.RocketLaunch,
        accentColor = ApexOrange,
        trailing = {
            StatusPill(
                text = if (forceMaxRefreshRate) "${telemetry.maxSupportedRefreshRateHz}Hz LOCKED" else "${telemetry.currentRefreshRateHz}Hz AUTO",
                color = if (forceMaxRefreshRate) OptimalGreen else CyanTelemetry,
            )
        },
    )
    Spacer(modifier = Modifier.height(6.dp))
    TacticalCardSurface(borderColor = CyanTelemetry.copy(alpha = 0.35f)) {
        // 1. Force Max Refresh Rate Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(
                    text = "Force Max Refresh Rate (${telemetry.maxSupportedRefreshRateHz}Hz)",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (forceMaxRefreshRate) {
                        "Window locked to ${telemetry.maxSupportedRefreshRateHz}Hz peak display mode. Verify Smooth Display is ON in system settings for Free Fire."
                    } else {
                        "Lock display mode to peak ${telemetry.maxSupportedRefreshRateHz}Hz for smoother drag-headshot tracking."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }
            Switch(
                checked = forceMaxRefreshRate,
                onCheckedChange = onToggleForceMaxRefreshRate,
                modifier = Modifier.testTag("toggle_force_max_hz"),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = TextPrimary,
                    checkedTrackColor = ApexOrange,
                    uncheckedThumbColor = TextSecondary,
                    uncheckedTrackColor = TacticalCardBright,
                ),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = { onOpenSettings(SettingsDestination.DISPLAY) },
            modifier = Modifier.testTag("open_display_hz_settings"),
            border = BorderStroke(1.dp, CyanTelemetry.copy(alpha = 0.45f)),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text(
                text = "System Display Refresh Settings",
                style = MaterialTheme.typography.labelLarge,
                color = CyanTelemetry,
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                contentDescription = null,
                tint = CyanTelemetry,
                modifier = Modifier.size(15.dp),
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = BorderSubtle)
        Spacer(modifier = Modifier.height(14.dp))

        // 2. Adaptive Battery Optimization & Auto-Pause Background Apps
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Adaptive Battery & Background Pause",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (adaptiveBatteryPause) {
                        "Armed: When Free Fire starts, cached background apps are paused via ActivityManager and memory is trimmed."
                    } else {
                        "Enable to pause cached background apps automatically when launching Free Fire."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }
            Switch(
                checked = adaptiveBatteryPause,
                onCheckedChange = onToggleAdaptiveBatteryPause,
                modifier = Modifier.testTag("toggle_adaptive_battery_pause"),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = TextPrimary,
                    checkedTrackColor = OptimalGreen,
                    uncheckedThumbColor = TextSecondary,
                    uncheckedTrackColor = TacticalCardBright,
                ),
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusPill(
                text = if (telemetry.isIgnoringBatteryOptimizations) "UNRESTRICTED POWER" else "ADAPTIVE BATTERY",
                color = if (telemetry.isIgnoringBatteryOptimizations) OptimalGreen else ThermalAmber,
            )
            if (telemetry.isBackgroundRestricted) {
                StatusPill(
                    text = "BG RESTRICTED",
                    color = CriticalRed,
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = { onOpenSettings(SettingsDestination.BATTERY_OPTIMIZATION) },
            modifier = Modifier.testTag("open_adaptive_battery_settings"),
            border = BorderStroke(1.dp, OptimalGreen.copy(alpha = 0.45f)),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text(
                text = "Configure Adaptive Battery",
                style = MaterialTheme.typography.labelLarge,
                color = OptimalGreen,
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                contentDescription = null,
                tint = OptimalGreen,
                modifier = Modifier.size(15.dp),
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = BorderSubtle)
        Spacer(modifier = Modifier.height(14.dp))

        // 3. Real-Time Floating FPS Counter Overlay
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(
                    text = "Floating FPS Counter Overlay",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (floatingFpsOverlayEnabled) {
                        "Active: Draggable real-time FPS, frame-time (ms), and ${telemetry.currentRefreshRateHz}Hz overlay stays pinned inside the booster."
                    } else {
                        "Show a movable real-time FPS pill across all booster screens."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }
            Switch(
                checked = floatingFpsOverlayEnabled,
                onCheckedChange = onToggleFloatingFpsOverlay,
                modifier = Modifier.testTag("toggle_floating_fps_overlay"),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = TextPrimary,
                    checkedTrackColor = CyanTelemetry,
                    uncheckedThumbColor = TextSecondary,
                    uncheckedTrackColor = TacticalCardBright,
                ),
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = BorderSubtle)
        Spacer(modifier = Modifier.height(14.dp))

        // 4. Thunder Boost Activation Sound FX
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(
                    text = "Thunder ⚡ Boost Activation SFX",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (thunderSoundEnabled) {
                        "Plays an electric lightning crackle & deep thunderclap whenever Boost Mode activates."
                    } else {
                        "Muted: Enable to hear the thunderclap surge when Boost Mode activates."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }
            Switch(
                checked = thunderSoundEnabled,
                onCheckedChange = onToggleThunderSound,
                modifier = Modifier.testTag("toggle_thunder_sound"),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = TextPrimary,
                    checkedTrackColor = ApexOrange,
                    uncheckedThumbColor = TextSecondary,
                    uncheckedTrackColor = TacticalCardBright,
                ),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onPreviewThunderSound,
            modifier = Modifier.testTag("preview_thunder_sound_button"),
            border = BorderStroke(1.dp, ApexOrange.copy(alpha = 0.55f)),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                contentDescription = null,
                tint = ApexOrange,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Preview Thunder ⚡ Sound",
                style = MaterialTheme.typography.labelLarge,
                color = ApexOrange,
            )
        }
    }
}

@Composable
private fun GameLauncherCard(
    telemetry: SystemTelemetry,
    forceMaxRefreshRate: Boolean,
    adaptiveBatteryPause: Boolean,
    onLaunchGame: (String) -> Unit,
    onTestPreLaunchBoost: () -> Unit,
) {
    TacticalCardSurface(borderColor = ApexOrange.copy(alpha = 0.4f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "DIRECT GAME LAUNCHER",
                style = MaterialTheme.typography.labelLarge,
                color = ApexOrange,
            )
            if (adaptiveBatteryPause) {
                StatusPill(
                    text = "AUTO-PAUSE ARMED",
                    color = OptimalGreen,
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        if (telemetry.installedGames.isEmpty()) {
            Text(
                text = stringResource(R.string.free_fire_not_installed),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Install Garena Free Fire or Free Fire MAX to launch directly with ${if (forceMaxRefreshRate) "${telemetry.maxSupportedRefreshRateHz}Hz lock & " else ""}background-app pause.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onTestPreLaunchBoost,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("test_prelaunch_pause_button"),
                border = BorderStroke(1.dp, ApexOrange.copy(alpha = 0.6f)),
            ) {
                Icon(Icons.Rounded.Bolt, contentDescription = null, tint = ApexOrange)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Run Pre-Launch Background Pause Now",
                    color = ApexOrange,
                )
            }
        } else {
            Text(
                text = if (adaptiveBatteryPause) {
                    "Launching will pause cached background apps, reclaim RAM, and start Free Fire:"
                } else {
                    "Optimize memory first, then launch straight into your match:"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                telemetry.installedGames.forEach { game ->
                    Button(
                        onClick = { onLaunchGame(game.packageName) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("launch_${game.packageName}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ApexOrange,
                            contentColor = TextPrimary,
                        ),
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(game.displayName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}
