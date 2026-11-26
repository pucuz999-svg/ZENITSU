package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.model.FpsGraphicsPreset
import com.example.model.SensitivityValues
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
import kotlin.math.roundToInt

@Composable
fun ResponsiveScreenContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 840.dp),
            content = content,
        )
    }
}

@Composable
fun TacticalSectionHeader(
    title: String,
    icon: ImageVector? = null,
    accentColor: Color = CyanTelemetry,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp),
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        trailing?.invoke()
    }
}

@Composable
fun TacticalCardSurface(
    modifier: Modifier = Modifier,
    borderColor: Color = BorderSubtle,
    containerColor: Color = TacticalCard,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content,
        )
    }
}

@Composable
fun StatusPill(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = color.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.45f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun SensitivityMeterRow(
    label: String,
    value: Int,
    maxValue: Int = 200,
    accentColor: Color = ApexOrange,
) {
    val fraction by animateFloatAsState(
        targetValue = (value.toFloat() / maxValue).coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 360f),
        label = "sensBar",
    )
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
                text = "$value",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(TacticalCardBright),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(7.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(accentColor, CyanTelemetry),
                        ),
                    ),
            )
        }
    }
}

@Composable
fun SensitivityGridPanel(
    sensitivity: SensitivityValues,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SensitivityMeterRow(label = "General", value = sensitivity.general, accentColor = ApexOrange)
        SensitivityMeterRow(label = "Red Dot", value = sensitivity.redDot, accentColor = ApexOrange)
        SensitivityMeterRow(label = "2x Scope", value = sensitivity.scope2x, accentColor = CyanTelemetry)
        SensitivityMeterRow(label = "4x Scope", value = sensitivity.scope4x, accentColor = CyanTelemetry)
        SensitivityMeterRow(label = "Sniper Scope", value = sensitivity.sniperScope, accentColor = OptimalGreen)
        SensitivityMeterRow(label = "Free Look", value = sensitivity.freeLook, accentColor = OptimalGreen)

        Spacer(modifier = Modifier.height(2.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MiniSpecChip(
                label = "FIRE BUTTON",
                value = "${sensitivity.fireButtonPercent}%",
                color = ApexOrange,
                modifier = Modifier.weight(1f),
            )
            MiniSpecChip(
                label = "TARGET DPI",
                value = "${sensitivity.recommendedDpi} dp",
                color = CyanTelemetry,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
fun MiniSpecChip(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = TacticalCardBright,
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun FpsPresetSummaryGrid(
    fps: FpsGraphicsPreset,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MiniSpecChip(
                label = "GRAPHICS",
                value = fps.graphicsQuality,
                color = CyanTelemetry,
                modifier = Modifier.weight(1f),
            )
            MiniSpecChip(
                label = "HIGH FPS",
                value = "${fps.fpsMode} (${fps.targetFps} FPS)",
                color = OptimalGreen,
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MiniSpecChip(
                label = "HIGH RES / SHADOW",
                value = "${fps.highResolution} / ${if (fps.shadowEnabled) "On" else "Off"}",
                color = TextSecondary,
                modifier = Modifier.weight(1f),
            )
            MiniSpecChip(
                label = "FILTER / SCALE",
                value = "${fps.filter} / ${if (fps.autoScale) "Auto" else "Fixed"}",
                color = TextSecondary,
                modifier = Modifier.weight(1f),
            )
        }
        Text(
            text = fps.rationale,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
    }
}

@Composable
fun FramePacingSparkline(
    frameTimesMs: List<Float>,
    targetFrameMs: Float,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp),
    ) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        // Target frame budget line (e.g., 16.6ms for 60Hz or 8.3ms for 120Hz)
        val maxScaleMs = 34f
        val budgetY = h - ((targetFrameMs / maxScaleMs).coerceIn(0.05f, 0.95f) * h)
        drawLine(
            color = OptimalGreen.copy(alpha = 0.45f),
            start = Offset(0f, budgetY),
            end = Offset(w, budgetY),
            strokeWidth = 2f,
        )

        if (frameTimesMs.size < 2) return@Canvas

        val stepX = w / (frameTimesMs.size - 1).coerceAtLeast(1)
        val path = Path()
        val fillPath = Path()

        frameTimesMs.forEachIndexed { idx, ms ->
            val x = idx * stepX
            val normalized = (ms / maxScaleMs).coerceIn(0.04f, 0.96f)
            val y = h - (normalized * h)
            if (idx == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, h)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
            if (idx == frameTimesMs.lastIndex) {
                fillPath.lineTo(x, h)
                fillPath.close()
            }
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    CyanTelemetry.copy(alpha = 0.28f),
                    Color.Transparent,
                ),
            ),
        )
        drawPath(
            path = path,
            color = CyanTelemetry,
            style = Stroke(width = 4f, cap = StrokeCap.Round),
        )
    }
}

