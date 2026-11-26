package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.DeviceSensitivityProfile
import com.example.model.PlatformBrand
import com.example.model.PlayStyleMode
import com.example.ui.components.FpsPresetSummaryGrid
import com.example.ui.components.ResponsiveScreenContainer
import com.example.ui.components.SensitivityGridPanel
import com.example.ui.components.StatusPill
import com.example.ui.components.TacticalCardSurface
import com.example.ui.components.formatSensitivityShareText
import com.example.ui.theme.ApexOrange
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CyanTelemetry
import com.example.ui.theme.OptimalGreen
import com.example.ui.theme.TacticalCard
import com.example.ui.theme.TacticalCardBright
import com.example.ui.theme.TextMuted
import com.example.ui.theme.ThunderInk
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DevicesSensitivityScreen(
    devices: List<DeviceSensitivityProfile>,
    searchQuery: String,
    selectedBrand: PlatformBrand,
    selectedPlayStyle: PlayStyleMode,
    favoriteIds: Set<String>,
    onSearchChange: (String) -> Unit,
    onSelectBrand: (PlatformBrand) -> Unit,
    onSelectPlayStyle: (PlayStyleMode) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onApplyPreset: (DeviceSensitivityProfile, PlayStyleMode) -> Unit,
    onPresetCopied: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expandedDeviceId by remember { mutableStateOf<String?>("detected_current_device") }
    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current

    Column(modifier = modifier.fillMaxSize()) {
        // Sticky Search + Brand + Playstyle Controls
        ResponsiveScreenContainer(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("device_search_input"),
                placeholder = {
                    Text(
                        text = stringResource(R.string.devices_search_hint),
                        color = TextMuted,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = CyanTelemetry,
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(
                                imageVector = Icons.Rounded.Clear,
                                contentDescription = "Clear search",
                                tint = TextSecondary,
                            )
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanTelemetry,
                    unfocusedBorderColor = BorderSubtle,
                    focusedContainerColor = TacticalCard,
                    unfocusedContainerColor = TacticalCard,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                ),
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Platform / Brand Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(PlatformBrand.entries) { brand ->
                    val selected = brand == selectedBrand
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectBrand(brand) },
                        label = {
                            Text(
                                text = brand.label,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        },
                        modifier = Modifier.testTag("brand_chip_${brand.name}"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ApexOrange,
                            selectedLabelColor = TextPrimary,
                            containerColor = TacticalCard,
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

            Spacer(modifier = Modifier.height(8.dp))

            // Playstyle Mode Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Tune,
                    contentDescription = null,
                    tint = CyanTelemetry,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "PLAYSTYLE:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(PlayStyleMode.entries) { mode ->
                        val selected = mode == selectedPlayStyle
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = if (selected) CyanTelemetry.copy(alpha = 0.18f) else TacticalCard,
                            border = BorderStroke(
                                1.dp,
                                if (selected) CyanTelemetry else BorderSubtle,
                            ),
                            modifier = Modifier
                                .testTag("playstyle_${mode.name}")
                                .clickable { onSelectPlayStyle(mode) },
                        ) {
                            Text(
                                text = mode.shortLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) CyanTelemetry else TextSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            )
                        }
                    }
                }
            }
        }

        // Device List
        if (devices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.Devices,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(48.dp),
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No matching device models",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try searching another phone, tablet, iPhone, or select All platforms.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(devices, key = { it.id }) { device ->
                    val isExpanded = expandedDeviceId == device.id
                    val isFavorite = favoriteIds.contains(device.id)
                    ResponsiveScreenContainer {
                        DevicePresetCard(
                            device = device,
                            playStyle = selectedPlayStyle,
                            isExpanded = isExpanded,
                            isFavorite = isFavorite,
                            onToggleExpand = {
                                expandedDeviceId = if (isExpanded) null else device.id
                            },
                            onToggleFavorite = { onToggleFavorite(device.id) },
                            onApplyPreset = { onApplyPreset(device, selectedPlayStyle) },
                            onCopyPreset = {
                                val tuned = device.baseSensitivity.tunedFor(selectedPlayStyle)
                                val text = formatSensitivityShareText(
                                    deviceName = device.deviceName,
                                    playStyleLabel = selectedPlayStyle.label,
                                    sens = tuned,
                                    fps = device.fpsPreset,
                                )
                                clipboard.setText(AnnotatedString(text))
                                onPresetCopied()
                            },
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun DevicePresetCard(
    device: DeviceSensitivityProfile,
    playStyle: PlayStyleMode,
    isExpanded: Boolean,
    isFavorite: Boolean,
    onToggleExpand: () -> Unit,
    onToggleFavorite: () -> Unit,
    onApplyPreset: () -> Unit,
    onCopyPreset: () -> Unit,
) {
    val tuned = remember(device, playStyle) {
        device.baseSensitivity.tunedFor(playStyle)
    }

    TacticalCardSurface(
        borderColor = when {
            device.isDetectedCurrentDevice -> ApexOrange.copy(alpha = 0.65f)
            isExpanded -> CyanTelemetry.copy(alpha = 0.55f)
            else -> BorderSubtle
        },
        modifier = Modifier
            .testTag("device_card_${device.id}")
            .clickable { onToggleExpand() },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (device.isDetectedCurrentDevice) {
                        StatusPill(
                            text = stringResource(R.string.devices_detected_badge),
                            color = ApexOrange,
                        )
                    }
                    StatusPill(
                        text = "${device.displayHz}Hz • ${device.fpsPreset.targetFps} FPS",
                        color = OptimalGreen,
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = device.deviceName,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${device.platformBadge} • ${device.ramTier}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.testTag("fav_${device.id}"),
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Rounded.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = if (isFavorite) "Remove saved preset" else "Save preset",
                        tint = if (isFavorite) ApexOrange else TextSecondary,
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = TextSecondary,
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Compact quick-glance badges even when collapsed
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            QuickStatPill("GEN", "${tuned.general}", ApexOrange, Modifier.weight(1f))
            QuickStatPill("RED DOT", "${tuned.redDot}", ApexOrange, Modifier.weight(1f))
            QuickStatPill("2X", "${tuned.scope2x}", CyanTelemetry, Modifier.weight(1f))
            QuickStatPill("FIRE", "${tuned.fireButtonPercent}%", OptimalGreen, Modifier.weight(1f))
        }

        AnimatedVisibility(visible = isExpanded) {
            Column {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = BorderSubtle)
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "${playStyle.label.uppercase()} PRESET (0–200 SCALE)",
                    style = MaterialTheme.typography.labelLarge,
                    color = CyanTelemetry,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = device.screenSpec,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                )
                Spacer(modifier = Modifier.height(12.dp))

                SensitivityGridPanel(sensitivity = tuned)

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "OPTIMAL FPS & GRAPHICS",
                    style = MaterialTheme.typography.labelLarge,
                    color = OptimalGreen,
                )
                Spacer(modifier = Modifier.height(8.dp))
                FpsPresetSummaryGrid(fps = device.fpsPreset)

                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = TacticalCardBright,
                    border = BorderStroke(1.dp, ApexOrange.copy(alpha = 0.35f)),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "Pro Drag Tip: ${device.dragTip}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                        )
                        if (device.preferredTouchSetting.isNotBlank()) {
                            Text(
                                text = "Device Preference: ${device.preferredTouchSetting}",
                                style = MaterialTheme.typography.labelMedium,
                                color = CyanTelemetry,
                            )
                        }
                        if (device.dragTechnique.isNotBlank()) {
                            Text(
                                text = "Drag Headshot Technique: ${device.dragTechnique}",
                                style = MaterialTheme.typography.labelMedium,
                                color = OptimalGreen,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Button(
                        onClick = onApplyPreset,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("apply_${device.id}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OptimalGreen,
                            contentColor = androidx.compose.ui.graphics.Color(0xFF07130D),
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Apply Scribd", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onCopyPreset,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("copy_${device.id}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ApexOrange,
                            contentColor = ThunderInk,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.copy_preset))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_btn_${device.id}"),
                    border = BorderStroke(1.dp, BorderSubtle),
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Rounded.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = null,
                        tint = if (isFavorite) ApexOrange else TextPrimary,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(if (isFavorite) R.string.saved_favorite else R.string.save_favorite),
                        color = TextPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickStatPill(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = TacticalCardBright,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = color,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
