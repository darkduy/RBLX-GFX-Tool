package com.gfxtool.roblox.data.model

/**
 * Toàn bộ cài đặt GFX của Roblox.
 * Tương ứng với các key trong ClientAppSettings.json của Roblox Android.
 */
data class GfxSettings(
    // ── Graphics Quality ──────────────────────────────────────────
    /** 1–10, map sang GfxQualityLevel của Roblox */
    val graphicsQuality: Int = 5,

    // ── Resolution / FPS ─────────────────────────────────────────
    /** Scale màn hình: 0.5x → 1.0x (50% → 100%) */
    val renderResolutionScale: Float = 1.0f,
    /** Giới hạn FPS: 30 / 60 / 120 / 0 (unlimited) */
    val fpsCapIndex: Int = 1,           // index vào FPS_OPTIONS

    // ── Shadows ───────────────────────────────────────────────────
    val shadowsEnabled: Boolean = true,
    /** Shadow quality: 0=Off, 1=Low, 2=Medium, 3=High */
    val shadowQuality: Int = 2,

    // ── Textures ──────────────────────────────────────────────────
    /** 0=Low, 1=Medium, 2=High */
    val textureQuality: Int = 2,
    val texturesEnabled: Boolean = true,

    // ── Particles & Effects ───────────────────────────────────────
    val particlesEnabled: Boolean = true,
    val explosionsEnabled: Boolean = true,
    val cloudsEnabled: Boolean = true,

    // ── Lighting ──────────────────────────────────────────────────
    /** 0=Compatibility, 1=ShadowMap, 2=Voxel, 3=Future */
    val lightingTechnologyIndex: Int = 1,
    val ambientOcclusion: Boolean = false,
    val bloomEnabled: Boolean = true,
    val sunRaysEnabled: Boolean = true,
    val depthOfFieldEnabled: Boolean = false,

    // ── Fog ───────────────────────────────────────────────────────
    val fogEnabled: Boolean = true,
    val fogStart: Float = 0f,
    val fogEnd: Float = 100000f,

    // ── Misc ─────────────────────────────────────────────────────
    val antiAliasingEnabled: Boolean = true,
    val postProcessingEnabled: Boolean = true,
    val grassRenderingEnabled: Boolean = true,
    val waterReflectionsEnabled: Boolean = true,
) {
    companion object {
        val FPS_OPTIONS = listOf(30, 60, 120, 0)
        val FPS_LABELS  = listOf("30 FPS", "60 FPS", "120 FPS", "Unlimited")

        val QUALITY_LABELS = listOf("Off", "Low", "Medium", "High")

        val LIGHTING_OPTIONS = listOf("Compatibility", "ShadowMap", "Voxel", "Future")

        /** Preset: Performance - tối đa FPS, tối thiểu visual */
        val PRESET_PERFORMANCE = GfxSettings(
            graphicsQuality        = 1,
            renderResolutionScale  = 0.75f,
            fpsCapIndex            = 2,       // 120 FPS
            shadowsEnabled         = false,
            shadowQuality          = 0,
            textureQuality         = 0,
            particlesEnabled       = false,
            explosionsEnabled      = false,
            cloudsEnabled          = false,
            lightingTechnologyIndex = 0,       // Compatibility
            ambientOcclusion       = false,
            bloomEnabled           = false,
            sunRaysEnabled         = false,
            depthOfFieldEnabled    = false,
            fogEnabled             = false,
            antiAliasingEnabled    = false,
            postProcessingEnabled  = false,
            grassRenderingEnabled  = false,
            waterReflectionsEnabled = false,
        )

        /** Preset: Balanced */
        val PRESET_BALANCED = GfxSettings()   // default values

        /** Preset: Ultra - đẹp nhất */
        val PRESET_ULTRA = GfxSettings(
            graphicsQuality         = 10,
            renderResolutionScale   = 1.0f,
            fpsCapIndex             = 1,       // 60 FPS stable
            shadowsEnabled          = true,
            shadowQuality           = 3,
            textureQuality          = 2,
            particlesEnabled        = true,
            cloudsEnabled           = true,
            lightingTechnologyIndex = 3,       // Future
            ambientOcclusion        = true,
            bloomEnabled            = true,
            sunRaysEnabled          = true,
            depthOfFieldEnabled     = true,
            antiAliasingEnabled     = true,
            postProcessingEnabled   = true,
            grassRenderingEnabled   = true,
            waterReflectionsEnabled = true,
        )
    }
}

/**
 * Preset được lưu tên do user tạo.
 */
data class UserPreset(
    val id: String,
    val name: String,
    val settings: GfxSettings,
    val createdAt: Long = System.currentTimeMillis(),
)

/**
 * Chế độ áp dụng config.
 */
enum class ApplyMode {
    /** Ghi vào thư mục Roblox không cần root (Android 10 scoped storage workaround) */
    FILE_CONFIG,
    /** Dùng root shell để patch trực tiếp */
    ROOT_SHELL,
}
