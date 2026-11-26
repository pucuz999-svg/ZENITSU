package com.example.model

enum class PlatformBrand(val label: String) {
    ALL("All"),
    DETECTED("This Device"),
    SAVED("Saved"),
    SAMSUNG("Samsung"),
    XIAOMI_POCO("Xiaomi / Poco"),
    MOTOROLA("Motorola"),
    IPHONE_IPAD("iPhone / iPad"),
    ASUS_REDMAGIC("ROG / RedMagic"),
    ONEPLUS_REALME("OnePlus / Realme"),
    VIVO_IQOO("Vivo / iQOO"),
    OPPO_OTHERS("Oppo / Pixel / More"),
    EMULATOR_PC("PC Emulator"),
}

enum class PlayStyleMode(
    val label: String,
    val shortLabel: String,
    val description: String,
) {
    ONE_TAP_DRAG(
        label = "One-Tap Drag Headshot",
        shortLabel = "One-Tap",
        description = "High snap-up General & Red Dot for Desert Eagle, Woodpecker & M1887.",
    ),
    SMG_SPRAY(
        label = "SMG Close-Range Spray",
        shortLabel = "SMG Spray",
        description = "Controlled recoil tracking for MP40, UMP & Thompson rushes.",
    ),
    BALANCED_RUSH(
        label = "Balanced Clash Squad",
        shortLabel = "Balanced",
        description = "All-round tournament preset for fast gloo walls and mid-range drags.",
    ),
    SNIPER_PRECISION(
        label = "Sniper & Long Range",
        shortLabel = "Sniper",
        description = "Stable Sniper & 4x scope micro-aim with steady crosshairs.",
    ),
}

enum class SmoothPlayPriority(
    val label: String,
    val subtitle: String,
) {
    MAX_FPS("Max Smooth FPS", "Prioritizes locked 60–120 FPS frame pacing over shadows."),
    ZERO_LAG_SQUAD("Zero-Lag Squad", "Eliminates final-circle gloo-wall & smoke stutter."),
    BALANCED_HEADSHOT("Balanced Drag", "Crisp visuals paired with responsive drag-headshot tracking."),
}

data class SensitivityValues(
    val general: Int,
    val redDot: Int,
    val scope2x: Int,
    val scope4x: Int,
    val sniperScope: Int,
    val freeLook: Int,
    val fireButtonPercent: Int,
    val recommendedDpi: Int,
) {
    fun tunedFor(mode: PlayStyleMode): SensitivityValues = when (mode) {
        PlayStyleMode.ONE_TAP_DRAG -> copy(
            general = (general + 6).coerceIn(80, 200),
            redDot = (redDot + 8).coerceIn(80, 200),
            fireButtonPercent = (fireButtonPercent - 3).coerceIn(36, 68),
        )
        PlayStyleMode.SMG_SPRAY -> copy(
            general = (general + 2).coerceIn(80, 200),
            redDot = (redDot - 4).coerceIn(70, 200),
            scope2x = (scope2x + 5).coerceIn(70, 200),
            fireButtonPercent = (fireButtonPercent + 4).coerceIn(40, 72),
        )
        PlayStyleMode.BALANCED_RUSH -> this
        PlayStyleMode.SNIPER_PRECISION -> copy(
            general = (general - 6).coerceIn(70, 200),
            scope4x = (scope4x - 8).coerceIn(60, 200),
            sniperScope = (sniperScope - 12).coerceIn(40, 195),
            fireButtonPercent = (fireButtonPercent + 2).coerceIn(40, 72),
        )
    }
}

data class FpsGraphicsPreset(
    val graphicsQuality: String,
    val fpsMode: String,
    val targetFps: Int,
    val shadowEnabled: Boolean,
    val highResolution: String,
    val filter: String,
    val autoScale: Boolean,
    val rationale: String,
)

data class DeviceSensitivityProfile(
    val id: String,
    val brand: PlatformBrand,
    val deviceName: String,
    val platformBadge: String,
    val ramTier: String,
    val displayHz: Int,
    val screenSpec: String,
    val baseSensitivity: SensitivityValues,
    val fpsPreset: FpsGraphicsPreset,
    val dragTip: String,
    val preferredTouchSetting: String = "",
    val dragTechnique: String = "",
    val isDetectedCurrentDevice: Boolean = false,
)

data class DeviceSmoothSuggestion(
    val targetDevice: DeviceSensitivityProfile,
    val priority: SmoothPlayPriority,
    val recommendedFps: FpsGraphicsPreset,
    val recommendedSensitivity: SensitivityValues,
    val hardwareNote: String,
    val similarDevices: List<DeviceSensitivityProfile>,
)

data class ActiveAppliedProfile(
    val sourceName: String,
    val sensitivity: SensitivityValues,
    val fpsPreset: FpsGraphicsPreset,
    val isCustomized: Boolean,
)
