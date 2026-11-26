package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.ActiveAppliedProfile
import com.example.model.DeviceSensitivityProfile
import com.example.model.DeviceSmoothSuggestion
import com.example.model.FpsGraphicsPreset
import com.example.model.SensitivityValues
import com.example.model.SmoothPlayPriority
import com.example.ui.theme.ApexOrange
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CyanTelemetry
import com.example.ui.theme.OptimalGreen
import com.example.ui.theme.TacticalCard
import com.example.ui.theme.TacticalCardBright
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun BestSmoothSuggestionsCard(
    suggestion: DeviceSmoothSuggestion,
    allDevices: List<DeviceSensitivityProfile>,
    onSelectPriority: (SmoothPlayPriority) -> Unit,
    onSelectDevice: (String?) -> Unit,
    onApplyPreset: (String, SensitivityValues, FpsGraphicsPreset) -> Unit,
    onPresetCopied: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDevicePicker by remember { mutableStateOf(false) }
    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current

    Column(modifier = modifier.fillMaxWidth()) {
        TacticalSectionHeader(
            title = "BEST SMOOTH GAMEPLAY & SCRIBD ADVISOR",
            icon = Icons.Rounded.AutoAwesome,
            accentColor = OptimalGreen,
            trailing = {
                StatusPill(
                    text = "${suggestion.recommendedFps.targetFps} FPS",
                    color = OptimalGreen,
                )
            },
        )
        Spacer(modifier = Modifier.height(6.dp))
        TacticalCardSurface(borderColor = OptimalGreen.copy(alpha = 0.4f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TARGET DEVICE PROFILE",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                    )
                    Text(
                        text = suggestion.targetDevice.deviceName,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${suggestion.targetDevice.displayHz}Hz • ${suggestion.targetDevice.ramTier}",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanTelemetry,
                    )
                }
                OutlinedButton(
                    onClick = { showDevicePicker = true },
                    modifier = Modifier.testTag("pick_suggestion_device_button"),
                    border = BorderStroke(1.dp, CyanTelemetry.copy(alpha = 0.55f)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Devices,
                        contentDescription = null,
                        tint = CyanTelemetry,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Change Device",
                        style = MaterialTheme.typography.labelMedium,
                        color = CyanTelemetry,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "SMOOTHNESS PRIORITY",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SmoothPlayPriority.entries) { priority ->
                    val selected = priority == suggestion.priority
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectPriority(priority) },
                        label = { Text(priority.label) },
                        modifier = Modifier.testTag("smooth_priority_${priority.name}"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OptimalGreen.copy(alpha = 0.22f),
                            selectedLabelColor = OptimalGreen,
                            containerColor = TacticalCardBright,
                            labelColor = TextSecondary,
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = BorderSubtle,
                            selectedBorderColor = OptimalGreen,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = MaterialTheme.shapes.small,
                color = TacticalCardBright,
                border = BorderStroke(1.dp, CyanTelemetry.copy(alpha = 0.3f)),
            ) {
                Text(
                    text = suggestion.hardwareNote,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    modifier = Modifier.padding(12.dp),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            FpsPresetSummaryGrid(fps = suggestion.recommendedFps)

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BorderSubtle)
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "SCRIBD SMOOTH SENSITIVITY (${suggestion.priority.label.uppercase()})",
                style = MaterialTheme.typography.labelLarge,
                color = ApexOrange,
            )
            Spacer(modifier = Modifier.height(8.dp))
            SensitivityGridPanel(sensitivity = suggestion.recommendedSensitivity)

            if (suggestion.similarDevices.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "BEST SIMILAR DEVICES FOR SMOOTH PLAY",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(suggestion.similarDevices, key = { it.id }) { similar ->
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = TacticalCardBright,
                            border = BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier
                                .testTag("similar_device_${similar.id}")
                                .clickable { onSelectDevice(similar.id) },
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                                Text(
                                    text = similar.deviceName,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = "${similar.displayHz}Hz • ${similar.fpsPreset.targetFps} FPS • Gen ${similar.baseSensitivity.general}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OptimalGreen,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = {
                        onApplyPreset(
                            "${suggestion.targetDevice.deviceName} (${suggestion.priority.label})",
                            suggestion.recommendedSensitivity,
                            suggestion.recommendedFps,
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("apply_smooth_suggestion_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OptimalGreen,
                        contentColor = Color(0xFF07130D),
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Apply Scribd Calc",
                        fontWeight = FontWeight.Bold,
                    )
                }
                OutlinedButton(
                    onClick = {
                        val text = formatSensitivityShareText(
                            deviceName = suggestion.targetDevice.deviceName,
                            playStyleLabel = suggestion.priority.label,
                            sens = suggestion.recommendedSensitivity,
                            fps = suggestion.recommendedFps,
                        )
                        clipboard.setText(AnnotatedString(text))
                        onPresetCopied()
                    },
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("copy_smooth_suggestion_button"),
                    border = BorderStroke(1.dp, CyanTelemetry.copy(alpha = 0.5f)),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = "Copy suggestion",
                        tint = CyanTelemetry,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy", color = CyanTelemetry)
                }
            }
        }
    }

    if (showDevicePicker) {
        SuggestionDevicePickerDialog(
            allDevices = allDevices,
            selectedDeviceId = suggestion.targetDevice.id,
            onSelect = { deviceId ->
                onSelectDevice(deviceId)
                showDevicePicker = false
            },
            onDismiss = { showDevicePicker = false },
        )
    }
}

@Composable
private fun SuggestionDevicePickerDialog(
    allDevices: List<DeviceSensitivityProfile>,
    selectedDeviceId: String,
    onSelect: (String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(allDevices, query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) {
            allDevices
        } else {
            allDevices.filter {
                it.deviceName.lowercase().contains(q) ||
                    it.platformBadge.lowercase().contains(q) ||
                    it.brand.label.lowercase().contains(q)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TacticalCard,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        title = {
            Text(
                text = "Select Device for Smooth Suggestion",
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search Motorola, Samsung, iPhone…", color = TextMuted) },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = CyanTelemetry) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanTelemetry,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                    ),
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(filtered, key = { it.id }) { device ->
                        val isCurrent = device.id == selectedDeviceId
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = if (isCurrent) ApexOrange.copy(alpha = 0.16f) else TacticalCardBright,
                            border = BorderStroke(
                                1.dp,
                                if (isCurrent) ApexOrange else BorderSubtle,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelect(if (device.isDetectedCurrentDevice) null else device.id)
                                },
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = device.deviceName,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = "${device.platformBadge} • ${device.displayHz}Hz • ${device.ramTier}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                    )
                                }
                                Text(
                                    text = "${device.fpsPreset.targetFps} FPS",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = OptimalGreen,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = CyanTelemetry)
            }
        },
    )
}

@Composable
fun ActiveScribdProfileEditorCard(
    activeProfile: ActiveAppliedProfile,
    maxRefreshRateHz: Int,
    onUpdateSensitivity: (SensitivityValues) -> Unit,
    onUpdateFpsPreset: (FpsGraphicsPreset) -> Unit,
    onResetAuto: () -> Unit,
    onPresetCopied: () -> Unit,
    modifier: Modifier = Modifier,
) {
    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current

    var draftSens by remember(activeProfile) { mutableStateOf(activeProfile.sensitivity) }
    LaunchedEffect(activeProfile) {
        draftSens = activeProfile.sensitivity
    }

    Column(modifier = modifier.fillMaxWidth()) {
        TacticalSectionHeader(
            title = "ACTIVE SCRIBD PROFILE & PREFERENCES",
            icon = Icons.Rounded.Tune,
            accentColor = ApexOrange,
            trailing = {
                StatusPill(
                    text = if (activeProfile.isCustomized) "CUSTOM SCRIBD" else "SCRIBD AUTO",
                    color = if (activeProfile.isCustomized) CyanTelemetry else OptimalGreen,
                )
            },
        )
        Spacer(modifier = Modifier.height(6.dp))
        TacticalCardSurface(borderColor = ApexOrange.copy(alpha = 0.42f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ACTIVE PRESET SOURCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                    )
                    Text(
                        text = activeProfile.sourceName,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                TextButton(
                    onClick = onResetAuto,
                    modifier = Modifier.testTag("reset_active_scribd_button"),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.RestartAlt,
                        contentDescription = null,
                        tint = CyanTelemetry,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset Auto", color = CyanTelemetry)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BorderSubtle)
            Spacer(modifier = Modifier.height(10.dp))

            // Editable Sensitivity Sliders (0–200 Scribd Calculation Scale)
            EditableSensitivitySlider(
                label = "General Sensitivity",
                value = draftSens.general,
                valueRange = 80f..200f,
                accent = ApexOrange,
                onValueChange = { draftSens = draftSens.copy(general = it) },
                onCommit = { onUpdateSensitivity(draftSens) },
                testTag = "slider_general",
            )
            EditableSensitivitySlider(
                label = "Red Dot",
                value = draftSens.redDot,
                valueRange = 80f..200f,
                accent = ApexOrange,
                onValueChange = { draftSens = draftSens.copy(redDot = it) },
                onCommit = { onUpdateSensitivity(draftSens) },
                testTag = "slider_red_dot",
            )
            EditableSensitivitySlider(
                label = "2x Scope",
                value = draftSens.scope2x,
                valueRange = 70f..200f,
                accent = CyanTelemetry,
                onValueChange = { draftSens = draftSens.copy(scope2x = it) },
                onCommit = { onUpdateSensitivity(draftSens) },
                testTag = "slider_2x",
            )
            EditableSensitivitySlider(
                label = "4x Scope",
                value = draftSens.scope4x,
                valueRange = 60f..200f,
                accent = CyanTelemetry,
                onValueChange = { draftSens = draftSens.copy(scope4x = it) },
                onCommit = { onUpdateSensitivity(draftSens) },
                testTag = "slider_4x",
            )
            EditableSensitivitySlider(
                label = "Sniper Scope",
                value = draftSens.sniperScope,
                valueRange = 40f..195f,
                accent = OptimalGreen,
                onValueChange = { draftSens = draftSens.copy(sniperScope = it) },
                onCommit = { onUpdateSensitivity(draftSens) },
                testTag = "slider_sniper",
            )
            EditableSensitivitySlider(
                label = "Free Look",
                value = draftSens.freeLook,
                valueRange = 60f..200f,
                accent = OptimalGreen,
                onValueChange = { draftSens = draftSens.copy(freeLook = it) },
                onCommit = { onUpdateSensitivity(draftSens) },
                testTag = "slider_free_look",
            )
            EditableSensitivitySlider(
                label = "Fire Button Size",
                value = draftSens.fireButtonPercent,
                valueRange = 36f..72f,
                unitSuffix = "%",
                accent = ApexOrange,
                onValueChange = { draftSens = draftSens.copy(fireButtonPercent = it) },
                onCommit = { onUpdateSensitivity(draftSens) },
                testTag = "slider_fire_button",
            )
            EditableSensitivitySlider(
                label = "Target Developer DPI",
                value = draftSens.recommendedDpi,
                valueRange = 360f..760f,
                unitSuffix = " dp",
                accent = CyanTelemetry,
                onValueChange = { draftSens = draftSens.copy(recommendedDpi = it) },
                onCommit = { onUpdateSensitivity(draftSens) },
                testTag = "slider_target_dpi",
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BorderSubtle)
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "FPS & GRAPHICS PREFERENCES",
                style = MaterialTheme.typography.labelLarge,
                color = CyanTelemetry,
            )
            Spacer(modifier = Modifier.height(8.dp))

            val currentFps = activeProfile.fpsPreset
            Text("Graphics Quality", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("Smooth", "Standard", "Ultra").forEach { quality ->
                    val selected = currentFps.graphicsQuality.equals(quality, ignoreCase = true)
                    FilterChip(
                        selected = selected,
                        onClick = {
                            onUpdateFpsPreset(
                                currentFps.copy(
                                    graphicsQuality = quality,
                                    shadowEnabled = if (quality == "Smooth") false else currentFps.shadowEnabled,
                                ),
                            )
                        },
                        label = { Text(quality) },
                        modifier = Modifier.testTag("pref_graphics_$quality"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanTelemetry.copy(alpha = 0.2f),
                            selectedLabelColor = CyanTelemetry,
                            containerColor = TacticalCardBright,
                            labelColor = TextSecondary,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text("High FPS Mode & Shadows", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    "Normal" to 30,
                    "High" to maxRefreshRateHz.coerceAtMost(90).coerceAtLeast(60),
                ).forEach { (mode, fpsNum) ->
                    val selected = currentFps.fpsMode.equals(mode, ignoreCase = true)
                    FilterChip(
                        selected = selected,
                        onClick = {
                            onUpdateFpsPreset(currentFps.copy(fpsMode = mode, targetFps = fpsNum))
                        },
                        label = { Text("$mode (${fpsNum} FPS)") },
                        modifier = Modifier.testTag("pref_fps_$mode"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OptimalGreen.copy(alpha = 0.2f),
                            selectedLabelColor = OptimalGreen,
                            containerColor = TacticalCardBright,
                            labelColor = TextSecondary,
                        ),
                    )
                }
                FilterChip(
                    selected = currentFps.shadowEnabled,
                    onClick = {
                        onUpdateFpsPreset(currentFps.copy(shadowEnabled = !currentFps.shadowEnabled))
                    },
                    label = { Text(if (currentFps.shadowEnabled) "Shadows: On" else "Shadows: Off") },
                    modifier = Modifier.testTag("pref_shadows_toggle"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ApexOrange.copy(alpha = 0.2f),
                        selectedLabelColor = ApexOrange,
                        containerColor = TacticalCardBright,
                        labelColor = TextSecondary,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = {
                    val text = formatSensitivityShareText(
                        deviceName = activeProfile.sourceName,
                        playStyleLabel = if (activeProfile.isCustomized) "Custom Scribd" else "Scribd Auto",
                        sens = draftSens,
                        fps = currentFps,
                    )
                    clipboard.setText(AnnotatedString(text))
                    onPresetCopied()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("copy_active_scribd_profile_button"),
                border = BorderStroke(1.dp, ApexOrange.copy(alpha = 0.55f)),
            ) {
                Icon(
                    imageVector = Icons.Rounded.ContentCopy,
                    contentDescription = null,
                    tint = ApexOrange,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Copy Active Scribd Profile", color = ApexOrange)
            }
        }
    }
}

@Composable
private fun EditableSensitivitySlider(
    label: String,
    value: Int,
    valueRange: ClosedFloatingPointRange<Float>,
    accent: Color,
    onValueChange: (Int) -> Unit,
    onCommit: () -> Unit,
    testTag: String,
    unitSuffix: String = "",
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
            Text(
                text = "$value$unitSuffix",
                style = MaterialTheme.typography.labelLarge,
                color = accent,
                fontWeight = FontWeight.Bold,
            )
        }
        Slider(
            value = value.toFloat().coerceIn(valueRange.start, valueRange.endInclusive),
            onValueChange = { onValueChange(it.roundToInt()) },
            onValueChangeFinished = onCommit,
            valueRange = valueRange,
            modifier = Modifier.testTag(testTag),
            colors = SliderDefaults.colors(
                thumbColor = accent,
                activeTrackColor = accent,
                inactiveTrackColor = TacticalCardBright,
            ),
        )
    }
}