fun formatSensitivityShareText(
    deviceName: String,
    playStyleLabel: String,
    sens: SensitivityValues,
    fps: FpsGraphicsPreset,
): String = buildString {
    appendLine("Zenitsu_2.0 Preset — $deviceName ($playStyleLabel)")
    appendLine("General: ${sens.general} | Red Dot: ${sens.redDot}")
    appendLine("2x Scope: ${sens.scope2x} | 4x Scope: ${sens.scope4x}")
    appendLine("Sniper: ${sens.sniperScope} | Free Look: ${sens.freeLook}")
    appendLine("Fire Button: ${sens.fireButtonPercent}% | Target DPI: ${sens.recommendedDpi}")
    append("FPS & Graphics: ${fps.graphicsQuality} / ${fps.fpsMode} (${fps.targetFps} FPS) • Shadow: ${if (fps.shadowEnabled) "On" else "Off"}")
}

@Composable
fun FloatingFpsOverlayPill(
    liveFps: Int,
    frameTimeMs: Float,
    refreshRateHz: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fpsColor = when {
        liveFps >= 55 -> OptimalGreen
        liveFps >= 40 -> ThermalAmber
        else -> CriticalRed
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val maxHeightPx = with(density) { maxHeight.toPx() }
        val pillWidthPx = with(density) { 188.dp.toPx() }
        val pillHeightPx = with(density) { 46.dp.toPx() }
        val padPx = with(density) { 12.dp.toPx() }

        var offsetX by remember(maxWidthPx) {
            mutableFloatStateOf((maxWidthPx - pillWidthPx - padPx).coerceAtLeast(padPx))
        }
        var offsetY by remember(maxHeightPx) {
            mutableFloatStateOf(with(density) { 64.dp.toPx() })
        }

        Surface(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = offsetX.coerceIn(padPx, (maxWidthPx - pillWidthPx - padPx).coerceAtLeast(padPx)).roundToInt(),
                        y = offsetY.coerceIn(padPx, (maxHeightPx - pillHeightPx - padPx).coerceAtLeast(padPx)).roundToInt(),
                    )
                }
                .pointerInput(maxWidthPx, maxHeightPx) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX = (offsetX + dragAmount.x)
                            .coerceIn(padPx, (maxWidthPx - pillWidthPx - padPx).coerceAtLeast(padPx))
                        offsetY = (offsetY + dragAmount.y)
                            .coerceIn(padPx, (maxHeightPx - pillHeightPx - padPx).coerceAtLeast(padPx))
                    }
                }
                .testTag("floating_fps_overlay"),
            shape = RoundedCornerShape(999.dp),
            color = ObsidianBg.copy(alpha = 0.90f),
            border = BorderStroke(1.5.dp, fpsColor.copy(alpha = 0.7f)),
            shadowElevation = 8.dp,
        ) {
            Row(
                modifier = Modifier.padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.DragIndicator,
                    contentDescription = "Drag FPS counter",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp),
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(fpsColor),
                )
                Text(
                    text = "$liveFps FPS",
                    style = MaterialTheme.typography.labelLarge,
                    color = fpsColor,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "${"%.1f".format(frameTimeMs)}ms • ${refreshRateHz}Hz",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("dismiss_floating_fps_button"),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Hide floating FPS counter",
                        tint = TextMuted,
                        modifier = Modifier.size(15.dp),
                    )
                }
            }
        }
    }
}
