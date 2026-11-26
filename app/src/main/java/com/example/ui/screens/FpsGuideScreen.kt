package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.CheckBoxOutlineBlank
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.DpiCalculationResult
import com.example.model.FpsGraphicsPreset
import com.example.model.SavedCalibratedProfile
import com.example.ui.components.MiniSpecChip
import com.example.ui.components.ResponsiveScreenContainer
import com.example.ui.components.SensitivityGridPanel
import com.example.ui.components.StatusPill
import com.example.ui.components.TacticalCardSurface
import com.example.ui.components.TacticalSectionHeader
import com.example.ui.components.formatSensitivityShareText
import com.example.ui.theme.ApexOrange
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CyanTelemetry
import com.example.ui.theme.OptimalGreen
import com.example.ui.theme.TacticalCard
import com.example.ui.theme.TacticalCardBright
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.ThunderInk
import kotlin.math.roundToInt

private data class ProChecklistItem(
    val id: String,
    val title: String,
    val detail: String,
)

private val proChecklist = listOf(
    ProChecklistItem(
        id = "aim_precision",
        title = "Set Aim Precision to Default",
        detail = "Default Aim Assist helps initial chest lock before your upward drag snaps to the head.",
    ),
    ProChecklistItem(
        id = "fire_position",
        title = "Lower Fire Button Position (Bottom-Right 25%)",
        detail = "Leaving upward swipe runway above the Fire Button prevents running out of screen space on close drags.",
    ),
    ProChecklistItem(
        id = "high_fps",
        title = "Lock In-Game FPS to High",
        detail = "Higher frame rate reduces input-to-photon lag; drop Graphics to Smooth/Standard before ever lowering FPS.",
    ),
    ProChecklistItem(
        id = "quick_weapon",
        title = "Enable Quick Weapon Switch & Left Fire Button",
        detail = "Faster M1887 / AWM switch cancels reload delay and enables instant gloo wall crouch combos.",
    ),
    ProChecklistItem(
        id = "thermal_prep",
        title = "Unplug Charger & Disable Battery Saver",
        detail = "Charging while playing spikes battery heat above 41°C and triggers SoC FPS throttling.",
    ),
)

