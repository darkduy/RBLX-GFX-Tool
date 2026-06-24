package com.gfxtool.roblox

import com.google.gson.JsonParser
import com.gfxtool.roblox.data.model.GfxSettings
import com.gfxtool.roblox.data.model.RobloxConfigMapper
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests cho logic sinh config Roblox.
 * Chạy: ./gradlew test
 */
class RobloxConfigMapperTest {

    // ── Helpers ───────────────────────────────────────────────────

    private fun parseJson(settings: GfxSettings) =
        JsonParser.parseString(RobloxConfigMapper.toJson(settings)).asJsonObject

    // ── FPS ───────────────────────────────────────────────────────

    @Test
    fun `fps 30 maps to correct value`() {
        val json = parseJson(GfxSettings(fpsCapIndex = 0))
        assertEquals(30, json["DFIntTaskSchedulerTargetFps"].asInt)
    }

    @Test
    fun `fps 60 maps to correct value`() {
        val json = parseJson(GfxSettings(fpsCapIndex = 1))
        assertEquals(60, json["DFIntTaskSchedulerTargetFps"].asInt)
    }

    @Test
    fun `fps 120 maps to correct value`() {
        val json = parseJson(GfxSettings(fpsCapIndex = 2))
        assertEquals(120, json["DFIntTaskSchedulerTargetFps"].asInt)
    }

    @Test
    fun `fps unlimited maps to 9999`() {
        val json = parseJson(GfxSettings(fpsCapIndex = 3))
        assertEquals(9999, json["DFIntTaskSchedulerTargetFps"].asInt)
    }

    // ── Resolution ────────────────────────────────────────────────

    @Test
    fun `resolution scale 100 percent maps to 100`() {
        val json = parseJson(GfxSettings(renderResolutionScale = 1.0f))
        assertEquals(100, json["DFIntRenderResolutionScale"].asInt)
    }

    @Test
    fun `resolution scale 75 percent maps to 75`() {
        val json = parseJson(GfxSettings(renderResolutionScale = 0.75f))
        assertEquals(75, json["DFIntRenderResolutionScale"].asInt)
    }

    // ── Shadows ───────────────────────────────────────────────────

    @Test
    fun `shadows disabled sets intensity to 0`() {
        val json = parseJson(GfxSettings(shadowsEnabled = false))
        assertEquals(0, json["FIntRenderShadowIntensity"].asInt)
    }

    @Test
    fun `shadows enabled with high quality sets high intensity`() {
        val json = parseJson(GfxSettings(shadowsEnabled = true, shadowQuality = 3))
        assertTrue(json["FIntRenderShadowIntensity"].asInt > 0)
    }

    // ── Textures ──────────────────────────────────────────────────

    @Test
    fun `textures disabled sets quality to 0`() {
        val json = parseJson(GfxSettings(texturesEnabled = false))
        assertEquals(0, json["DFIntTextureQualityOverride"].asInt)
        assertEquals(false, json["DFFlagTextureQualityOverrideEnabled"].asBoolean)
    }

    // ── Particles ─────────────────────────────────────────────────

    @Test
    fun `particles disabled sets max to 0`() {
        val json = parseJson(GfxSettings(particlesEnabled = false))
        assertEquals(0, json["DFIntParticleMaxNumParticles"].asInt)
        assertEquals(0, json["DFIntParticleMaxEmissionRate"].asInt)
    }

    // ── Lighting ──────────────────────────────────────────────────

    @Test
    fun `future lighting sets both shadow and attenuation flags`() {
        val json = parseJson(GfxSettings(lightingTechnologyIndex = 3))
        assertTrue(json["DFFlagDebugRenderForceShadowTexture"].asBoolean)
        assertTrue(json["DFFlagNewLightAttenuation"].asBoolean)
        assertTrue(json["DFFlagRenderShadowVolume"].asBoolean)
    }

    @Test
    fun `compatibility lighting disables both flags`() {
        val json = parseJson(GfxSettings(lightingTechnologyIndex = 0))
        assertFalse(json["DFFlagDebugRenderForceShadowTexture"].asBoolean)
        assertFalse(json["DFFlagNewLightAttenuation"].asBoolean)
    }

    // ── Post Processing ───────────────────────────────────────────

    @Test
    fun `post processing disabled turns off all post fx flags`() {
        val json = parseJson(GfxSettings(
            postProcessingEnabled = false,
            bloomEnabled          = true,   // should be overridden
            sunRaysEnabled        = true,
        ))
        assertTrue(json["DFFlagDisablePostFx"].asBoolean)
        assertFalse(json["DFFlagRenderBloom"].asBoolean)
        assertFalse(json["DFFlagRenderSunRays"].asBoolean)
    }

    // ── Fog ───────────────────────────────────────────────────────

    @Test
    fun `fog disabled sets debug flag true`() {
        val json = parseJson(GfxSettings(fogEnabled = false))
        assertTrue(json["DFFlagDebugDisableFog"].asBoolean)
    }

    // ── Preset sanity ─────────────────────────────────────────────

    @Test
    fun `performance preset has shadows disabled`() {
        assertFalse(GfxSettings.PRESET_PERFORMANCE.shadowsEnabled)
        assertFalse(GfxSettings.PRESET_PERFORMANCE.particlesEnabled)
        assertEquals(0, GfxSettings.PRESET_PERFORMANCE.shadowQuality)
    }

    @Test
    fun `ultra preset has future lighting`() {
        assertEquals(3, GfxSettings.PRESET_ULTRA.lightingTechnologyIndex)
        assertTrue(GfxSettings.PRESET_ULTRA.bloomEnabled)
        assertTrue(GfxSettings.PRESET_ULTRA.ambientOcclusion)
    }

    @Test
    fun `all presets produce valid json`() {
        listOf(
            GfxSettings.PRESET_PERFORMANCE,
            GfxSettings.PRESET_BALANCED,
            GfxSettings.PRESET_ULTRA,
        ).forEach { preset ->
            val json = RobloxConfigMapper.toJson(preset)
            assertTrue("Preset JSON rỗng", json.isNotBlank())
            // Phải parse được không throw
            JsonParser.parseString(json).asJsonObject
        }
    }
}
