package com.gfxtool.roblox.data.model

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject

/**
 * Chuyển đổi [GfxSettings] sang định dạng JSON mà Roblox đọc được
 * (ClientAppSettings.json / FVariables).
 *
 * Tham khảo: Roblox Fast Flags documentation (community-researched).
 */
object RobloxConfigMapper {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    /**
     * Sinh ra nội dung JSON đầy đủ để ghi vào ClientAppSettings.json.
     */
    fun toJson(s: GfxSettings): String {
        val obj = JsonObject()

        // ── Graphics Quality ────────────────────────────────────
        obj.addProperty("DFFlagDebugGraphicsDisableVulkan", false)
        obj.addProperty("FIntRenderLocalLightUpdatesMax", qualityToLightUpdates(s.graphicsQuality))
        obj.addProperty("DFIntTaskSchedulerTargetFps", GfxSettings.FPS_OPTIONS[s.fpsCapIndex].takeIf { it > 0 } ?: 9999)

        // ── Resolution Scale ────────────────────────────────────
        obj.addProperty("DFFlagDebugRenderForceLOD0", s.graphicsQuality == 1)
        obj.addProperty("DFIntRenderResolutionScale", (s.renderResolutionScale * 100).toInt())

        // ── Shadows ─────────────────────────────────────────────
        obj.addProperty("DFFlagDebugForceFastGPULightCulling3", !s.shadowsEnabled)
        obj.addProperty("FIntRenderShadowIntensity", if (s.shadowsEnabled) shadowQualityToIntensity(s.shadowQuality) else 0)
        obj.addProperty("DFIntRenderShadowFadeDistance", if (s.shadowsEnabled) 250 else 0)
        obj.addProperty("DFIntCullFactorPixelThresholdShadowMapHighQuality", if (s.shadowQuality >= 2) 64 else 256)
        obj.addProperty("DFIntCullFactorPixelThresholdShadowMapLowQuality", if (s.shadowQuality >= 1) 128 else 512)

        // ── Textures ────────────────────────────────────────────
        obj.addProperty("DFIntTextureQualityOverride", if (s.texturesEnabled) s.textureQuality else 0)
        obj.addProperty("DFFlagTextureQualityOverrideEnabled", s.texturesEnabled)

        // ── Particles & Effects ─────────────────────────────────
        obj.addProperty("DFIntParticleMaxEmissionRate", if (s.particlesEnabled) 1000 else 0)
        obj.addProperty("DFIntParticleMaxNumParticles", if (s.particlesEnabled) 20000 else 0)
        obj.addProperty("DFFlagDebugRenderingSetDeterministic", false)
        obj.addProperty("FIntExplosionParticleCount", if (s.explosionsEnabled) 20 else 0)
        obj.addProperty("DFFlagRenderClouds", s.cloudsEnabled)
        obj.addProperty("FIntRenderGrassDetailStrands", if (s.grassRenderingEnabled) 50 else 0)

        // ── Lighting Technology ─────────────────────────────────
        when (s.lightingTechnologyIndex) {
            0 -> {  // Compatibility
                obj.addProperty("DFFlagDebugRenderForceShadowTexture", false)
                obj.addProperty("DFFlagNewLightAttenuation", false)
            }
            1 -> {  // ShadowMap
                obj.addProperty("DFFlagDebugRenderForceShadowTexture", true)
                obj.addProperty("DFFlagNewLightAttenuation", false)
            }
            2 -> {  // Voxel
                obj.addProperty("DFFlagDebugRenderForceShadowTexture", false)
                obj.addProperty("DFFlagNewLightAttenuation", true)
            }
            3 -> {  // Future
                obj.addProperty("DFFlagDebugRenderForceShadowTexture", true)
                obj.addProperty("DFFlagNewLightAttenuation", true)
                obj.addProperty("DFFlagRenderShadowVolume", true)
            }
        }

        // ── Post Processing ─────────────────────────────────────
        obj.addProperty("DFFlagDisablePostFx", !s.postProcessingEnabled)
        obj.addProperty("DFFlagRenderBloom", s.bloomEnabled && s.postProcessingEnabled)
        obj.addProperty("DFFlagRenderSunRays", s.sunRaysEnabled && s.postProcessingEnabled)
        obj.addProperty("DFFlagRenderDepthOfField", s.depthOfFieldEnabled && s.postProcessingEnabled)
        obj.addProperty("DFFlagDebugSkyGray", !s.postProcessingEnabled)

        // ── Ambient Occlusion ───────────────────────────────────
        obj.addProperty("DFFlagRenderAmbientOcclusion", s.ambientOcclusion)
        obj.addProperty("DFFlagRenderAmbientOcclusionDownscaled", s.ambientOcclusion)

        // ── Fog ─────────────────────────────────────────────────
        obj.addProperty("DFFlagDebugDisableFog", !s.fogEnabled)
        if (s.fogEnabled) {
            obj.addProperty("DFIntFogStartDistance", s.fogStart.toInt())
            obj.addProperty("DFIntFogEndDistance", s.fogEnd.toInt())
        }

        // ── Anti-Aliasing ───────────────────────────────────────
        obj.addProperty("DFFlagDebugRenderingDisableAntiAliasing", !s.antiAliasingEnabled)

        // ── Water ───────────────────────────────────────────────
        obj.addProperty("DFFlagRenderReflections", s.waterReflectionsEnabled)
        obj.addProperty("FIntRenderReflectionRate", if (s.waterReflectionsEnabled) 4 else 0)

        return gson.toJson(obj)
    }

    // ── Helpers ────────────────────────────────────────────────────

    private fun qualityToLightUpdates(quality: Int): Int =
        when {
            quality <= 2  -> 4
            quality <= 5  -> 8
            quality <= 8  -> 16
            else          -> 24
        }

    private fun shadowQualityToIntensity(q: Int): Int =
        when (q) {
            0    -> 0
            1    -> 64
            2    -> 128
            else -> 200
        }
}