@Composable
fun FpsGuideScreen(
    dpiCalculation: DpiCalculationResult,
    savedProfile: SavedCalibratedProfile?,
    completedChecklistIds: Set<String>,
    onUpdateDpiCalc: (targetDpi: Int, thumbSpeedLevel: Int) -> Unit,
    onToggleChecklist: (String) -> Unit,
    onNavigateToCalibrator: () -> Unit,
    onPresetCopied: () -> Unit,
    modifier: Modifier = Modifier,
) {
    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Saved Calibrated Profile Card
        item {
            ResponsiveScreenContainer {
                TacticalSectionHeader(
                    title = "MY CALIBRATED PROFILE",
                    icon = Icons.Rounded.Bookmark,
                    accentColor = OptimalGreen,
                )
                Spacer(modifier = Modifier.height(6.dp))
                if (savedProfile == null) {
                    TacticalCardSurface {
                        Text(
                            text = "No calibrated touch profile saved yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Run a 5-swipe session in the Touch Calibrator to auto-tune sensitivity to your thumb speed.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onNavigateToCalibrator,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("go_to_calibrator_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ApexOrange,
                                contentColor = ThunderInk,
                            ),
                        ) {
                            Text("Open Touch Calibrator")
                        }
                    }
                } else {
                    TacticalCardSurface(borderColor = OptimalGreen.copy(alpha = 0.5f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Saved Personal Preset",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                            )
                            StatusPill(
                                text = "${savedProfile.headshotRatePercent}% HS • ${savedProfile.measuredTouchHz}Hz",
                                color = OptimalGreen,
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        SensitivityGridPanel(sensitivity = savedProfile.sensitivity)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                val text = formatSensitivityShareText(
                                    deviceName = "My Calibrated Device",
                                    playStyleLabel = "Touch Calibrated",
                                    sens = savedProfile.sensitivity,
                                    fps = FpsGraphicsPreset(
                                        "Standard/Smooth",
                                        "High",
                                        dpiCalculation.recommendedFpsTarget,
                                        false,
                                        "High",
                                        "Bright",
                                        false,
                                        "",
                                    ),
                                )
                                clipboard.setText(AnnotatedString(text))
                                onPresetCopied()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OptimalGreen,
                                contentColor = TacticalCard,
                            ),
                        ) {
                            Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Copy Calibrated Preset")
                        }
                    }
                }
            }
        }

        // Interactive DPI & Sensitivity Scaler
        item {
            ResponsiveScreenContainer {
                TacticalSectionHeader(
                    title = stringResource(R.string.guide_dpi_calc),
                    icon = Icons.Rounded.Calculate,
                    accentColor = CyanTelemetry,
                )
                Spacer(modifier = Modifier.height(6.dp))
                TacticalCardSurface(borderColor = CyanTelemetry.copy(alpha = 0.4f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = "Current Smallest Width: ${dpiCalculation.currentDpi} dp",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                            )
                            Text(
                                text = "Target DPI: ${dpiCalculation.targetDpi} dp",
                                style = MaterialTheme.typography.titleMedium,
                                color = CyanTelemetry,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        StatusPill(
                            text = "${dpiCalculation.displayHz}Hz PANEL",
                            color = ApexOrange,
                        )
                    }

                    Slider(
                        value = dpiCalculation.targetDpi.toFloat(),
                        onValueChange = { newVal ->
                            onUpdateDpiCalc(newVal.roundToInt(), dpiCalculation.thumbSpeedLevel)
                        },
                        valueRange = 360f..760f,
                        modifier = Modifier.testTag("dpi_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = CyanTelemetry,
                            activeTrackColor = CyanTelemetry,
                            inactiveTrackColor = TacticalCardBright,
                        ),
                    )

                    Text(
                        text = "THUMB DRAG SPEED",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf(1 to "Controlled", 2 to "Balanced", 3 to "Fast Flick").forEach { (level, label) ->
                            val selected = dpiCalculation.thumbSpeedLevel == level
                            FilterChip(
                                selected = selected,
                                onClick = { onUpdateDpiCalc(dpiCalculation.targetDpi, level) },
                                label = { Text(label, style = MaterialTheme.typography.labelLarge) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ApexOrange,
                                    selectedLabelColor = TextPrimary,
                                    containerColor = TacticalCardBright,
                                    labelColor = TextSecondary,
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selected,
                                    borderColor = BorderSubtle,
                                    selectedBorderColor = ApexOrange,
                                ),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = BorderSubtle)
                    Spacer(modifier = Modifier.height(12.dp))

                    SensitivityGridPanel(sensitivity = dpiCalculation.sensitivity)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = dpiCalculation.coachNote,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }
            }
        }

        // Drag Headshot Technique Matrix (Scribd Field Manual)
        item {
            ResponsiveScreenContainer {
                TacticalSectionHeader(
                    title = "DRAG HEADSHOT & FPS FIELD MANUAL",
                    icon = Icons.AutoMirrored.Rounded.MenuBook,
                    accentColor = ApexOrange,
                )
                Spacer(modifier = Modifier.height(6.dp))
                TacticalCardSurface {
                    TechniqueBlock(
                        title = "1. Straight Vertical Drag (Mid & Long Range)",
                        body = "Place white crosshair slightly below neck or chest level. Pull straight upward smoothly for 1.5–2.2 cm. Ideal for Woodpecker, AC80, SCAR, and M4A1.",
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    TechniqueBlock(
                        title = "2. J-Drag / Rotation Drag (Close 1v1 Shotguns)",
                        body = "When enemy strafes left or right at <10m, dip the Fire Button slightly toward their shoulder side then whip up in a crisp J-curve. Best with M1887 & Desert Eagle.",
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    TechniqueBlock(
                        title = "3. FPS Priority Rule (Frame Rate > Shadows)",
                        body = "Always set High FPS = High first. If your phone drops below 50 FPS in Squad fights, lower Graphics from Ultra to Standard or Smooth and keep Shadows OFF.",
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MiniSpecChip("LOW RAM (2-4GB)", "Smooth + High FPS", ThermalThumbColor(), Modifier.weight(1f))
                        MiniSpecChip("MID-HIGH (6-16GB)", "Std/Ultra + High FPS", OptimalGreen, Modifier.weight(1f))
                    }
                }
            }
        }

        // Pre-Match Pro Checklist
        item {
            ResponsiveScreenContainer {
                TacticalSectionHeader(
                    title = stringResource(R.string.guide_checklist),
                    icon = Icons.Rounded.Speed,
                    accentColor = OptimalGreen,
                    trailing = {
                        StatusPill(
                            text = "${completedChecklistIds.size}/${proChecklist.size} READY",
                            color = if (completedChecklistIds.size == proChecklist.size) OptimalGreen else CyanTelemetry,
                        )
                    },
                )
                Spacer(modifier = Modifier.height(6.dp))
                TacticalCardSurface {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        proChecklist.forEachIndexed { index, item ->
                            val checked = completedChecklistIds.contains(item.id)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("checklist_${item.id}")
                                    .clickable { onToggleChecklist(item.id) }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Icon(
                                    imageVector = if (checked) Icons.Rounded.CheckBox else Icons.Rounded.CheckBoxOutlineBlank,
                                    contentDescription = if (checked) "Checked" else "Unchecked",
                                    tint = if (checked) OptimalGreen else TextMuted,
                                    modifier = Modifier.padding(top = 2.dp),
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (checked) OptimalGreen else TextPrimary,
                                    )
                                    Text(
                                        text = item.detail,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary,
                                    )
                                }
                            }
                            if (index < proChecklist.lastIndex) {
                                HorizontalDivider(color = BorderSubtle.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun TechniqueBlock(title: String, body: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = TacticalCardBright,
        border = BorderStroke(1.dp, BorderSubtle),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = ApexOrange,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
    }
}

@Composable
private fun ThermalThumbColor() = ApexOrange
